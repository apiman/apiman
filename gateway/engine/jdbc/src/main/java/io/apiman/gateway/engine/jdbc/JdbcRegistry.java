/*
 * Copyright 2016 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.gateway.engine.jdbc;

import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.jdbc.i18n.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A JDBC implementation of the gateway registry.  Only suitable for a 
 * synchronous environment - should not be used when running an async 
 * Gateway (e.g. vert.x).
 * 
 * Must be configured with the JNDI location of the datasource to use.
 * Example:
 * 
 *     apiman-gateway.registry=io.apiman.gateway.engine.jdbc.JdbcRegistry
 *     apiman-gateway.registry.datasource.jndi-location=java:jboss/datasources/apiman-gateway
 * 
 * @author ewittman
 */
public class JdbcRegistry implements IRegistry {
    
    protected static final ObjectMapper mapper = new ObjectMapper();
    protected DataSource ds;

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public JdbcRegistry(Map<String, String> config) {
        String dsJndiLocation = config.get("datasource.jndi-location"); //$NON-NLS-1$
        if (dsJndiLocation == null) {
            throw new RuntimeException("Missing datasource JNDI location from JdbcRegistry configuration."); //$NON-NLS-1$
        }
        ds = lookupDS(dsJndiLocation);
    }
    
    /**
     * Lookup the datasource in JNDI.
     * @param dsJndiLocation
     */
    private static DataSource lookupDS(String dsJndiLocation) {
        DataSource ds;
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(dsJndiLocation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (ds == null) {
            throw new RuntimeException("Datasource not found: " + dsJndiLocation); //$NON-NLS-1$
        }
        return ds;
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#publishApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            QueryRunner run = new QueryRunner();

            // First delete any record we might already have.
            run.update(conn, "DELETE FROM apis WHERE org_id = ? AND id = ? AND version = ?",  //$NON-NLS-1$
                    api.getOrganizationId(), api.getApiId(), api.getVersion());

            // Now insert a row for the api.
            String bean = mapper.writeValueAsString(api);
            run.update(conn, "INSERT INTO apis (org_id, id, version, bean) VALUES (?, ?, ?, ?)",  //$NON-NLS-1$
                    api.getOrganizationId(), api.getApiId(), api.getVersion(), bean);

            DbUtils.commitAndClose(conn);
            handler.handle(AsyncResultImpl.create((Void) null, Void.class));
        } catch (SQLException | JsonProcessingException e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.IRegistry#registerClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            QueryRunner run = new QueryRunner();

            // Validate the client and populate the api map with apis found during validation.
            validateClient(client, conn);

            // Remove any old data first, then (re)insert
            run.update(conn, "DELETE FROM clients WHERE org_id = ? AND id = ? AND version = ?",  //$NON-NLS-1$
                    client.getOrganizationId(), client.getClientId(), client.getVersion());

            String bean = mapper.writeValueAsString(client);
            run.update(conn, "INSERT INTO clients (api_key, org_id, id, version, bean) VALUES (?, ?, ?, ?, ?)",  //$NON-NLS-1$
                    client.getApiKey(), client.getOrganizationId(), client.getClientId(), client.getVersion(), bean);
            
            DbUtils.commitAndClose(conn);
            handler.handle(AsyncResultImpl.create((Void) null));
        } catch (Exception re) {
            DbUtils.rollbackAndCloseQuietly(conn);
            handler.handle(AsyncResultImpl.create(re, Void.class));
        }
    }

    /**
     * Removes all of the api contracts from the database.
     * @param client
     * @param connection 
     * @throws SQLException
     */
    protected void unregisterApiContracts(Client client, Connection connection) throws SQLException {
        QueryRunner run = new QueryRunner();
        run.update(connection, "DELETE FROM contracts WHERE client_org_id = ? AND client_id = ? AND client_version = ?",  //$NON-NLS-1$
                client.getOrganizationId(), client.getClientId(), client.getVersion());
    }

