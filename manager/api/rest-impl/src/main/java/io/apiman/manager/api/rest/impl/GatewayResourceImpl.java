/*
 * Copyright 2014 JBoss Inc
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

import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.AlreadyExistsException;
import io.apiman.manager.api.core.exceptions.DoesNotExistException;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.contract.IGatewayResource;
import io.apiman.manager.api.rest.contract.exceptions.GatewayAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation of the Gateway API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class GatewayResourceImpl implements IGatewayResource {

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    public GatewayResourceImpl() {
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#list()
     */
    @Override
    public List<GatewayBean> list() throws NotAuthorizedException {
        try {
            SearchResultsBean<GatewayBean> resultsBean = query.listGateways();
            List<GatewayBean> beans = resultsBean.getBeans();
            return beans;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#create(io.apiman.manager.api.beans.orgs.GatewayBean)
     */
    @Override
    public GatewayBean create(GatewayBean bean) throws GatewayAlreadyExistsException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        Date now = new Date();

        bean.setId(BeanUtils.idFromName(bean.getName()));
        bean.setCreatedBy(securityContext.getCurrentUser());
        bean.setCreatedOn(now);
        bean.setModifiedBy(securityContext.getCurrentUser());
        bean.setModifiedOn(now);
        try {
            storage.beginTx();
            // Store/persist the new gateway
            storage.createGateway(bean);
            storage.commitTx();
            return bean;
        } catch (AlreadyExistsException e) {
            storage.rollbackTx();
            throw ExceptionFactory.gatewayAlreadyExistsException(bean.getName());
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#get(java.lang.String)
     */
    @Override
    public GatewayBean get(String gatewayId) throws GatewayNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            GatewayBean bean = storage.getGateway(gatewayId);
            storage.commitTx();
            return bean;
        } catch (DoesNotExistException e) {
            storage.rollbackTx();
            throw ExceptionFactory.gatewayNotFoundException(gatewayId);
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#update(java.lang.String, io.apiman.manager.api.beans.orgs.GatewayBean)
     */
    @Override
    public void update(String gatewayId, GatewayBean bean)
            throws GatewayNotFoundException, NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            Date now = new Date();

            bean.setId(gatewayId);
            GatewayBean gbean = storage.getGateway(gatewayId);
            gbean.setModifiedBy(securityContext.getCurrentUser());
            gbean.setModifiedOn(now);
            if (bean.getName() != null)
                gbean.setName(bean.getName());
            if (bean.getDescription() != null)
                gbean.setDescription(bean.getDescription());
            if (bean.getHttpEndpoint() != null)
                gbean.setHttpEndpoint(bean.getHttpEndpoint());
            if (bean.getType() != null)
                gbean.setType(bean.getType());
            if (bean.getConfiguration() != null)
                gbean.setConfiguration(bean.getConfiguration());
            storage.updateGateway(gbean);
            storage.commitTx();
        } catch (DoesNotExistException e) {
            storage.rollbackTx();
            throw ExceptionFactory.gatewayNotFoundException(gatewayId);
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#delete(java.lang.String)
     */
    @Override
    public void delete(String gatewayId) throws GatewayNotFoundException,
            NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            GatewayBean gbean = storage.getGateway(gatewayId);
            storage.deleteGateway(gbean);
            storage.commitTx();
        } catch (DoesNotExistException e) {
            storage.rollbackTx();
            throw ExceptionFactory.gatewayNotFoundException(gatewayId);
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @return the storage
     */
    public IStorage getStorage() {
        return storage;
    }

    /**
     * @param storage the storage to set
     */
    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    /**
     * @return the securityContext
     */
    public ISecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * @param securityContext the securityContext to set
     */
    public void setSecurityContext(ISecurityContext securityContext) {
        this.securityContext = securityContext;
    }
    
}
