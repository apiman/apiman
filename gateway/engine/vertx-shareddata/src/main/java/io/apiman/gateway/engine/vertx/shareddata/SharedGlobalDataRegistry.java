/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.engine.vertx.shareddata;

import io.apiman.gateway.engine.IEngineConfig;
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
import io.apiman.gateway.engine.i18n.Messages;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
* @author Marc Savy {@literal <marc@rhymewithgravy.com>}
*/
@SuppressWarnings("nls")
public class SharedGlobalDataRegistry implements IRegistry {
    Vertx vertx;
    IEngineConfig vxConfig;
    Map<String, String> options;
    AsyncMap<String, Object> objectMap;

    public SharedGlobalDataRegistry(Vertx vertx, IEngineConfig vxConfig, Map<String, String> options) {
        this.vertx = vertx;
        this.vxConfig = vxConfig;
        this.options = options;

        if (!vertx.isClustered()) {
            throw new IllegalStateException(SharedGlobalDataRegistry.class.getCanonicalName() + " only works when operating in clustered mode!");
        }

        vertx.sharedData().<String, Object> getClusterWideMap("SharedGlobalDataRegistry-Shared",  async -> {
            if (async.succeeded()) {
                objectMap = async.result();
            } else {
                throw new IllegalStateException(async.cause());
            }
        });
    }

    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        objectMap.put(getApiIndex(api), api, handleResultVoid(handler));
    }

    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        objectMap.remove(getApiIndex(api), handleSuccessfulResult(handler, deletedApi -> {
            if (deletedApi == null) {
                Exception ex = new PublishingException(Messages.i18n.format("InMemoryRegistry.ApiNotFound"));
                handler.handle(AsyncResultImpl.create(ex));
            } else {
                handler.handle(AsyncResultImpl.create((Void) null));
            }
        }));
    }

    private <T> Handler<AsyncResult<T>> handleSuccessfulResult(IAsyncResultHandler<Void> failureHandler, Handler<T> successHandler) {
        return result -> {
            if (result.succeeded()) {
                successHandler.handle(result.result());
            } else {
                failureHandler.handle(AsyncResultImpl.create(result.cause()));
            }
        };
    }

    @SuppressWarnings("rawtypes") // CompositeFuture.all(list) requires raw futures.
    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> resultHandler) {
        List<Future> futures = new ArrayList<>(client.getContracts().size());
        List<Contract> contracts = new ArrayList<>(client.getContracts());
        String clientIndex = getClientIndex(client);

        // Future for each contract and execute get.
        for (Contract contract : contracts) {
            Future future = Future.future();
            futures.add(future);
            String apiIndex = getApiIndex(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
            objectMap.get(apiIndex, future.completer());
        }

        CompositeFuture.all(futures).setHandler(compositeResult -> {
            if (compositeResult.succeeded()) {
                // If any contract didn't correspond to a stored API.
                Contract failedContract = null;
                for (int i=0; i<futures.size(); i++) {
                    if (futures.get(i).result() == null) {
                        failedContract = contracts.get(0);
                        break;
                    }
                }
                // If we found an invalid contract.
                if (failedContract != null) {
                    Exception ex = new RegistrationException(Messages.i18n.format("InMemoryRegistry.ApiNotFoundInOrg",
                            failedContract.getApiId(), failedContract.getApiOrgId()));
                    resultHandler.handle(AsyncResultImpl.create(ex));
                } else {
                    Future<Object> putNewApiKeyFuture = Future.future();
                    Future<Object> endFuture = Future.future();

                    // Order: Create new API Key reference; Replace old ID -> API mapping; Delete old key reference)
                    // This should ensure no breaking/irreconcilable behaviour.
                    objectMap.putIfAbsent(client.getApiKey(), client, putNewApiKeyFuture.completer());

                    // Replace API Key reference
                    putNewApiKeyFuture.compose(clientWithSameApiKey -> {
                        Future<Object> replaceClientFuture = Future.future();
                        // There's a small chance the same key will replace the old one, usually
                        // only in hard-coded tests. Generally sameKeyReplace will be null.
                        if (clientWithSameApiKey != null) {
                            //System.err.println("!!!!! Same API Key -- Replacing. Must not delete later. !!!!!!");
                            objectMap.replace(client.getApiKey(), client, replaceClientFuture.completer());
                        } else {
                            objectMap.putIfAbsent(clientIndex, client, replaceClientFuture.completer());
                        }
                        return replaceClientFuture;
                    // Remove old API key reference
                    }).compose(oldClientRaw -> {
                        Client oldClient = (Client) oldClientRaw;
                        if (oldClientRaw != null && !oldClient.getApiKey().equals(client.getApiKey())) {
                            objectMap.remove(oldClient.getApiKey(), endFuture.completer());
                        } else {
                            endFuture.complete();
                        }
                    }, endFuture)
                    // When finished, call this handler and then resultHandler
                    .setHandler(handleResult(resultHandler));
                }
            } else {
                resultHandler.handle(AsyncResultImpl.create(compositeResult.cause()));
            }
        });
    }

    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> resultHandler) {
        String clientIndex = getClientIndex(client);
        objectMap.get(clientIndex, handleSuccessfulResult(resultHandler, oldClientRaw -> {
            Client oldClient = (Client) oldClientRaw;
            if (oldClient == null) {
                Exception ex = new RegistrationException(Messages.i18n.format("InMemoryRegistry.ClientNotFound"));
                resultHandler.handle(AsyncResultImpl.create(ex));
            } else {
                Future<Object> future1 = Future.future();
                Future<Object> future2 = Future.future();

                objectMap.remove(clientIndex, future1.completer());
                objectMap.remove(oldClient.getApiKey(), future2.completer());

                CompositeFuture.all(future1, future2).setHandler(handleCompositeResult(resultHandler));
            }
        }));
    }

    @Override
    public void getApi(String organizationId, String apiId, String apiVersion, IAsyncResultHandler<Api> handler) {
        objectMap.get(getApiIndex(organizationId, apiId, apiVersion), handleResult(handler));
    }

    @Override
    public void getClient(String apiKey, IAsyncResultHandler<Client> handler) {
        objectMap.get(apiKey, handleResult(handler));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void getContract(String apiOrganizationId, String apiId, String apiVersion, String apiKey, IAsyncResultHandler<ApiContract> handler) {
        String apiIndex = getApiIndex(apiOrganizationId, apiId, apiVersion);

        Future apiFuture = Future.future();
        Future clientFuture = Future.future();

        objectMap.get(apiIndex, apiFuture.completer());
        objectMap.get(apiKey, clientFuture.completer());

        CompositeFuture.all(apiFuture, clientFuture).setHandler(compositeResult -> {
            if (compositeResult.succeeded()) {
                Api api = (Api) apiFuture.result();
                Client client = (Client) clientFuture.result();

                if (api == null) {
                    Exception error = new InvalidContractException(Messages.i18n.format("InMemoryRegistry.NoClientForAPIKey", apiKey));
                    handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                } else if (client == null) {
                    Exception error = new InvalidContractException(Messages.i18n.format("InMemoryRegistry.ApiWasRetired",
                            apiId, apiOrganizationId));
                    handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                } else {
                    Optional<Contract> matchedOpt = client.getContracts().stream()
                            .filter(contract -> contract.matches(apiOrganizationId, apiId, apiVersion))
                            .findFirst();

                    if (matchedOpt.isPresent()) {
                        Contract contract = matchedOpt.get();
                        ApiContract apiContract = new ApiContract(api, client, contract.getPlan(), contract.getPolicies());
                        handler.handle(AsyncResultImpl.create(apiContract));
                    } else {
                        Exception error = new InvalidContractException(Messages.i18n.format("InMemoryRegistry.NoContractFound", //$NON-NLS-1$
                                client.getClientId(), api.getApiId()));
                        handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                    }
                }
            } else {
                handler.handle(AsyncResultImpl.create(compositeResult.cause()));
            }
        });
    }

    private String getApiIndex(Api api) {
        return getApiIndex(api.getOrganizationId(), api.getApiId(), api.getVersion());
    }

    private String getApiIndex(String orgId, String apiId, String version) {
        return "API::" + orgId + "|" + apiId + "|" + version; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private String getClientIndex(Client client) {
        return "CLIENT::" + client.getOrganizationId() + "|" + client.getClientId() + "|" + client.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @SuppressWarnings("unchecked")
    private <T> Handler<AsyncResult<CompositeFuture>> handleCompositeResult(IAsyncResultHandler<T> apimanResultHandler) {
        return result -> {
            if (result.succeeded()) {
                apimanResultHandler.handle(AsyncResultImpl.create((T) result.result()));
            } else {
                apimanResultHandler.handle(AsyncResultImpl.create(result.cause()));
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> Handler<AsyncResult<Object>> handleResult(IAsyncResultHandler<T> apimanResultHandler) {
        return result -> {
            if (result.succeeded()) {
                apimanResultHandler.handle(AsyncResultImpl.create((T) result.result()));
            } else {
                apimanResultHandler.handle(AsyncResultImpl.create(result.cause()));
            }
        };
    }

    private Handler<AsyncResult<Void>> handleResultVoid(IAsyncResultHandler<Void> apimanResultHandler) {
        return result -> {
            if (result.succeeded()) {
                apimanResultHandler.handle(AsyncResultImpl.create(result.result()));
            } else {
                apimanResultHandler.handle(AsyncResultImpl.create(result.cause()));
            }
        };
    }
}
