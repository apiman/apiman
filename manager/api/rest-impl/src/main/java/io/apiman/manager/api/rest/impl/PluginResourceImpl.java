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

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.api.core.IPluginRegistry;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.InvalidPluginException;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.contract.IPluginResource;
import io.apiman.manager.api.rest.contract.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.PluginAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PluginNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation of the Plugin API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PluginResourceImpl implements IPluginResource {

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject ISecurityContext securityContext;
    @Inject IPluginRegistry pluginRegistry;

    /**
     * Constructor.
     */
    public PluginResourceImpl() {
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IPluginResource#list()
     */
    @Override
    public List<PluginSummaryBean> list() throws NotAuthorizedException {
        try {
            return query.listPlugins();
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IPluginResource#create(io.apiman.manager.api.beans.orgs.PluginBean)
     */
    @Override
    public PluginBean create(PluginBean bean) throws PluginAlreadyExistsException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        try {
            Plugin plugin = pluginRegistry.loadPlugin(new PluginCoordinates(bean.getGroupId(), bean.getArtifactId(), bean.getVersion(),
                    bean.getClassifier(), bean.getType()));
            bean.setName(plugin.getName());
            bean.setDescription(plugin.getDescription());
        } catch (InvalidPluginException e) {
            throw new SystemErrorException(e);
        }
        
        // TODO auto-discover policy definitions in the plugin and add them to the storage
        
        Date now = new Date();
        bean.setId(null);
        bean.setCreatedBy(securityContext.getCurrentUser());
        bean.setCreatedOn(now);
        try {
            storage.beginTx();
            if (storage.getPlugin(bean.getGroupId(), bean.getArtifactId(), bean.getVersion(), bean.getClassifier(), bean.getType()) != null) {
                throw ExceptionFactory.pluginAlreadyExistsException();
            }
            storage.createPlugin(bean);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return bean;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IPluginResource#get(java.lang.String)
     */
    @Override
    public PluginBean get(Long pluginId) throws PluginNotFoundException, NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            PluginBean bean = storage.getPlugin(pluginId);
            if (bean == null) {
                throw ExceptionFactory.pluginNotFoundException(pluginId);
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
     * @see io.apiman.manager.api.rest.contract.IPluginResource#delete(java.lang.String)
     */
    @Override
    public void delete(Long pluginId) throws PluginNotFoundException,
            NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();
        try {
            storage.beginTx();
            PluginBean pbean = storage.getPlugin(pluginId);
            if (pbean == null) {
                throw ExceptionFactory.pluginNotFoundException(pluginId);
            }
            storage.deletePlugin(pbean);
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
