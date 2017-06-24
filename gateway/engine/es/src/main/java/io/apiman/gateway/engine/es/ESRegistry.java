/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.gateway.engine.es;

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
import io.apiman.gateway.engine.es.i18n.Messages;
import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.params.Parameters;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the Registry that uses elasticsearch as a storage
 * mechanism.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESRegistry extends AbstractESComponent implements IRegistry {

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public ESRegistry(Map<String, String> config) {
        super(config);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#publishApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishApi(final Api api, final IAsyncResultHandler<Void> handler) {
        try {
            String id = getApiId(api);
            Index index = new Index.Builder(api).refresh(false)
                    .index(getIndexName()).setParameter(Parameters.OP_TYPE, "index") //$NON-NLS-1$
                    .type("api").id(id).build(); //$NON-NLS-1$
            JestResult result = getClient().execute(index);
            if (!result.isSucceeded()) {
                throw new IOException(result.getErrorMessage());
            } else {
                handler.handle(AsyncResultImpl.create((Void) null));
            }
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(
                    new PublishingException(Messages.i18n.format("ESRegistry.ErrorPublishingApi"), e),  //$NON-NLS-1$
                    Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#retireApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireApi(Api api, final IAsyncResultHandler<Void> handler) {
        final String id = getApiId(api);

        try {
            Delete delete = new Delete.Builder(id).index(getIndexName()).type("api").build(); //$NON-NLS-1$
            JestResult result = getClient().execute(delete);
            if (result.isSucceeded()) {
                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                handler.handle(AsyncResultImpl.create(new ApiNotFoundException(Messages.i18n.format("ESRegistry.ApiNotFound")))); //$NON-NLS-1$
            }
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorRetiringApi"), e))); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerClient(final Client client, final IAsyncResultHandler<Void> handler) {
        try {
            // Validate the client and populate the api map with apis found during validation.
            validateClient(client);

            String id = getClientId(client);
            Index index = new Index.Builder(client)
                    .refresh(false).index(getIndexName())
                    .setParameter(Parameters.OP_TYPE, "index") //$NON-NLS-1$
                    .type("client").id(id).build(); //$NON-NLS-1$
            JestResult result = getClient().execute(index);
            if (!result.isSucceeded()) {
                throw new IOException(result.getErrorMessage());
            }
            handler.handle(AsyncResultImpl.create((Void) null));
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(
                    new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringClient"), e),  //$NON-NLS-1$
                    Void.class));
        } catch (RuntimeException re) {
            handler.handle(AsyncResultImpl.create(re, Void.class));
        }
    }

    /**
     * Validate that the client should be registered.
     * @param client
     */
    private void validateClient(Client client) throws RegistrationException {
        Set<Contract> contracts = client.getContracts();
        if (contracts.isEmpty()) {
            throw new NoContractFoundException(Messages.i18n.format("ESRegistry.NoContracts")); //$NON-NLS-1$
        }
        for (Contract contract : contracts) {
            validateContract(contract);
        }
    }

    /**
     * Ensures that the api referenced by the Contract at the head of
     * the iterator actually exists (is published).
     * @param contract
     * @param apiMap
     */
    private void validateContract(final Contract contract)
            throws RegistrationException {
        final String id = getApiId(contract);

        try {
            Get get = new Get.Builder(getIndexName(), id).type("api").build(); //$NON-NLS-1$
            JestResult result = getClient().execute(get);
            if (!result.isSucceeded()) {
                String apiId = contract.getApiId();
                String orgId = contract.getApiOrgId();
                throw new ApiNotFoundException(Messages.i18n.format("ESRegistry.ApiNotFoundInOrg", apiId, orgId));  //$NON-NLS-1$
            }
        } catch (IOException e) {
            throw new RegistrationException(Messages.i18n.format("ESRegistry.ErrorValidatingClient"), e); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterClient(final Client client, final IAsyncResultHandler<Void> handler) {
        try {
            final Client lclient = lookupClient(client.getOrganizationId(), client.getClientId(), client.getVersion());
            final String id = getClientId(lclient);

            Delete delete = new Delete.Builder(id).index(getIndexName()).type("client").build(); //$NON-NLS-1$
            JestResult result = getClient().execute(delete);
            if (result.isSucceeded()) {
                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                handler.handle(AsyncResultImpl.create(new ClientNotFoundException(Messages.i18n.format("ESRegistry.ClientNotFound")), Void.class)); //$NON-NLS-1$
            }
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorUnregisteringClient"), e), Void.class)); //$NON-NLS-1$
        } catch (RuntimeException e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }

    /**
     * Searches for a client by its orgid:clientId:version and returns it.
     * @param orgId
     * @param clientId
     * @param version
     */
    @SuppressWarnings("nls")
    private Client lookupClient(String orgId, String clientId, String version) {
        String query = "{" +
                "  \"query\": {" +
                "    \"filtered\": { " +
                "      \"filter\": {" +
                "        \"and\" : [" +
                "          {" +
                "            \"term\": { \"organizationId\": \"" + orgId + "\" }" +
                "          }," +
                "          {" +
                "            \"term\": { \"clientId\": \"" + clientId + "\" }" +
                "          }," +
                "          {" +
                "            \"term\": { \"version\": \"" + version + "\" }" +
                "          }" +
                "        ]" +
                "      }" +
                "    }" +
                "  }" +
                "}";
        try {
            Search search = new Search.Builder(query).addIndex(getIndexName())
                    .addType("client").build();
            SearchResult response = getClient().execute(search);
            if (response.getTotal() < 1) {
                throw new IOException();
            }
            Hit<Client,Void> hit = response.getFirstHit(Client.class);
            return hit.source;
        } catch (IOException e) {
            throw new ClientNotFoundException(Messages.i18n.format("ESRegistry.ClientNotFound"), e);  //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getApi(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getApi(String organizationId, String apiId, String apiVersion,
            IAsyncResultHandler<Api> handler) {
        String id = getApiId(organizationId, apiId, apiVersion);
        try {
            Api api = getApi(id);
            handler.handle(AsyncResultImpl.create(api));
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(e, Api.class));
        }
    }

    /**
     * Gets the api synchronously.
     * @param id
     * @throws IOException
     */
    protected Api getApi(String id) throws IOException {
        Get get = new Get.Builder(getIndexName(), id).type("api").build(); //$NON-NLS-1$
        JestResult result = getClient().execute(get);
        if (result.isSucceeded()) {
            Api api = result.getSourceAsObject(Api.class);
            return api;
        } else {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getClient(java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getClient(String apiKey, IAsyncResultHandler<Client> handler) {
        String id = apiKey;
        try {
            Client client = getClient(id);
            handler.handle(AsyncResultImpl.create(client));
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(e, Client.class));
        }
    }

    /**
     * Gets the client synchronously.
     * @param id
     * @throws IOException
     */
    protected Client getClient(String id) throws IOException {
        Get get = new Get.Builder(getIndexName(), id).type("client").build(); //$NON-NLS-1$
        JestResult result = getClient().execute(get);
        if (result.isSucceeded()) {
            Client client = result.getSourceAsObject(Client.class);
            return client;
        } else {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(String apiOrganizationId, String apiId, String apiVersion, String apiKey,
            IAsyncResultHandler<ApiContract> handler) {

        try {
            Client client = getClient(apiKey);
            Api api = getApi(getApiId(apiOrganizationId, apiId, apiVersion));

            if (client == null) {
                Exception error = new ClientNotFoundException(Messages.i18n.format("ESRegistry.NoClientForAPIKey", apiKey)); //$NON-NLS-1$
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                return;
            }
            if (api == null) {
                Exception error = new ApiRetiredException(Messages.i18n.format("ESRegistry.ApiWasRetired", //$NON-NLS-1$
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
                Exception error = new NoContractFoundException(Messages.i18n.format("ESRegistry.NoContractFound", //$NON-NLS-1$
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
     * Generates a valid document ID for a api, used to index the api in ES.
     * @param api an api
     * @return a api key
     */
    private String getApiId(Api api) {
        return getApiId(api.getOrganizationId(), api.getApiId(), api.getVersion());
    }

    /**
     * Generates a valid document ID for a api referenced by a contract, used to
     * retrieve the api from ES.
     * @param contract
     */
    private String getApiId(Contract contract) {
        return getApiId(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
    }

    /**
     * Generates a valid document ID for a api, used to index the api in ES.
     * @param orgId
     * @param apiId
     * @param version
     * @return a api id
     */
    protected String getApiId(String orgId, String apiId, String version) {
        return orgId + ":" + apiId + ":" + version; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates a valid document ID for the client - used to index the client in ES.
     * @param client
     * @return an id
     */
    protected String getClientId(Client client) {
        return client.getApiKey();
    }

    /**
     * @see io.apiman.gateway.engine.es.AbstractESComponent#getDefaultIndexName()
     */
    @Override
    protected String getDefaultIndexName() {
        return ESConstants.GATEWAY_INDEX_NAME;
    }

}
