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

import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.developers.DeveloperBean;
import io.apiman.manager.api.beans.developers.DeveloperMappingBean;
import io.apiman.manager.api.beans.developers.UpdateDeveloperBean;
import io.apiman.manager.api.beans.summary.ClientVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.IDeveloperResource;
import io.apiman.manager.api.rest.exceptions.*;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the Developer Portal API
 */
public class DeveloperResourceImpl implements IDeveloperResource {

    @Inject
    IStorage storage;
    @Inject
    IStorageQuery query;
    @Inject
    ISecurityContext securityContext;
    @Inject
    @ApimanLogger(DeveloperResourceImpl.class)
    IApimanLogger log;
    @Inject
    IGatewayLinkFactory gatewayLinkFactory;


    private OrganizationResourceImpl organizationResource;

    /**
     * Constructor
     */
    public DeveloperResourceImpl() {
    }

    @Override
    public List<ApiVersionBean> getAllPublicApiVersions() throws NotAuthorizedException {
        List<ApiVersionBean> apiVersionBeans = new ArrayList<>();
        Iterator<ApiVersionBean> iterator;
        try {
            storage.beginTx();
            iterator = storage.getAllPublicApiVersions();
            storage.commitTx();
            while (iterator.hasNext()) {
                ApiVersionBean apiVersionBean = iterator.next();
                apiVersionBeans.add(apiVersionBean);
            }

        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return apiVersionBeans;
    }

    /**
     * @see IDeveloperResource#getDevelopers()
     */
    @Override
    public List<DeveloperBean> getDevelopers() throws NotAuthorizedException {
        securityContext.checkAdminPermissions();

        Iterator<DeveloperBean> iterator;
        List<DeveloperBean> developerBeans = new ArrayList<>();
        try {
            storage.beginTx();
            iterator = storage.getDevelopers();
            storage.commitTx();
            while (iterator.hasNext()) {
                DeveloperBean bean = iterator.next();
                developerBeans.add(bean);
            }
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return developerBeans;
    }

    /**
     * @see IDeveloperResource#create(DeveloperBean)
     */
    @Override
    public DeveloperBean create(DeveloperBean bean) throws InvalidNameException, NotAuthorizedException, DeveloperAlreadyExistsException {
        securityContext.checkAdminPermissions();

        DeveloperBean developerBean = new DeveloperBean();
        developerBean.setId(bean.getId());
        developerBean.setClients(bean.getClients());

        try {
            storage.beginTx();
            if (storage.getDeveloper(bean.getId()) != null) {
                throw ExceptionFactory.developerAlreadyExistsException(bean.getId());
            }
            storage.createDeveloper(developerBean);
            storage.commitTx();
            log.debug(String.format("Created developer %s: %s", developerBean.getId(), developerBean)); //$NON-NLS-1$
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return developerBean;
    }

    /**
     * @see IDeveloperResource#update(String, UpdateDeveloperBean)
     */
    @Override
    public void update(String id, UpdateDeveloperBean bean) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        try {
            storage.beginTx();
            DeveloperBean developerBean = getDeveloperBeanFromStorage(id);
            developerBean.setClients(bean.getClients());
            storage.updateDeveloper(developerBean);
            storage.commitTx();
            log.debug(String.format("Updated developer %s: %s", developerBean.getId(), developerBean));
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IDeveloperResource#get(String)
     */
    @Override
    public DeveloperBean get(String id) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        DeveloperBean developerBean;
        try {

            storage.beginTx();
            developerBean = getDeveloperBeanFromStorage(id);
            storage.commitTx();
            log.debug(String.format("Got developer %s: %s", developerBean.getId(), developerBean));
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return developerBean;
    }

    /**
     * Gets the developer from storage
     * A transaction must be present
     *
     * @param id the id of the developer
     * @return the developer
     * @throws StorageException           if something unexpected happens
     * @throws DeveloperNotFoundException when trying to get, update, or delete an organization that does not exist
     */
    private DeveloperBean getDeveloperBeanFromStorage(String id) throws StorageException, DeveloperNotFoundException {
        DeveloperBean developerBean = storage.getDeveloper(id);
        if (developerBean == null) {
            throw ExceptionFactory.developerNotFoundException(id);
        }
        return developerBean;
    }

    /**
     * @see IDeveloperResource#delete(String)
     */
    @Override
    public void delete(String id) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        try {
            storage.beginTx();
            DeveloperBean developerBean = getDeveloperBeanFromStorage(id);
            storage.deleteDeveloper(developerBean);
            storage.commitTx();
            log.debug("Deleted developer: " + developerBean.getId()); //$NON-NLS-1$
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see IDeveloperResource#getAllApiVersions(String)
     */
    @Override
    public List<ClientVersionSummaryBean> getAllClientVersions(String id) throws DeveloperNotFoundException, NotAuthorizedException {
       securityContext.checkIfUserIsCurrentUser(id);

        DeveloperBean developer;
        List<ClientVersionSummaryBean> clientVersionSummaryBeans;
        try {
            storage.beginTx();
            developer = getDeveloperBeanFromStorage(id);
            storage.commitTx();

            clientVersionSummaryBeans = queryMatchingClientVersions(developer);
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return clientVersionSummaryBeans;
    }

    /**
     * Queries all matching client versions to the corresponding developer
     *
     * @param developer the developer
     * @return a list of ClientVersionSummaryBeans
     * @throws StorageException if something unexpected happens
     */
    private List<ClientVersionSummaryBean> queryMatchingClientVersions(DeveloperBean developer) throws StorageException {
        List<ClientVersionSummaryBean> clientVersionSummaryBeans = new ArrayList<>();
        Set<DeveloperMappingBean> developerMappingBeans = developer.getClients();

        for (DeveloperMappingBean bean : developerMappingBeans) {
            List<ClientVersionSummaryBean> allClientVersionsList = query.getClientVersions(bean.getOrganizationId(), bean.getClientId());
            clientVersionSummaryBeans.addAll(allClientVersionsList);
        }
        return clientVersionSummaryBeans;
    }

    /**
     * @see IDeveloperResource#getAllClientContracts(String)
     */
    @Override
    public List<ContractSummaryBean> getAllClientContracts(String id) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(id);

        DeveloperBean developer;
        List<ClientVersionSummaryBean> clientVersionSummaryBeans;
        List<ContractSummaryBean> contractSummaryBeans = new ArrayList<>();

        try {
            storage.beginTx();
            developer = getDeveloperBeanFromStorage(id);
            storage.commitTx();

            clientVersionSummaryBeans = queryMatchingClientVersions(developer);
            for (ClientVersionSummaryBean bean : clientVersionSummaryBeans) {
                List<ContractSummaryBean> allClientContracts = query.getClientContracts(bean.getOrganizationId(), bean.getId(), bean.getVersion());
                contractSummaryBeans.addAll(allClientContracts);
            }

        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return contractSummaryBeans;
    }

    /**
     * @see IDeveloperResource#getAllApiVersions(String)
     */
    @Override
    public List<ApiVersionBean> getAllApiVersions(String id) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(id);

        List<ApiVersionBean> apiVersionBeans = new ArrayList<>();
        List<ContractSummaryBean> contracts = getAllClientContracts(id);

        try {
            storage.beginTx();
            for (ContractSummaryBean contract : contracts) {
                ApiVersionBean apiVersion = storage.getApiVersion(contract.getApiOrganizationId(), contract.getApiId(), contract.getApiVersion());
                apiVersionBeans.add(apiVersion);
            }
            storage.commitTx();
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return apiVersionBeans;
    }

    /**
     * @see IDeveloperResource#getPublicApiDefinition(String, String, String)
     */
    @Override
    public Response getPublicApiDefinition(String organizationId, String apiId, String version) {
        instantiateOrganizationResource();

        try {
            storage.beginTx();
            ApiVersionBean apiVersion = organizationResource.getApiVersionFromStorage(organizationId, apiId, version);
            storage.commitTx();
            if (apiVersion.isPublicAPI()) {
                return organizationResource.getApiDefinition(organizationId, apiId, version);
            } else {
                throw ExceptionFactory.notAuthorizedException();
            }
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }

    }

    /**
     * @see IDeveloperResource#getApiDefinition(String, String, String, String)
     */
    @Override
    public Response getApiDefinition(String developerId, String organizationId, String apiId, String version) throws DeveloperNotFoundException, NotAuthorizedException {
        securityContext.checkIfUserIsCurrentUser(developerId);

        instantiateOrganizationResource();

        Set<DeveloperMappingBean> developerClients;
        List<ContractSummaryBean> contracts;

        try {
            storage.beginTx();
            developerClients = getDeveloperBeanFromStorage(developerId).getClients();
            // get all contracts from the API Version
            contracts = query.getContracts(organizationId, apiId, version, 1, 10000);
            storage.commitTx();

            for (ContractSummaryBean contract : contracts) {
                for (DeveloperMappingBean client : developerClients) {
                    // check if the developer is allowed to request the definition
                    if (client.getClientId().equals(contract.getClientId()) && client.getOrganizationId().equals(contract.getClientOrganizationId())) {
                        return organizationResource.getApiDefinition(organizationId, apiId, version);
                    }
                }
            }

        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return null;
    }

    /**
     * @see ActionResourceImpl#instantiateOrganizationResource()
     */
    private void instantiateOrganizationResource() {
        if (organizationResource == null) {
            organizationResource = new OrganizationResourceImpl();
            organizationResource.securityContext = securityContext;
            organizationResource.storage = storage;
            organizationResource.query = query;
            organizationResource.log = log;
            organizationResource.gatewayLinkFactory = gatewayLinkFactory;
        }
    }
}
