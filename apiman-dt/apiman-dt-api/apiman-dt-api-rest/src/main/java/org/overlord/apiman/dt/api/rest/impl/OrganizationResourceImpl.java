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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.BeanUtils;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.persist.AlreadyExistsException;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;

/**
 * Implementation of the Organization API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class OrganizationResourceImpl implements IOrganizationResource {
    
    @Inject IStorage storage;
    
    /**
     * Constructor.
     */
    public OrganizationResourceImpl() {
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#create(org.overlord.apiman.dt.api.beans.orgs.OrganizationBean)
     */
    @Override
    public OrganizationBean create(OrganizationBean bean) throws OrganizationAlreadyExistsException {
        bean.setId(BeanUtils.idFromName(bean.getName()));
        bean.setCreatedOn(new Date());
        try {
            storage.create(bean);
            return bean;
        } catch (AlreadyExistsException e) {
            throw OrganizationAlreadyExistsException.create(bean.getName());
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IOrganizationResource#get(java.lang.String)
     */
    @Override
    public OrganizationBean get(String organizationId) throws OrganizationNotFoundException {
        try {
            return storage.get(organizationId, OrganizationBean.class);
        } catch (DoesNotExistException e) {
            throw OrganizationNotFoundException.create(organizationId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
}
