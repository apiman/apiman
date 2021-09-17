/*
 * Copyright 2020 Scheer PAS Schweiz AG
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

package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.developers.DeveloperBean;
import io.apiman.manager.api.beans.developers.DeveloperMappingBean;
import io.apiman.manager.api.beans.developers.UpdateDeveloperBean;
import io.apiman.manager.api.beans.summary.ClientVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.rest.IDeveloperResource;
import io.apiman.manager.api.rest.exceptions.DeveloperAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.DeveloperNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.service.ApiService;
import io.apiman.manager.api.service.ApiService.ApiDefinitionStream;
import io.apiman.manager.api.service.ClientAppService;
import io.apiman.manager.api.service.ContractService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import com.google.common.collect.ImmutableList;

/**
 * Implementation of the Developer Portal API
 */
@Transactional
@ApplicationScoped
@Deprecated(forRemoval = true)
public class DeveloperResourceImpl implements IDeveloperResource, DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(DeveloperResourceImpl.class);

    private IStorage storage;
    private IStorageQuery query;
    private ISecurityContext securityContext;
    private ApiService apiService;

    /**
     * Constructor
     */
    @Inject
    public DeveloperResourceImpl(
        IStorage storage,
        IStorageQuery query,
        ISecurityContext securityContext,
        ContractService contractService,
        ApiService apiService,
        ClientAppService clientService
    ) {
        this.storage = storage;
        this.query = query;
        this.securityContext = securityContext;
        this.apiService = apiService;
    }

    public DeveloperResourceImpl() {
    }

    @Override
    public List<ApiVersionBean> getAllPublicApiVersions() throws NotAuthorizedException {
        Iterator<ApiVersionBean> iterator = tryAction(storage::getAllPublicApiVersions);
        return ImmutableList.copyOf(iterator);
    }

    @Override
    public List<DeveloperBean> getDevelopers() throws NotAuthorizedException {
        securityContext.checkAdminPermissions();
        Iterator<DeveloperBean> iterator = tryAction(storage::getDevelopers);
        return ImmutableList.copyOf(iterator);
    }

    @Override
    public DeveloperBean create(DeveloperBean bean) throws InvalidNameException, NotAuthorizedException, DeveloperAlreadyExistsException {
        securityContext.checkAdminPermissions();

        DeveloperBean developerBean = new DeveloperBean();
        developerBean.setId(bean.getId());
        developerBean.setClients(bean.getClients());

        return tryAction(() -> {
            if (storage.getDeveloper(bean.getId()) != null) {
                throw ExceptionFactory.developerAlreadyExistsException(bean.getId());
            }
            storage.createDeveloper(developerBean);
            LOGGER.debug(String.format("Created developer %s: %s", developerBean.getId(), developerBean)); //$NON-NLS-1$
            return developerBean;
        });
    }

    @Override
    public void update(String id, UpdateDeveloperBean bean) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();
        tryAction(() -> {
            DeveloperBean developerBean = getDeveloperBeanFromStorage(id);
            developerBean.setClients(bean.getClients());
            storage.updateDeveloper(developerBean);
            LOGGER.debug(String.format("Updated developer %s: %s", developerBean.getId(), developerBean));
        });
    }

    @Override
    public DeveloperBean get(String id) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();
        return getDeveloperBeanFromStorage(id);
    }

    @Override
    public void delete(String id) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        DeveloperBean developerBean = getDeveloperBeanFromStorage(id);
        tryAction(() -> storage.deleteDeveloper(developerBean));
        LOGGER.debug("Deleted developer: {0}", developerBean.getId()); //$NON-NLS-1$
    }

    @Override
    public List<ClientVersionSummaryBean> getAllClientVersions(String id) throws DeveloperNotFoundException, NotAuthorizedException {
       securityContext.checkIfUserIsCurrentUser(id);
        DeveloperBean developer = getDeveloperBeanFromStorage(id);
        return queryMatchingClientVersions(developer);
    }

    @Override
    public List<ContractSummaryBean> getAllClientContracts(String id) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(id);

        DeveloperBean developer;
        List<ClientVersionSummaryBean> clientVersionSummaryBeans;
        List<ContractSummaryBean> contractSummaryBeans = new ArrayList<>();

        developer = getDeveloperBeanFromStorage(id);

        clientVersionSummaryBeans = queryMatchingClientVersions(developer);
        for (ClientVersionSummaryBean bean : clientVersionSummaryBeans) {
            List<ContractSummaryBean> allClientContracts =
                tryAction(() -> query.getClientContracts(bean.getOrganizationId(), bean.getId(), bean.getVersion()));
            contractSummaryBeans.addAll(allClientContracts);
        }

        return contractSummaryBeans;
    }

    @Override
    public List<ApiVersionBean> getAllApiVersions(String id) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(id);

        List<ApiVersionBean> apiVersionBeans = new ArrayList<>();
        List<ContractSummaryBean> contracts = getAllClientContracts(id);
        for (ContractSummaryBean contract : contracts) {
            ApiVersionBean apiVersion = apiService.getApiVersion(contract.getApiOrganizationId(), contract.getApiId(), contract.getApiVersion());
            apiVersionBeans.add(apiVersion);
        }
        return apiVersionBeans;
    }

    @Override
    public Response getPublicApiDefinition(String organizationId, String apiId, String version) {
        ApiVersionBean apiVersion = apiService.getApiVersion(organizationId, apiId, version);
        if (apiVersion.isPublicAPI()) {
            ApiDefinitionStream apiDef = apiService.getApiDefinition(organizationId, apiId, version);
            return Response.ok()
                .entity(apiDef.getDefinition())
                .type(apiDef.getDefinitionType().getMediaType())
                .build();
        } else {
            throw ExceptionFactory.notAuthorizedException();
        }
    }

    @Override
    public Response getApiDefinition(String developerId, String organizationId, String apiId, String version) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(developerId);

        Set<DeveloperMappingBean> developerClients = getDeveloperBeanFromStorage(developerId).getClients();

        return (Response) tryAction(() -> {
            // get all contracts from the API Version
            List<ContractSummaryBean> contracts = query.getContracts(organizationId, apiId, version, 1, 10000);

            for (ContractSummaryBean contract : contracts) {
                for (DeveloperMappingBean client : developerClients) {
                    // check if the developer is allowed to request the definition
                    if (client.getClientId().equals(contract.getClientId()) && client.getOrganizationId().equals(contract.getClientOrganizationId())) {
                        ApiDefinitionStream apiDef = apiService.getApiDefinition(organizationId, apiId, version);
                        return Response.ok()
                            .entity(apiDef.getDefinition())
                            .type(apiDef.getDefinitionType().getMediaType())
                            .build();
                    }
                }
            }

            return Response.noContent();
        });
    }

    /**
     * Gets the developer from storage
     * A transaction must be present
     *
     * @param id the id of the developer
     * @return the developer
     * @throws DeveloperNotFoundException when trying to get, update, or delete an organization that does not exist
     */
    private DeveloperBean getDeveloperBeanFromStorage(String id) throws DeveloperNotFoundException {
        DeveloperBean developerBean = tryAction(() -> storage.getDeveloper(id));
        if (developerBean == null) {
            throw ExceptionFactory.developerNotFoundException(id);
        }
        return developerBean;
    }

    /**
     * Queries all matching client versions to the corresponding developer
     *
     * @param developer the developer
     * @return a list of ClientVersionSummaryBeans
     */
    private List<ClientVersionSummaryBean> queryMatchingClientVersions(DeveloperBean developer) {
        List<ClientVersionSummaryBean> clientVersionSummaryBeans = new ArrayList<>();
        Set<DeveloperMappingBean> developerMappingBeans = developer.getClients();

        for (DeveloperMappingBean bean : developerMappingBeans) {
            List<ClientVersionSummaryBean> allClientVersionsList =
                tryAction(() -> query.getClientVersions(bean.getOrganizationId(), bean.getClientId()));
            clientVersionSummaryBeans.addAll(allClientVersionsList);
        }
        return clientVersionSummaryBeans;
    }
}
