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
import io.apiman.gateway.engine.beans.exceptions.ApiNotFoundException;
import io.apiman.gateway.engine.beans.exceptions.ApiRetiredException;
import io.apiman.gateway.engine.beans.exceptions.ClientNotFoundException;
import io.apiman.gateway.engine.beans.exceptions.NoContractFoundException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.jdbc.i18n.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

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
 *     apiman-gateway.registry.datasource.jndi-location=java:/apiman/datasources/apiman-gateway
 *
 * @author ewittman
 */
public class JdbcRegistry extends AbstractJdbcComponent implements IRegistry {

    protected static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public JdbcRegistry(Map<String, String> config) {
        super(config);
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
            run.update(conn, "DELETE FROM gw_apis WHERE org_id = ? AND id = ? AND version = ?",  //$NON-NLS-1$
                    api.getOrganizationId(), api.getApiId(), api.getVersion());

            // Now insert a row for the api.
            String bean = mapper.writeValueAsString(api);
            run.update(conn, "INSERT INTO gw_apis (org_id, id, version, bean) VALUES (?, ?, ?, ?)",  //$NON-NLS-1$
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
            run.update(conn, "DELETE FROM gw_clients WHERE org_id = ? AND id = ? AND version = ?",  //$NON-NLS-1$
                    client.getOrganizationId(), client.getClientId(), client.getVersion());

            String bean = mapper.writeValueAsString(client);
            run.update(conn, "INSERT INTO gw_clients (api_key, org_id, id, version, bean) VALUES (?, ?, ?, ?, ?)",  //$NON-NLS-1$
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
            Api api = run.query(connection, "SELECT bean FROM gw_apis WHERE org_id = ? AND id = ? AND version = ?", //$NON-NLS-1$
                    Handlers.API_HANDLER, contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
            if (api == null) {
                String apiId = contract.getApiId();
                String orgId = contract.getApiOrgId();
                throw new ApiNotFoundException(Messages.i18n.format("JdbcRegistry.ApiNotFoundInOrg", apiId, orgId));  //$NON-NLS-1$
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
            run.update("DELETE FROM gw_apis WHERE org_id = ? AND id = ? AND version = ?",  //$NON-NLS-1$
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
            run.update("DELETE FROM gw_clients WHERE org_id = ? AND id = ? AND version = ?",  //$NON-NLS-1$
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


    @Override
    public void getClient(String organizationId, String clientId, String clientVersion,
            IAsyncResultHandler<Client> handler) {
        try {
            QueryRunner run = new QueryRunner(ds);
            Client client = run.query("SELECT bean FROM gw_clients WHERE org_id = ? AND id = ? AND version = ?", //$NON-NLS-1$
                    Handlers.CLIENT_HANDLER, organizationId, clientId, clientVersion);
            handler.handle(AsyncResultImpl.create(client));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e, Client.class));
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
        return run.query("SELECT bean FROM gw_apis WHERE org_id = ? AND id = ? AND version = ?", //$NON-NLS-1$
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
        return run.query("SELECT bean FROM gw_clients WHERE api_key = ?", //$NON-NLS-1$
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
                Exception error = new ClientNotFoundException(Messages.i18n.format("JdbcRegistry.NoClientForAPIKey", apiKey)); //$NON-NLS-1$
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                return;
            }
            if (api == null) {
                Exception error = new ApiRetiredException(Messages.i18n.format("JdbcRegistry.ApiWasRetired", //$NON-NLS-1$
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
                Exception error = new NoContractFoundException(Messages.i18n.format("JdbcRegistry.NoContractFound", //$NON-NLS-1$
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

    @Override
    public void listApis(String organizationId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        QueryRunner run = new QueryRunner(ds);
        try {
            List<String> apiList = run.query("SELECT DISTINCT id FROM gw_apis WHERE org_id = ?",
                    Handlers.STRING_LIST_COL1_HANDLER, organizationId);
            handler.handle(AsyncResultImpl.create(apiList));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }

    @Override
    @SuppressWarnings("nls")
    public void listOrgs(IAsyncResultHandler<List<String>> handler) {
        QueryRunner run = new QueryRunner(ds);
        try {
            List<String> orgList = run.query("SELECT DISTINCT merged.org_id\n" +
                    "FROM\n" +
                    "    (\n" +
                    "        SELECT\n" +
                    "            org_id\n" +
                    "        FROM\n" +
                    "            gw_apis\n" +
                    "    UNION \n" +
                    "        SELECT\n" +
                    "            org_id\n" +
                    "        FROM\n" +
                    "            gw_clients\n" +
                    "    ) merged;",
                    Handlers.STRING_LIST_COL1_HANDLER);
            handler.handle(AsyncResultImpl.create(orgList));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }

    @Override
    public void listApiVersions(String organizationId, String apiId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        QueryRunner run = new QueryRunner(ds);
        try {
            List<String> apiVersions = run.query("SELECT DISTINCT version FROM gw_apis WHERE org_id = ? AND id = ?",
                    Handlers.STRING_LIST_COL1_HANDLER, organizationId, apiId);
            handler.handle(AsyncResultImpl.create(apiVersions));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }

    @Override
    public void listClients(String organizationId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        QueryRunner run = new QueryRunner(ds);
        try {
            List<String> clientList = run.query("SELECT DISTINCT id FROM gw_clients WHERE org_id = ?",
                    Handlers.STRING_LIST_COL1_HANDLER, organizationId);
            handler.handle(AsyncResultImpl.create(clientList));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }

    @Override
    public void listClientVersions(String organizationId, String clientId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        QueryRunner run = new QueryRunner(ds);
        try {
            List<String> clientVersions = run.query("SELECT DISTINCT version FROM gw_clients WHERE org_id = ? AND id = ?",
                    Handlers.STRING_LIST_COL1_HANDLER, organizationId, clientId);
            handler.handle(AsyncResultImpl.create(clientVersions));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e));
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
        public static final AbstractListHandler<String> STRING_LIST_COL1_HANDLER = new AbstractListHandler<String>() {

            @Override
            protected String handleRow(ResultSet rs) throws SQLException {
                return rs.getString(1);
            }

        };

        public static final ResultSetHandler<Api> API_HANDLER = (ResultSet rs) -> {
            if (!rs.next()) {
                return null;
            }
            try (InputStream is = rs.getAsciiStream(1)) {
                return mapper.reader(Api.class).readValue(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        public static final ResultSetHandler<Client> CLIENT_HANDLER = (ResultSet rs) -> {
            if (!rs.next()) {
                return null;
            }
            try (InputStream is = rs.getAsciiStream(1)) {
                return mapper.reader(Client.class).readValue(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
