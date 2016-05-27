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

import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginClassLoader;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.plugin.PluginUtils;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.plugins.NewPluginBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.plugins.PluginRegistryBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyFormType;
import io.apiman.manager.api.core.IPluginRegistry;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.core.exceptions.InvalidPluginException;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.rest.contract.IPluginResource;
import io.apiman.manager.api.rest.contract.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.PluginAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PluginNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PluginResourceNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.i18n.Messages;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of the Plugin API.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PluginResourceImpl implements IPluginResource {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject ISecurityContext securityContext;
    @Inject IPluginRegistry pluginRegistry;
    @Inject ApiManagerConfig config;

    private Map<URI, PluginRegistryBean> registryCache = new HashMap<>();

    @Inject @ApimanLogger(PluginResourceImpl.class)
    IApimanLogger log;

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
     * @see io.apiman.manager.api.rest.contract.IPluginResource#create(io.apiman.manager.api.beans.plugins.NewPluginBean)
     */
    @Override
    public PluginBean create(NewPluginBean bean) throws PluginAlreadyExistsException, PluginNotFoundException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        PluginCoordinates coordinates = new PluginCoordinates(bean.getGroupId(), bean.getArtifactId(), bean.getVersion(),
                bean.getClassifier(), bean.getType());

        boolean isSnapshot = PluginUtils.isSnapshot(coordinates);
        if (isSnapshot) {
            log.debug("Loading a snapshot version of plugin: " + coordinates); //$NON-NLS-1$
        }
        boolean isUpgrade = isSnapshot || bean.isUpgrade();

        Plugin plugin;
        try {
            plugin = pluginRegistry.loadPlugin(coordinates);
            bean.setName(plugin.getName());
            bean.setDescription(plugin.getDescription());
        } catch (InvalidPluginException e) {
            throw new PluginNotFoundException(coordinates.toString(), e);
        }

        PluginBean pluginBean = new PluginBean();
        pluginBean.setGroupId(bean.getGroupId());
        pluginBean.setArtifactId(bean.getArtifactId());
        pluginBean.setVersion(bean.getVersion());
        pluginBean.setClassifier(bean.getClassifier());
        pluginBean.setType(bean.getType());
        pluginBean.setName(bean.getName());
        pluginBean.setDescription(bean.getDescription());
        pluginBean.setCreatedBy(securityContext.getCurrentUser());
        pluginBean.setCreatedOn(new Date());
        try {
            storage.beginTx();
            PluginBean existingPlugin = storage.getPlugin(bean.getGroupId(), bean.getArtifactId());

            boolean hasExistingPlugin = existingPlugin != null && !existingPlugin.isDeleted();
            boolean isUpdatePolicyDefs = false;

            if (hasExistingPlugin && !isUpgrade) {
                throw ExceptionFactory.pluginAlreadyExistsException();
            } else if (hasExistingPlugin && isUpgrade) {
                isUpdatePolicyDefs = true;
                existingPlugin.setName(pluginBean.getName());
                existingPlugin.setDescription(pluginBean.getDescription());
                existingPlugin.setVersion(pluginBean.getVersion());
                existingPlugin.setClassifier(pluginBean.getClassifier());
                existingPlugin.setType(pluginBean.getType());
                pluginBean.setId(existingPlugin.getId());
                storage.updatePlugin(existingPlugin);
            } else if (!hasExistingPlugin && existingPlugin != null) {
                isUpdatePolicyDefs = true;
                existingPlugin.setName(pluginBean.getName());
                existingPlugin.setDescription(pluginBean.getDescription());
                existingPlugin.setVersion(pluginBean.getVersion());
                existingPlugin.setClassifier(pluginBean.getClassifier());
                existingPlugin.setType(pluginBean.getType());
                existingPlugin.setCreatedOn(new Date());
                existingPlugin.setCreatedBy(securityContext.getCurrentUser());
                existingPlugin.setDeleted(false);
                pluginBean.setId(existingPlugin.getId());
                storage.updatePlugin(existingPlugin);
            } else {
                if (bean.isUpgrade()) {
                    throw ExceptionFactory.pluginNotFoundException(0L);
                }
                storage.createPlugin(pluginBean);
            }

            // Process any contributed policy definitions.
            List<URL> policyDefs = plugin.getPolicyDefinitions();
            int createdPolicyDefCounter = 0;
            int updatedPolicyDefCounter = 0;
            for (URL url : policyDefs) {
                PolicyDefinitionBean policyDef = (PolicyDefinitionBean) mapper.reader(PolicyDefinitionBean.class).readValue(url);
                if (policyDef.getId() == null || policyDef.getId().trim().isEmpty()) {
                    throw ExceptionFactory.policyDefInvalidException(Messages.i18n.format("PluginResourceImpl.MissingPolicyDefId", policyDef.getName())); //$NON-NLS-1$
                }
                policyDef.setPluginId(pluginBean.getId());
                if (policyDef.getId() == null) {
                    policyDef.setId(BeanUtils.idFromName(policyDef.getName()));
                } else {
                    policyDef.setId(BeanUtils.idFromName(policyDef.getId()));
                }
                if (policyDef.getFormType() == null) {
                    policyDef.setFormType(PolicyFormType.Default);
                }
                PolicyDefinitionBean existingPolicyDef = storage.getPolicyDefinition(policyDef.getId());
                if (existingPolicyDef == null) {
                    storage.createPolicyDefinition(policyDef);
                    createdPolicyDefCounter++;
                } else if (isUpdatePolicyDefs) {
                    existingPolicyDef.setName(policyDef.getName());
                    existingPolicyDef.setDescription(policyDef.getDescription());
                    existingPolicyDef.setIcon(policyDef.getIcon());
                    existingPolicyDef.getTemplates().clear();
                    existingPolicyDef.getTemplates().addAll(policyDef.getTemplates());
                    existingPolicyDef.setFormType(policyDef.getFormType());
                    existingPolicyDef.setForm(policyDef.getForm());
                    existingPolicyDef.setDeleted(false);
                    existingPolicyDef.setPolicyImpl(policyDef.getPolicyImpl());
                    storage.updatePolicyDefinition(existingPolicyDef);
                    updatedPolicyDefCounter++;
                } else {
                    throw ExceptionFactory.policyDefInvalidException(Messages.i18n.format("PluginResourceImpl.DuplicatePolicyDef", policyDef.getId())); //$NON-NLS-1$
                }
            }

            storage.commitTx();
            log.info(String.format("Created plugin mvn:%s:%s:%s", pluginBean.getGroupId(), pluginBean.getArtifactId(),  //$NON-NLS-1$
                    pluginBean.getVersion()));
            log.info(String.format("\tCreated %s policy definitions from plugin.", String.valueOf(createdPolicyDefCounter))); //$NON-NLS-1$
            if (isUpdatePolicyDefs) {
                log.info(String.format("\tUpdated %s policy definitions from plugin.", String.valueOf(updatedPolicyDefCounter))); //$NON-NLS-1$
            }
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        return pluginBean;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IPluginResource#get(java.lang.Long)
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
     * @see io.apiman.manager.api.rest.contract.IPluginResource#delete(java.lang.Long)
     */
    @Override
    public void delete(Long pluginId) throws PluginNotFoundException,
            NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        try {
            List<PolicyDefinitionSummaryBean> policyDefs = query.listPluginPolicyDefs(pluginId);

            storage.beginTx();
            PluginBean pbean = storage.getPlugin(pluginId);
            if (pbean == null) {
                throw ExceptionFactory.pluginNotFoundException(pluginId);
            }
            pbean.setDeleted(true);
            storage.updatePlugin(pbean);

            // Now delete all the policy definitions for this plugin.
            for (PolicyDefinitionSummaryBean policyDef : policyDefs) {
                PolicyDefinitionBean definition = storage.getPolicyDefinition(policyDef.getId());
                if (definition != null) {
                    definition.setDeleted(true);
                    storage.updatePolicyDefinition(definition);
                }
            }

            storage.commitTx();
            log.info(String.format("Deleted plugin mvn:%s:%s:%s", pbean.getGroupId(), pbean.getArtifactId(),  //$NON-NLS-1$
                    pbean.getVersion()));
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IPluginResource#getPolicyDefs(java.lang.Long)
     */
    @Override
    public List<PolicyDefinitionSummaryBean> getPolicyDefs(Long pluginId) throws PluginNotFoundException {
        get(pluginId);
        try {
            return query.listPluginPolicyDefs(pluginId);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @return 
     * @see io.apiman.manager.api.rest.contract.IPluginResource#getPolicyForm(java.lang.Long, java.lang.String)
     */
    @Override
    public Response getPolicyForm(Long pluginId, String policyDefId) throws PluginNotFoundException,
            PluginResourceNotFoundException, PolicyDefinitionNotFoundException {
        PluginBean pbean;
        PolicyDefinitionBean pdBean;
        
        try {
            storage.beginTx();
            pbean = storage.getPlugin(pluginId);
            if (pbean == null) {
                throw ExceptionFactory.pluginNotFoundException(pluginId);
            }
            pdBean = storage.getPolicyDefinition(policyDefId);
            storage.commitTx();
        } catch (AbstractRestException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
        PluginCoordinates coordinates = new PluginCoordinates(pbean.getGroupId(), pbean.getArtifactId(),
                pbean.getVersion(), pbean.getClassifier(), pbean.getType());
        try {
            if (pdBean == null) {
                throw ExceptionFactory.policyDefNotFoundException(policyDefId);
            }
            if (pdBean.getPluginId() == null || !pdBean.getPluginId().equals(pbean.getId())) {
                throw ExceptionFactory.pluginNotFoundException(pluginId);
            }
            if ((pdBean.getFormType() == PolicyFormType.JsonSchema || pdBean.getFormType() == PolicyFormType.AngularTemplate) 
                    && pdBean.getForm() != null) {
                String formPath = pdBean.getForm();
                
                if (!formPath.startsWith("/")) { //$NON-NLS-1$
                    formPath = "META-INF/apiman/policyDefs/" + formPath; //$NON-NLS-1$
                } else {
                    formPath = formPath.substring(1);
                }
                
                Plugin plugin = pluginRegistry.loadPlugin(coordinates);
                PluginClassLoader loader = plugin.getLoader();
                InputStream resource = null;
                
                resource = loader.getResourceAsStream(formPath);
                
                if (resource == null) {
                    throw ExceptionFactory.pluginResourceNotFoundException(formPath, coordinates);
                }
                
                MediaType type = MediaType.APPLICATION_JSON_TYPE;
                
                if (pdBean.getFormType() == PolicyFormType.AngularTemplate) {
                    type = MediaType.TEXT_HTML_TYPE;
                }
                
                return Response
                        .ok(resource, type)
                        .build();
            } else {
                throw ExceptionFactory.pluginResourceNotFoundException(null, coordinates);
            }
        } catch (AbstractRestException e) {
            throw e;
        } catch (Throwable t) {
            throw new SystemErrorException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.IPluginResource#getAvailablePlugins()
     */
    @Override
    public List<PluginSummaryBean> getAvailablePlugins() throws NotAuthorizedException {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        List<PluginSummaryBean> rval = new ArrayList<>();
        Set<URI> registries = config.getPluginRegistries();

        for (URI registryUrl : registries) {
            PluginRegistryBean registry = loadRegistry(registryUrl);
            if (registry == null) {
                System.out.println("WARN: plugin registry failed to load - " + registryUrl); //$NON-NLS-1$
            } else {
                rval.addAll(registry.getPlugins());
            }
        }

        // Sort before returning
        Collections.sort(rval, new Comparator<PluginSummaryBean>() {
            @Override
            public int compare(PluginSummaryBean o1, PluginSummaryBean o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return rval;
    }

    /**
     * Loads a plugin registry from its URL.  Will use the value in the
     * cache if it exists.  If not, it will connect to the remote URL and
     * grab the registry JSON file.
     * @param registryUrl the URL of the registry
     */
    private PluginRegistryBean loadRegistry(URI registryUrl) {
        PluginRegistryBean fromCache = registryCache.get(registryUrl);
        if (fromCache != null) {
            return fromCache;
        }
        try {
            PluginRegistryBean registry = mapper.reader(PluginRegistryBean.class).readValue(registryUrl.toURL());
            registryCache.put(registryUrl, registry);
            return registry;
        } catch (IOException e) {
            return null;
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
