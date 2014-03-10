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

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.BeanUtils;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.services.ServiceStatus;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ServicePlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.persist.AlreadyExistsException;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IIdmStorage;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.persist.IStorageQuery;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.overlord.apiman.dt.api.rest.contract.IRoleResource;
import org.overlord.apiman.dt.api.rest.contract.IServiceResource;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.impl.util.ExceptionFactory;
import org.overlord.apiman.dt.api.security.ISecurityContext;

/**
 * Implementation of the Service API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ServiceResourceImpl implements IServiceResource {

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject IIdmStorage idmStorage;
    
    @Inject IUserResource users;
    @Inject IRoleResource roles;
    
    @Inject ISecurityContext securityContext;
    
    /**
     * Constructor.
     */
    public ServiceResourceImpl() {
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IServiceResource#create(java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceBean)
     */
    @Override
    public ServiceBean create(String organizationId, ServiceBean bean)
            throws OrganizationNotFoundException, ServiceAlreadyExistsException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        try {
            storage.get(organizationId, OrganizationBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.organizationNotFoundException(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        String currentUser = securityContext.getCurrentUser();

        bean.setOrganizationId(organizationId);
        bean.setId(BeanUtils.idFromName(bean.getName()));
        bean.setCreatedOn(new Date());
        bean.setCreatedBy(currentUser);
        try {
            // Store/persist the new service
            storage.create(bean);
            return bean;
        } catch (AlreadyExistsException e) {
            throw ExceptionFactory.serviceAlreadyExistsException(bean.getName());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IServiceResource#get(java.lang.String, java.lang.String)
     */
    @Override
    public ServiceBean get(String organizationId, String serviceId)
            throws ServiceNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            return storage.get(organizationId, serviceId, ServiceBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IServiceResource#list(java.lang.String)
     */
    @Override
    public List<ServiceSummaryBean> list(String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.get(organizationId, OrganizationBean.class);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.organizationNotFoundException(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }

        try {
            return query.getServicesInOrg(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IServiceResource#update(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceBean)
     */
    @Override
    public void update(String organizationId, String serviceId, ServiceBean bean)
            throws ServiceNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            bean.setOrganizationId(organizationId);
            bean.setId(serviceId);
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IServiceResource#createVersion(java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceVersionBean)
     */
    @Override
    public ServiceVersionBean createVersion(String organizationId, String serviceId, ServiceVersionBean bean)
            throws ServiceNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ServiceBean service = storage.get(organizationId, serviceId, ServiceBean.class);
            bean.setCreatedBy(securityContext.getCurrentUser());
            bean.setCreatedOn(new Date());
            bean.setStatus(ServiceStatus.Created);
            bean.setService(service);
            storage.create(bean);
            return bean;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IServiceResource#getVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ServiceVersionBean getVersion(String organizationId, String serviceId, String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        try {
            ServiceVersionBean serviceVersion = query.getServiceVersion(organizationId, serviceId, version);
            if (serviceVersion == null)
                throw ExceptionFactory.serviceVersionNotFoundException(serviceId, version);
            return serviceVersion;
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IServiceResource#updateVersion(java.lang.String, java.lang.String, java.lang.String, org.overlord.apiman.dt.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void updateVersion(String organizationId, String serviceId, String version, ServiceVersionBean bean)
            throws ServiceVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        // TODO throw error if version is not in the right state
        try {
            ServiceVersionBean svb = getVersion(organizationId, serviceId, version);
            bean.setId(svb.getId());
            bean.setService(svb.getService());
            bean.setStatus(ServiceStatus.Created);
            bean.setPublishedOn(null);
            bean.setRetiredOn(null);
            storage.update(bean);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IServiceResource#listVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ServiceVersionBean> listVersions(String organizationId, String serviceId)
            throws ServiceNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Try to get the service first - will throw a ServiceNotFoundException if not found.
        get(organizationId, serviceId);
        
        try {
            return query.getServiceVersions(organizationId, serviceId);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IServiceResource#getVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ServicePlanSummaryBean> getVersionPlans(String organizationId, String serviceId,
            String version) throws ServiceVersionNotFoundException, NotAuthorizedException {
        if (!securityContext.hasPermission(PermissionType.svcView, organizationId))
            throw ExceptionFactory.notAuthorizedException();
        
        // Ensure the version exists first.
        getVersion(organizationId, serviceId, version);
        
        try {
            return query.getServiceVersionPlans(organizationId, serviceId, version);
        } catch (DoesNotExistException e) {
            throw ExceptionFactory.serviceNotFoundException(serviceId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
