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
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.es.i18n.Messages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.engine.DocumentAlreadyExistsException;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * An implementation of the Registry that uses elasticsearch as a storage
 * mechanism.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESRegistry implements IRegistry {

    private Map<String, String> config;
    private Client esClient;

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public ESRegistry(Map<String, String> config) {
        this.config = config;
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#publishService(io.apiman.gateway.engine.beans.Service, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishService(final Service service, final IAsyncResultHandler<Void> handler) {
        try {
            String id = getServiceId(service);
            getClient().prepareIndex(ESConstants.INDEX_NAME, "service", id) //$NON-NLS-1$
                .setSource(ESRegistryMarshalling.marshall(service))
                .setCreate(true)
                .setContentType(XContentType.JSON)
                .execute(new ActionListener<IndexResponse>() {
                    @Override
                    public void onResponse(IndexResponse response) {
                        if (!response.isCreated()) {
                            handler.handle(AsyncResultImpl.create(
                                    new PublishingException(Messages.i18n.format("ESRegistry.ServiceAlreadyPublished")),  //$NON-NLS-1$
                                    Void.class));
                        } else {
                            handler.handle(AsyncResultImpl.create((Void) null));
                        }
                    }
                    @Override
                    public void onFailure(Throwable e) {
                        if (ESUtils.rootCause(e) instanceof DocumentAlreadyExistsException) {
                            handler.handle(AsyncResultImpl.create(
                                    new PublishingException(Messages.i18n.format("ESRegistry.ServiceAlreadyPublished")),  //$NON-NLS-1$
                                    Void.class));
                        } else {
                            handler.handle(AsyncResultImpl.create(
                                    new PublishingException(Messages.i18n.format("ESRegistry.ErrorPublishingService"), e),  //$NON-NLS-1$
                                    Void.class));
                        }
                    }
                });
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(
                    new PublishingException(Messages.i18n.format("ESRegistry.ErrorPublishingService"), e),  //$NON-NLS-1$
                    Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#retireService(io.apiman.gateway.engine.beans.Service, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireService(Service service, final IAsyncResultHandler<Void> handler) {
        final String id = getServiceId(service);
        getClient().prepareDelete(ESConstants.INDEX_NAME, "service", id) //$NON-NLS-1$
            .execute(new ActionListener<DeleteResponse>() {
                @Override
                public void onResponse(DeleteResponse response) {
                    if (response.isFound()) {
                        handler.handle(AsyncResultImpl.create((Void) null));
                    } else {
                        handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ServiceNotFound")), Void.class)); //$NON-NLS-1$
                    }
                }
                @Override
                public void onFailure(Throwable e) {
                    handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorRetiringService"), e), Void.class)); //$NON-NLS-1$
                }
            });
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerApplication(final Application application, final IAsyncResultHandler<Void> handler) {
        final Map<String, Service> svcMap = new HashMap<>();
        validateApplication(application, svcMap, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isError()) {
                    handler.handle(result);
                } else {
                    String id = getApplicationId(application);
                    try {
                        getClient().prepareIndex(ESConstants.INDEX_NAME, "application", id) //$NON-NLS-1$
                            .setSource(ESRegistryMarshalling.marshall(application))
                            .setCreate(true)
                            .setContentType(XContentType.JSON)
                            .execute(new ActionListener<IndexResponse>() {
                                @Override
                                public void onResponse(IndexResponse response) {
                                    if (!response.isCreated()) {
                                        handler.handle(AsyncResultImpl.create(
                                                new RegistrationException(Messages.i18n.format("ESRegistry.AppAlreadyRegistered")),  //$NON-NLS-1$
                                                Void.class));
                                    } else {
                                        Iterator<Contract> iterator = application.getContracts().iterator();
                                        application.setContracts(null);
                                        registerContracts(application, iterator, svcMap, handler);
                                    }
                                }

                                @Override
                                public void onFailure(Throwable e) {
                                    if (ESUtils.rootCause(e) instanceof DocumentAlreadyExistsException) {
                                        handler.handle(AsyncResultImpl.create(
                                                new RegistrationException(Messages.i18n.format("ESRegistry.AppAlreadyRegistered")),  //$NON-NLS-1$
                                                Void.class));
                                    } else {
                                        handler.handle(AsyncResultImpl.create(
                                                new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringApplication"), e),  //$NON-NLS-1$
                                                Void.class));
                                    }
                                }
                            });
                    } catch (Exception e) {
                        handler.handle(AsyncResultImpl.create(
                                new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringApplication"), e),  //$NON-NLS-1$
                                Void.class));
                    }
                }
            }
        });
    }

    /**
     * Validate that the application should be registered.
     * @param application
     * @param serviceMap
     * @param iAsyncResultHandler
     */
    private void validateApplication(Application application, Map<String, Service> serviceMap, IAsyncResultHandler<Void> handler) {
        Set<Contract> contracts = application.getContracts();
        if (contracts.isEmpty()) {
            handler.handle(AsyncResultImpl.create(
                    new RegistrationException(Messages.i18n.format("ESRegistry.NoContracts")), Void.class)); //$NON-NLS-1$
            return;
        }
        final Iterator<Contract> iterator = contracts.iterator();
        validateServiceExists(iterator, serviceMap, handler);
    }

    /**
     * Ensures that the service referenced by the Contract at the head of
     * the iterator actually exists (is published).
     * @param iterator
     * @param serviceMap
     * @param handler
     */
    private void validateServiceExists(final Iterator<Contract> iterator, final Map<String, Service> serviceMap,
            final IAsyncResultHandler<Void> handler) {
        if (!iterator.hasNext()) {
            handler.handle(AsyncResultImpl.create((Void) null));
        } else {
            final Contract contract = iterator.next();
            final String serviceId = getServiceId(contract);
            getService(serviceId, new IAsyncResultHandler<Service>() {
                @Override
                public void handle(IAsyncResult<Service> result) {
                    if (result.isError()) {
                        handler.handle(AsyncResultImpl.create(
                                new RegistrationException(
                                        Messages.i18n.format("ESRegistry.ErrorValidatingApp"),  //$NON-NLS-1$
                                        result.getError()),
                                Void.class));
                    } else {
                        Service service = result.getResult();
                        if (service == null) {
                            String serviceId = contract.getServiceId();
                            String orgId = contract.getServiceOrgId();
                            handler.handle(AsyncResultImpl.create(
                                    new RegistrationException(Messages.i18n.format("ESRegistry.ServiceNotFoundInOrg", serviceId, orgId)),  //$NON-NLS-1$
                                    Void.class));
                        } else {
                            service.setServicePolicies(null);
                            serviceMap.put(serviceId, service);
                            validateServiceExists(iterator, serviceMap, handler);
                        }
                    }
                }
            });
        }
    }

    /**
     * Register all the contracts in ES so they can be looked up quickly by
     * their ID by all nodes in the cluster.
     * @param application
     * @param contracts
     * @param serviceMap
     * @param handler
     */
    private void registerContracts(final Application application, final Iterator<Contract> contracts,
            final Map<String, Service> serviceMap, final IAsyncResultHandler<Void> handler) {
        try {
            if (!contracts.hasNext()) {
                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                Contract contract = contracts.next();

                String svcId = getServiceId(contract);
                Service service = serviceMap.get(svcId);
                ServiceContract sc = new ServiceContract(contract.getApiKey(), service, application,
                        contract.getPlan(), contract.getPolicies());
                final String contractId = getContractId(contract);

                XContentBuilder source = ESRegistryMarshalling.marshall(sc);
                getClient().prepareIndex(ESConstants.INDEX_NAME, "serviceContract", contractId) //$NON-NLS-1$
                    .setSource(source)
                    .setCreate(true)
                    .setContentType(XContentType.JSON)
                    .execute(new ActionListener<IndexResponse>() {
                        @Override
                        public void onResponse(IndexResponse response) {
                            if (!response.isCreated()) {
                                handler.handle(AsyncResultImpl.create(
                                        new RegistrationException(Messages.i18n.format("ESRegistry.ContractAlreadyPublished", contractId)),  //$NON-NLS-1$
                                        Void.class));
                            } else {
                                registerContracts(application, contracts, serviceMap, handler);
                            }
                        }
                        @Override
                        public void onFailure(Throwable e) {
                            if (ESUtils.rootCause(e) instanceof DocumentAlreadyExistsException) {
                                handler.handle(AsyncResultImpl.create(
                                        new RegistrationException(Messages.i18n.format("ESRegistry.ContractAlreadyPublished", contractId)),  //$NON-NLS-1$
                                        Void.class));
                            } else {
                                handler.handle(AsyncResultImpl.create(
                                        new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringContract"), e),  //$NON-NLS-1$
                                        Void.class));
                            }
                        }
                    });

            }
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(
                    new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringContract"), e),  //$NON-NLS-1$
                    Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterApplication(final Application application, final IAsyncResultHandler<Void> handler) {
        final String id = getApplicationId(application);
        getClient().prepareDelete(ESConstants.INDEX_NAME, "application", id) //$NON-NLS-1$
            .execute(new ActionListener<DeleteResponse>() {
                @Override
                public void onResponse(DeleteResponse response) {
                    if (response.isFound()) {
                        unregisterServiceContracts(application, handler);
                    } else {
                        handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.AppNotFound")), Void.class)); //$NON-NLS-1$
                    }
                }
                @Override
                public void onFailure(Throwable e) {
                    handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorUnregisteringApp"), e), Void.class)); //$NON-NLS-1$
                }
            });
    }

    /**
     * Removes all of the service contracts from ES.
     * @param application
     * @param handler
     */
    protected void unregisterServiceContracts(Application application, final IAsyncResultHandler<Void> handler) {
        QueryBuilder qb = QueryBuilders.filteredQuery(
                QueryBuilders.matchAllQuery(),
                FilterBuilders.andFilter(
                        FilterBuilders.termFilter("application.organizationId", application.getOrganizationId()), //$NON-NLS-1$
                        FilterBuilders.termFilter("application.applicationId", application.getApplicationId()), //$NON-NLS-1$
                        FilterBuilders.termFilter("application.version", application.getVersion()) //$NON-NLS-1$
                )
            );
        getClient().prepareDeleteByQuery(ESConstants.INDEX_NAME)
            .setQuery(qb)
            .setTypes("serviceContract") //$NON-NLS-1$
            .execute(new ActionListener<DeleteByQueryResponse>() {
                @Override
                public void onResponse(DeleteByQueryResponse response) {
                    handler.handle(AsyncResultImpl.create((Void) null));
                }
                @Override
                public void onFailure(Throwable e) {
                    handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorUnregisteringApp"), e), Void.class)); //$NON-NLS-1$
                }
            });
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(final ServiceRequest request, final IAsyncResultHandler<ServiceContract> handler) {
        final String id = getContractId(request);
        getClient().prepareGet(ESConstants.INDEX_NAME, "serviceContract", id) //$NON-NLS-1$
            .setFetchSource(true)
            .execute(new ActionListener<GetResponse>() {
                @Override
                public void onResponse(GetResponse response) {
                    if (!response.isExists()) {
                        Exception error = new InvalidContractException(Messages.i18n.format("ESRegistry.NoContractForAPIKey", id)); //$NON-NLS-1$
                        handler.handle(AsyncResultImpl.create(error, ServiceContract.class));
                    } else {
                        Map<String, Object> source = response.getSource();
                        ServiceContract contract = ESRegistryMarshalling.unmarshallServiceContract(source);
                        checkService(contract, handler);
                    }
                }
                @Override
                public void onFailure(Throwable e) {
                    if (ESUtils.rootCause(e) instanceof DocumentAlreadyExistsException) {
                        Exception error = new InvalidContractException(Messages.i18n.format("ESRegistry.NoContractForAPIKey", id)); //$NON-NLS-1$
                        handler.handle(AsyncResultImpl.create(error, ServiceContract.class));
                    } else {
                        handler.handle(AsyncResultImpl.create(e, ServiceContract.class));
                    }
                }
            });
    }

    /**
     * Ensure that the service still exists.  If not, it was retired.
     * @param contract
     * @param handler
     */
    protected void checkService(final ServiceContract contract, final IAsyncResultHandler<ServiceContract> handler) {
        final Service service = contract.getService();
        String id = getServiceId(service);
        getClient().prepareGet(ESConstants.INDEX_NAME, "service", id) //$NON-NLS-1$
            .setFetchSource(false)
            .execute(new ActionListener<GetResponse>() {
                @Override
                public void onResponse(GetResponse response) {
                    if (response.isExists()) {
                        handler.handle(AsyncResultImpl.create(contract));
                    } else {
                        Exception error = new InvalidContractException(Messages.i18n.format("ESRegistry.ServiceWasRetired", //$NON-NLS-1$
                                service.getServiceId(), service.getOrganizationId()));
                        handler.handle(AsyncResultImpl.create(error, ServiceContract.class));
                    }
                }
                @Override
                public void onFailure(Throwable e) {
                    if (ESUtils.rootCause(e) instanceof DocumentAlreadyExistsException) {
                        Exception error = new InvalidContractException(Messages.i18n.format("ESRegistry.ServiceWasRetired", //$NON-NLS-1$
                                service.getServiceId(), service.getOrganizationId()));
                        handler.handle(AsyncResultImpl.create(error, ServiceContract.class));
                    } else {
                        handler.handle(AsyncResultImpl.create(e, ServiceContract.class));
                    }
                }
            });
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getService(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getService(String organizationId, String serviceId, String serviceVersion,
            IAsyncResultHandler<Service> handler) {
        String id = getServiceId(organizationId, serviceId, serviceVersion);
        getService(id, handler);
    }

    /**
     * Asynchronously gets a service.
     * @param id
     * @param handler
     */
    protected void getService(String id, final IAsyncResultHandler<Service> handler) {
        getClient().prepareGet(ESConstants.INDEX_NAME, "service", id) //$NON-NLS-1$
            .execute(new ActionListener<GetResponse>() {
                @Override
                public void onResponse(GetResponse response) {
                    if (response.isExists()) {
                        Map<String, Object> source = response.getSourceAsMap();
                        Service service = ESRegistryMarshalling.unmarshallService(source);
                        handler.handle(AsyncResultImpl.create(service));
                    } else {
                        handler.handle(AsyncResultImpl.create((Service) null));
                    }
                }
                @Override
                public void onFailure(Throwable e) {
                    if (ESUtils.rootCause(e) instanceof DocumentAlreadyExistsException) {
                        handler.handle(AsyncResultImpl.create((Service) null));
                    } else {
                        handler.handle(AsyncResultImpl.create(e, Service.class));
                    }
                }
            });
    }

    /**
     * Generates a valid document ID for a service, used to index the service in ES.
     * @param service an service
     * @return a service key
     */
    private String getServiceId(Service service) {
        return getServiceId(service.getOrganizationId(), service.getServiceId(), service.getVersion());
    }

    /**
     * Generates a valid document ID for a service referenced by a contract, used to
     * retrieve the service from ES.
     * @param contract
     */
    private String getServiceId(Contract contract) {
        return getServiceId(contract.getServiceOrgId(), contract.getServiceId(), contract.getServiceVersion());
    }

    /**
     * Generates a valid document ID for a service, used to index the service in ES.
     * @param orgId
     * @param serviceId
     * @param version
     * @return a service key
     */
    private String getServiceId(String orgId, String serviceId, String version) {
        return orgId + ":" + serviceId + ":" + version; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates a valid document ID for an application, used to index the app in ES.
     * @param app an application
     * @return an application key
     */
    private String getApplicationId(Application app) {
        return app.getOrganizationId() + ":" + app.getApplicationId() + ":" + app.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates a valid document ID for a contract, used to index the contract in ES.
     * @param request
     */
    private String getContractId(ServiceRequest request) {
        return request.getApiKey();
    }

    /**
     * Generates a valid document ID for a contract, used to index the contract in ES.
     * @param contract
     */
    private String getContractId(Contract contract) {
        return contract.getApiKey();
    }

    /**
     * @return the esClient
     */
    public synchronized Client getClient() {
        if (esClient == null) {
            esClient = ESClientFactory.createClient(config);
        }
        return esClient;
    }

}