    /**
     * Ensures that the api referenced by the Contract actually exists (is published).
     * @param contract
     * @param connection
     * @throws RegistrationException
     */
    private void validateContract(final Contract contract, Connection connection)
            throws RegistrationException {
        QueryRunner run = new QueryRunner();
        try {
            ResultSetHandler<Api> handler = (ResultSet rs) -> {
                if (!rs.next()) {
                    return null;
                }
                Clob clob = rs.getClob(1);
                InputStream is = clob.getAsciiStream();
                try {
                    return mapper.reader(Api.class).readValue(is);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };
            Api api = run.query(connection, "SELECT bean FROM apis WHERE org_id = ? AND id = ? AND version = ?", //$NON-NLS-1$
                    handler, contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
            if (api == null) {
                String apiId = contract.getApiId();
                String orgId = contract.getApiOrgId();
                throw new RegistrationException(Messages.i18n.format("JdbcRegistry.ApiNotFoundInOrg", apiId, orgId));  //$NON-NLS-1$
            }
        } catch (SQLException e) {
            throw new RegistrationException(Messages.i18n.format("JdbcRegistry.ErrorValidatingApp"), e); //$NON-NLS-1$
        }
    }

    /**
     * Validate that the client should be registered.
     * @param client
     * @param connection
     */
    private void validateClient(Client client, Connection connection) throws RegistrationException {
        Set<Contract> contracts = client.getContracts();
        if (contracts.isEmpty()) {
            throw new RegistrationException(Messages.i18n.format("JdbcRegistry.NoContracts")); //$NON-NLS-1$
        }
        for (Contract contract : contracts) {
            validateContract(contract, connection);
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#retireApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        QueryRunner run = new QueryRunner(ds);
        try {
            run.update("DELETE FROM apis WHERE org_id = ? AND id = ? AND version = ?",  //$NON-NLS-1$
                    api.getOrganizationId(), api.getApiId(), api.getVersion());
            handler.handle(AsyncResultImpl.create((Void) null, Void.class));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        try {
            QueryRunner run = new QueryRunner(ds);
            run.update("DELETE FROM clients WHERE org_id = ? AND id = ? AND version = ?",  //$NON-NLS-1$
                    client.getOrganizationId(), client.getClientId(), client.getVersion());
            handler.handle(AsyncResultImpl.create((Void) null));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("JdbcRegistry.ErrorUnregisteringApp"), e), Void.class)); //$NON-NLS-1$
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.IRegistry#getApi(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getApi(String organizationId, String apiId, String apiVersion,
            IAsyncResultHandler<Api> handler) {
        try {
            Api api = getApiInternal(organizationId, apiId, apiVersion);
            handler.handle(AsyncResultImpl.create(api));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }

    /**
     * Gets an api from the DB.
     * @param organizationId
     * @param apiId
     * @param apiVersion
     * @throws SQLException
     */
    protected Api getApiInternal(String organizationId, String apiId, String apiVersion) throws SQLException {
        QueryRunner run = new QueryRunner(ds);
        return run.query("SELECT bean FROM apis WHERE org_id = ? AND id = ? AND version = ?", //$NON-NLS-1$
                Handlers.API_HANDLER, organizationId, apiId, apiVersion);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getClient(java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getClient(String apiKey, IAsyncResultHandler<Client> handler) {
        try {
            Client client = getClientInternal(apiKey);
            handler.handle(AsyncResultImpl.create(client));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e, Client.class));
        }
    }

    /**
     * Simply pull the client from storage.
     * @param apiKey
     * @throws SQLException
     */
    protected Client getClientInternal(String apiKey) throws SQLException {
        QueryRunner run = new QueryRunner(ds);
        return run.query("SELECT bean FROM clients WHERE api_key = ?", //$NON-NLS-1$
                Handlers.CLIENT_HANDLER, apiKey);
    }
    
    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(String apiOrganizationId, String apiId, String apiVersion, String apiKey,
            IAsyncResultHandler<ApiContract> handler) {
        try {
            Client client = getClientInternal(apiKey);
            Api api = getApiInternal(apiOrganizationId, apiId, apiVersion);

            if (client == null) {
                Exception error = new InvalidContractException(Messages.i18n.format("JdbcRegistry.NoClientForAPIKey", apiKey)); //$NON-NLS-1$
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                return;
            }
            if (api == null) {
                Exception error = new InvalidContractException(Messages.i18n.format("JdbcRegistry.ApiWasRetired", //$NON-NLS-1$
                        apiId, apiOrganizationId));
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                return;
            }
            
            Contract matchedContract = null;
            for (Contract contract : client.getContracts()) {
                if (contract.matches(apiOrganizationId, apiId, apiVersion)) {
                    matchedContract = contract;
                    break;
                }
            }
            
            if (matchedContract == null) {
                Exception error = new InvalidContractException(Messages.i18n.format("JdbcRegistry.NoContractFound", //$NON-NLS-1$
                        client.getClientId(), api.getApiId()));
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                return;
            }
            
            ApiContract contract = new ApiContract(api, client, matchedContract.getPlan(), matchedContract.getPolicies());
            handler.handle(AsyncResultImpl.create(contract));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(e, ApiContract.class));
        }
    }

    /**
     * Generates a valid document ID for a api referenced by a contract, used to
     * retrieve the api from ES.
     * @param contract
     */
    protected String getApiId(Contract contract) {
        return getApiId(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
    }

    /**
     * Generates a valid document ID for a api, used to index the api in ES.
     * @param orgId
     * @param apiId
     * @param version
     * @return a api key
     */
    protected String getApiId(String orgId, String apiId, String version) {
        return orgId + "|" + apiId + "|" + version; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static final class Handlers {
        public static final ResultSetHandler<Api> API_HANDLER = (ResultSet rs) -> {
            if (!rs.next()) {
                return null;
            }
            Clob clob = rs.getClob(1);
            InputStream is = clob.getAsciiStream();
            try {
                return mapper.reader(Api.class).readValue(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        public static final ResultSetHandler<Client> CLIENT_HANDLER = (ResultSet rs) -> {
            if (!rs.next()) {
                return null;
            }
            Clob clob = rs.getClob(1);
            InputStream is = clob.getAsciiStream();
            try {
                return mapper.reader(Client.class).readValue(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
    
}
