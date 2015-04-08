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
package io.apiman.manager.api.war.wildfly8;

import io.apiman.manager.api.core.plugin.AbstractPluginRegistry;
import io.apiman.manager.api.war.WarApiManagerConfig;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * A wildfly 8 version of the plugin registry.  This subclass exists in order
 * to properly configure the data directory that should be used.  In this case
 * the data directory is $WILDFLY/standalone/data/apiman/plugins
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class Wildfly8PluginRegistry extends AbstractPluginRegistry {

    @Inject
    private WarApiManagerConfig config;
    
    private Set<URL> mavenRepos = null;
    
    /**
     * Creates the directory to use for the plugin registry.  The location of
     * the plugin registry is in the Wildfly data directory.
     */
    private static File getPluginDir() {
        String dataDirPath = System.getProperty("jboss.server.data.dir"); //$NON-NLS-1$
        File dataDir = new File(dataDirPath);
        if (!dataDir.isDirectory()) {
            throw new RuntimeException("Failed to find WildFly data directory at: " + dataDirPath); //$NON-NLS-1$
        }
        File pluginsDir = new File(dataDir, "apiman/plugins"); //$NON-NLS-1$
        return pluginsDir;
    }

    /**
     * Constructor.
     * @param pluginsDir
     */
    public Wildfly8PluginRegistry() {
        super(getPluginDir());
    }

    /**
     * @see io.apiman.manager.api.core.plugin.AbstractPluginRegistry#getMavenRepositories()
     */
    @Override
    protected Set<URL> getMavenRepositories() {
        if (mavenRepos == null) {
            mavenRepos = loadMavenRepositories();
        }
        return mavenRepos;
    }

    /**
     * @return the maven repositories to use when downloading plugins
     */
    protected Set<URL> loadMavenRepositories() {
        Set<URL> repos = new HashSet<>();
        repos.addAll(super.getMavenRepositories());
        repos.addAll(config.getPluginRepositories());
        return repos;
    }
    
}
