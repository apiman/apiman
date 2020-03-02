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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.beans.GatewayEndpoint;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.GatewayType;
import io.apiman.manager.api.beans.gateways.NewGatewayBean;
import io.apiman.manager.api.beans.gateways.RestGatewayConfigBean;
import io.apiman.manager.api.beans.gateways.UpdateGatewayBean;
import io.apiman.manager.api.beans.summary.GatewayEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.GatewayTestResultBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.contract.IGatewayResource;
import io.apiman.manager.api.rest.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.exceptions.GatewayAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
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
    @Inject IGatewayLinkFactory gatewayLinkFactory;
    @Inject @ApimanLogger(GatewayResourceImpl.class) IApimanLogger log;
    @Inject IDataEncrypter encrypter;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     */
    public GatewayResourceImpl() {
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#test(io.apiman.manager.api.beans.gateways.NewGatewayBean)
     */
    @Override
    public GatewayTestResultBean test(NewGatewayBean gatewayToTest) throws NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        GatewayTestResultBean rval = new GatewayTestResultBean();

        try {
            GatewayBean testGateway = new GatewayBean();
            testGateway.setName(gatewayToTest.getName());
            testGateway.setType(gatewayToTest.getType());
            testGateway.setConfiguration(gatewayToTest.getConfiguration());
            IGatewayLink gatewayLink = gatewayLinkFactory.create(testGateway);
            SystemStatus status = gatewayLink.getStatus();
            String detail = mapper.writer().writeValueAsString(status);
            rval.setSuccess(true);
            rval.setDetail(detail);
        } catch (GatewayAuthenticationException e) {
            rval.setSuccess(false);
            rval.setDetail(Messages.i18n.format("GatewayResourceImpl.AuthenticationFailed")); //$NON-NLS-1$
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
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#create(io.apiman.manager.api.beans.gateways.NewGatewayBean)
     */
    @Override
    public GatewayBean create(NewGatewayBean gatewayToInsert) throws GatewayAlreadyExistsException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        Date now = new Date();

        GatewayBean gateway = new GatewayBean();
        gateway.setId(BeanUtils.idFromName(gatewayToInsert.getName()));
        gateway.setName(gatewayToInsert.getName());
        gateway.setDescription(gatewayToInsert.getDescription());
        gateway.setType(gatewayToInsert.getType());
        gateway.setConfiguration(gatewayToInsert.getConfiguration());
        gateway.setCreatedBy(securityContext.getCurrentUser());
        gateway.setCreatedOn(now);
        gateway.setModifiedBy(securityContext.getCurrentUser());
        gateway.setModifiedOn(now);
        try {
            storage.beginTx();
            if (storage.getGateway(gateway.getId()) != null) {
                throw ExceptionFactory.gatewayAlreadyExistsException(gateway.getName());
            }
            // Store/persist the new gateway
            encryptPasswords(gateway);
            storage.createGateway(gateway);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        decryptPasswords(gateway);

        log.debug(String.format("Successfully created new gateway %s: %s", gateway.getName(), gateway)); //$NON-NLS-1$
        return gateway;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#get(java.lang.String)
     */
    @Override
    public GatewayBean get(String gatewayId) throws GatewayNotFoundException, NotAuthorizedException {
        try {
            storage.beginTx();
            GatewayBean gateway = storage.getGateway(gatewayId);
            if (gateway == null) {
                throw ExceptionFactory.gatewayNotFoundException(gatewayId);
            }
            if (!securityContext.isAdmin()) {
                gateway.setConfiguration(null);
            } else {
                decryptPasswords(gateway);
            }
            storage.commitTx();

            log.debug(String.format("Successfully fetched gateway %s: %s", gateway.getName(), gateway)); //$NON-NLS-1$
            return gateway;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#getGatewayEndpoint(java.lang.String)
     */
    public GatewayEndpointSummaryBean getGatewayEndpoint(String gatewayId)  throws GatewayNotFoundException,
            NotAuthorizedException {
        try {
            storage.beginTx();
            GatewayBean gateway = storage.getGateway(gatewayId);
            if (gateway == null) {
                throw ExceptionFactory.gatewayNotFoundException(gatewayId);
            } else {
                log.debug(String.format("Got endpoint summary: %s", gateway)); //$NON-NLS-1$
            }
            IGatewayLink link = gatewayLinkFactory.create(gateway);
            GatewayEndpoint endpoint = link.getGatewayEndpoint();
            GatewayEndpointSummaryBean gatewayEndpoint = new GatewayEndpointSummaryBean();
            gatewayEndpoint.setEndpoint(endpoint.getEndpoint());
            storage.commitTx();
            return gatewayEndpoint;
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IGatewayResource#update(java.lang.String, io.apiman.manager.api.beans.gateways.UpdateGatewayBean)
     */
    @Override
    public void update(String gatewayId, UpdateGatewayBean gatewayToUpdate) throws GatewayNotFoundException,
            NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            Date now = new Date();

            GatewayBean gateway = storage.getGateway(gatewayId);
            if (gateway == null) {
                throw ExceptionFactory.gatewayNotFoundException(gatewayId);
            }
            gateway.setModifiedBy(securityContext.getCurrentUser());
            gateway.setModifiedOn(now);
            if (gatewayToUpdate.getDescription() != null)
                gateway.setDescription(gatewayToUpdate.getDescription());
            if (gatewayToUpdate.getType() != null)
                gateway.setType(gatewayToUpdate.getType());
            if (gatewayToUpdate.getConfiguration() != null)
                gateway.setConfiguration(gatewayToUpdate.getConfiguration());
            encryptPasswords(gateway);
            storage.updateGateway(gateway);
            storage.commitTx();

            log.debug(String.format("Successfully updated gateway %s: %s", gateway.getName(), gateway)); //$NON-NLS-1$
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
            GatewayBean gateway = storage.getGateway(gatewayId);
            if (gateway == null) {
                throw ExceptionFactory.gatewayNotFoundException(gatewayId);
            }
            storage.deleteGateway(gateway);
            storage.commitTx();

            log.debug(String.format("Successfully deleted gateway %s: %s", gateway.getName(), gateway)); //$NON-NLS-1$
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @param gateway
     */
    private void encryptPasswords(GatewayBean gateway) {
        if (gateway.getConfiguration() == null) {
            return;
        }
        try {
            if (gateway.getType() == GatewayType.REST) {
                RestGatewayConfigBean config = mapper.readValue(gateway.getConfiguration(), RestGatewayConfigBean.class);
                config.setPassword(encrypter.encrypt(config.getPassword(), new DataEncryptionContext()));
                gateway.setConfiguration(mapper.writeValueAsString(config));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param gateway
     */
    private void decryptPasswords(GatewayBean gateway) {
        if (gateway.getConfiguration() == null) {
            return;
        }
        try {
            if (gateway.getType() == GatewayType.REST) {
                RestGatewayConfigBean config = mapper.readValue(gateway.getConfiguration(), RestGatewayConfigBean.class);
                config.setPassword(encrypter.decrypt(config.getPassword(), new DataEncryptionContext()));
                gateway.setConfiguration(mapper.writeValueAsString(config));
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
