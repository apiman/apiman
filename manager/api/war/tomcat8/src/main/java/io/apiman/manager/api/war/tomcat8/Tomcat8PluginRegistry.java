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
package io.apiman.manager.api.war.tomcat8;

import io.apiman.manager.api.core.plugin.AbstractPluginRegistry;
import io.apiman.manager.api.war.WarApiManagerConfig;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * A tomcat 8 version of the plugin registry.  This subclass exists in order
 * to properly configure the data directory that should be used.  In this case
 * the data directory is $CATALINA_HOME/apiman/plugins
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class Tomcat8PluginRegistry extends AbstractPluginRegistry {

    @Inject
    private WarApiManagerConfig config;

    private Set<URI> mavenRepos = null;

    /**
     * Creates the directory to use for the plugin registry.  The location of
     * the plugin registry is in the tomcat data directory.
     */
    private static File getPluginDir() {
        String dataDirPath = System.getProperty("catalina.home"); //$NON-NLS-1$
        File dataDir = new File(dataDirPath, "data"); //$NON-NLS-1$
        if (!dataDir.getParentFile().isDirectory()) {
            throw new RuntimeException("Failed to find Tomcat home at: " + dataDirPath); //$NON-NLS-1$
        }
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        File pluginsDir = new File(dataDir, "apiman/plugins"); //$NON-NLS-1$
        return pluginsDir;
    }

    /**
     * Constructor.
     */
    public Tomcat8PluginRegistry() {
        super(getPluginDir());
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
