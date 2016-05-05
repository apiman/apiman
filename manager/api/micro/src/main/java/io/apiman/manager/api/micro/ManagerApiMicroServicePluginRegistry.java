/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.micro;

import io.apiman.manager.api.core.plugin.AbstractPluginRegistry;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * A micro service version of the plugin registry.  This subclass exists in order
 * to properly configure the data directory that should be used.  The location of
 * the plugin data directory must be provided via a system property.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ManagerApiMicroServicePluginRegistry extends AbstractPluginRegistry {

    @Inject
    private ManagerApiMicroServiceConfig config;

    private Set<URI> mavenRepos = null;

    /**
     * Constructor.
     */
    public ManagerApiMicroServicePluginRegistry() {
    }

    @PostConstruct
    protected void postConstruct() {
        setPluginsDir(config.getPluginDirectory());
    }

    /**
     * @see io.apiman.manager.api.core.plugin.AbstractPluginRegistry#getMavenRepositories()
     */
    @Override
    protected Set<URI> getMavenRepositories() {
        if (mavenRepos == null) {
            mavenRepos = loadMavenRepositories();
        }
        return mavenRepos;
    }

    /**
     * @return the maven repositories to use when downloading plugins
     */
    protected Set<URI> loadMavenRepositories() {
        Set<URI> repos = new HashSet<>();
        repos.addAll(super.getMavenRepositories());
        repos.addAll(config.getPluginRepositories());
        return repos;
    }

}
