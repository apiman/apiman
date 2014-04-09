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

package org.overlord.apiman.dt.api.rest.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.actions.ActionBean;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.gateway.IGatewayLink;
import org.overlord.apiman.dt.api.persist.IIdmStorage;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.rest.contract.IActionResource;
import org.overlord.apiman.dt.api.rest.contract.IServiceResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ActionException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.impl.util.ExceptionFactory;
import org.overlord.apiman.dt.api.security.ISecurityContext;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;

/**
 * Implementation of the Action API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ActionResourceImpl implements IActionResource {

    @Inject IStorage storage;
    @Inject IIdmStorage idmStorage;
    @Inject IGatewayLink gatewayLink;
    
    @Inject IServiceResource services;

    @Inject ISecurityContext securityContext;

    /**
     * Constructor.
     */
    public ActionResourceImpl() {
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IActionResource#performAction(org.overlord.apiman.dt.api.beans.actions.ActionBean)
     */
    @Override
    public void performAction(ActionBean action) throws ActionException {
        switch (action.getType()) {
            case publishService:
                publishService(action);
                return;
            case retireService:
                retireService(action);
                return;
            case registerApplication:
                registerApplication(action);
                return;
            case deregisterApplication:
                deregisterApplication(action);
                return;
            default:
                throw ExceptionFactory.actionException("Action type not supported: " + action.getType().toString()); //$NON-NLS-1$
        }
    }

    /**
     * @param action
     */
    private void publishService(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, action.getOrganizationId()))
            throw ExceptionFactory.notAuthorizedException();

        ServiceVersionBean versionBean = null;
        try {
            versionBean = services.getVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ServiceVersionNotFoundException e) {
            throw ExceptionFactory.actionException("Service not found.");
        }
        
        Service gatewaySvc = new Service();
        gatewaySvc.setEndpoint(versionBean.getEndpoint());
        gatewaySvc.setEndpointType(versionBean.getEndpointType().toString());
        gatewaySvc.setOrganizationId(versionBean.getService().getOrganizationId());
        gatewaySvc.setServiceId(versionBean.getService().getId());
        gatewaySvc.setVersion(versionBean.getVersion());
        
        try {
            gatewayLink.publishService(gatewaySvc);
        } catch (PublishingException e) {
            throw ExceptionFactory.actionException("Failed to publish service.", e);
        }
    }

    /**
     * @param action
     */
    private void retireService(ActionBean action) throws ActionException {
        // TODO Auto-generated method stub
        throw ExceptionFactory.actionException("Not yet implemented.");

    }

    /**
     * @param action
     */
    private void registerApplication(ActionBean action) throws ActionException {
        // TODO Auto-generated method stub
        throw ExceptionFactory.actionException("Not yet implemented.");

    }

    /**
     * @param action
     */
    private void deregisterApplication(ActionBean action) throws ActionException {
        // TODO Auto-generated method stub
        throw ExceptionFactory.actionException("Not yet implemented.");

    }
        
}
