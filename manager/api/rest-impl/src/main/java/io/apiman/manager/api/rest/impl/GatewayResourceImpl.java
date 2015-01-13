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

import io.apiman.common.util.AesEncrypter;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.GatewayType;
import io.apiman.manager.api.beans.gateways.RestGatewayConfigBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.GatewayTestResultBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.contract.IGatewayResource;
import io.apiman.manager.api.rest.contract.exceptions.AbstractRestException;
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

import org.codehaus.jackson.map.ObjectMapper;

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
    @Inject IGatewayLinkFactory gatewayLinkFactory;

    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Constructor.
     */
    public GatewayResourceImpl() {
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#test(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public GatewayTestResultBean test(GatewayBean bean) throws NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        GatewayTestResultBean rval = new GatewayTestResultBean();

        try {
            IGatewayLink gatewayLink = gatewayLinkFactory.create(bean);
            SystemStatus status = gatewayLink.getStatus();
            String detail = mapper.writer().writeValueAsString(status);
            rval.setSuccess(true);
            rval.setDetail(detail);
        } catch (Exception e) {
            rval.setSuccess(false);
            rval.setDetail(e.getMessage());
        }

        return rval;
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#list()
     */
    @Override
    public List<GatewaySummaryBean> list() throws NotAuthorizedException {
        try {
            return query.listGateways();
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
            if (storage.getGateway(bean.getId()) != null) {
                throw ExceptionFactory.gatewayAlreadyExistsException(bean.getName());
            }
            // Store/persist the new gateway
            encryptPasswords(bean);
            storage.createGateway(bean);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        decryptPasswords(bean);
        return bean;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#get(java.lang.String)
     */
    @Override
    public GatewayBean get(String gatewayId) throws GatewayNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            GatewayBean bean = storage.getGateway(gatewayId);
            if (bean == null) {
                throw ExceptionFactory.gatewayNotFoundException(gatewayId);
            }
            if (!securityContext.isAdmin()) {
                bean.setConfiguration(null);
            } else {
                decryptPasswords(bean);
            }
            storage.commitTx();
            return bean;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
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
            if (gbean == null) {
                throw ExceptionFactory.gatewayNotFoundException(gatewayId);
            }
            gbean.setModifiedBy(securityContext.getCurrentUser());
            gbean.setModifiedOn(now);
            if (bean.getName() != null)
                gbean.setName(bean.getName());
            if (bean.getDescription() != null)
                gbean.setDescription(bean.getDescription());
            if (bean.getType() != null)
                gbean.setType(bean.getType());
            if (bean.getConfiguration() != null)
                gbean.setConfiguration(bean.getConfiguration());
            encryptPasswords(gbean);
            storage.updateGateway(gbean);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
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
            if (gbean == null) {
                throw ExceptionFactory.gatewayNotFoundException(gatewayId);
            }
            storage.deleteGateway(gbean);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @param bean
     */
    private void encryptPasswords(GatewayBean bean) {
        if (bean.getConfiguration() == null) {
            return;
        }
        try {
            if (bean.getType() == GatewayType.REST) {
                RestGatewayConfigBean configBean = mapper.readValue(bean.getConfiguration(), RestGatewayConfigBean.class);
                configBean.setPassword(AesEncrypter.encrypt(configBean.getPassword()));
                bean.setConfiguration(mapper.writeValueAsString(configBean));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @param bean
     */
    private void decryptPasswords(GatewayBean bean) {
        if (bean.getConfiguration() == null) {
            return;
        }
        try {
            if (bean.getType() == GatewayType.REST) {
                RestGatewayConfigBean configBean = mapper.readValue(bean.getConfiguration(), RestGatewayConfigBean.class);
                configBean.setPassword(AesEncrypter.decrypt(configBean.getPassword()));
                bean.setConfiguration(mapper.writeValueAsString(configBean));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
