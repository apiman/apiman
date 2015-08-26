/*
 * Copyright 2013 JBoss Inc
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

package io.apiman.manager.api.beans.plugins;

import io.apiman.manager.api.beans.summary.PluginSummaryBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Models a plugin registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class PluginRegistryBean implements Serializable {

    private static final long serialVersionUID = 8873068183817055704L;
    
    private String id;
    private String name;
    private String description;
    private String version;
    private PluginRegistryRepositoryBean repository;
    private List<PluginSummaryBean> plugins = new ArrayList<>();
    
    /**
     * Constructor.
     */
    public PluginRegistryBean() {
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the repository
     */
    public PluginRegistryRepositoryBean getRepository() {
        return repository;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(PluginRegistryRepositoryBean repository) {
        this.repository = repository;
    }

    /**
     * @return the plugins
     */
    public List<PluginSummaryBean> getPlugins() {
        return plugins;
    }

    /**
     * @param plugins the plugins to set
     */
    public void setPlugins(List<PluginSummaryBean> plugins) {
        this.plugins = plugins;
    }
    
}
