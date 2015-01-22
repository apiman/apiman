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
package io.apiman.gateway.engine.impl;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginClassLoader;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.plugin.PluginSpec;
import io.apiman.common.plugin.PluginUtils;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.i18n.Messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * A simple plugin registry that stores plugins in a temporary location.  This
 * implementation shouldn't really be used except for testing and perhaps getting
 * started with embedding the policy engine.  The reasons to not use this 
 * implementation include:
 * 
 * 1) not truly asynchronous (not good if embedding in a true async platform)
 * 2) stores downloaded plugins in java.io.tmp
 * 3) does not remember where it put plugins, so will re-download them often
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultPluginRegistry implements IPluginRegistry {
    
    private static File createTempPluginsDir() {
        // TODO log a warning here
        try {
            @SuppressWarnings("nls")
            File tempDir = File.createTempFile("_apiman", "plugins");
            tempDir.delete();
            tempDir.mkdir();
            return tempDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static File getConfiguredPluginsDir(Map<String, String> configMap) {
        String pluginsDirPath = configMap.get("pluginsDir"); //$NON-NLS-1$
        if (pluginsDirPath != null) {
            File file = new File(pluginsDirPath).getAbsoluteFile();
            if (!file.exists()) {
                file.mkdirs();
            } else if (file.isFile()) {
                throw new RuntimeException("Invalid plugins directory: " + file.toString()); //$NON-NLS-1$
            }
            return file;
        } else {
            return createTempPluginsDir();
        }
    }
    private static Set<URL> getConfiguredPluginRepositories(Map<String, String> configMap) {
        Set<URL> rval = new HashSet<URL>();
        rval.addAll(PluginUtils.getDefaultMavenRepositories());
        String repositories = configMap.get("pluginRepositories"); //$NON-NLS-1$
        if (repositories != null) {
            String[] split = repositories.split(","); //$NON-NLS-1$
            for (String repository : split) {
                try {
                    String trimmedRepo = repository.trim();
                    if (!trimmedRepo.isEmpty()) {
                        rval.add(new URL(trimmedRepo));
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return rval;
    }
    
    private File pluginsDir;
    private Map<PluginCoordinates, Plugin> pluginCache = new HashMap<>();
    private Set<URL> pluginRepositories;

    /**
     * Constructor.
     */
    public DefaultPluginRegistry() {
        this(createTempPluginsDir(), PluginUtils.getDefaultMavenRepositories());
    }
    
    /**
     * Constructor.
     * @param configMap
     */
    public DefaultPluginRegistry(Map<String, String> configMap) {
        this(getConfiguredPluginsDir(configMap), getConfiguredPluginRepositories(configMap));
    }

    /**
     * Constructor.
     * @param pluginsDir
     */
    public DefaultPluginRegistry(File pluginsDir, Set<URL> pluginRepositories) {
        this.pluginsDir = pluginsDir;
        this.pluginRepositories = pluginRepositories;
    }

    /**
     * @see io.apiman.gateway.engine.IPluginRegistry#loadPlugin(io.apiman.common.plugin.PluginCoordinates, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void loadPlugin(PluginCoordinates coordinates, IAsyncResultHandler<Plugin> handler) {
        // 0) check if the plugin is in the cache - if found return it immediately
        // 1) check to see if the plugin is located in the pluginsDir
        // 2) if not, try to "download" it from the local .m2 directory
        // 3) if not found, try to download it from available remote repositories
        // 4) if still not found, report an error
        // 5) if found, load the plugin, cache it, and return it
        Plugin plugin = null;
        Throwable error = null;
        synchronized (pluginCache) {
            if (pluginCache.containsKey(coordinates)) {
                plugin = pluginCache.get(coordinates);
            } else {
                String pluginRelativePath = PluginUtils.getPluginRelativePath(coordinates);
                File pluginDir = new File(pluginsDir, pluginRelativePath);
                if (!pluginDir.exists()) {
                    pluginDir.mkdirs();
                }
                File pluginFile = new File(pluginDir, "plugin." + coordinates.getType()); //$NON-NLS-1$
                // Doesn't exist?  Try to copy it from <user>/.m2/repository
                if (!pluginFile.exists()) {
                    copyFromM2(pluginFile, coordinates);
                }
                // Doesn't exist?  Better download it
                if (!pluginFile.exists()) {
                    downloadPlugin(pluginFile, coordinates);
                }
                // Still doesn't exist?  That's a failure.
                if (!pluginFile.exists()) {
                    error = new Exception(Messages.i18n.format("DefaultPluginRegistry.PluginNotFound")); //$NON-NLS-1$
                } else {
                    try {
                        PluginClassLoader pluginClassLoader = createPluginClassLoader(pluginFile);
                        URL specFile = pluginClassLoader.getResource(PluginUtils.PLUGIN_SPEC_PATH);
                        if (specFile == null) {
                            error = new Exception(Messages.i18n.format("DefaultPluginRegistry.MissingPluginSpecFile", PluginUtils.PLUGIN_SPEC_PATH)); //$NON-NLS-1$
                        } else {
                            PluginSpec spec = PluginUtils.readPluginSpecFile(specFile);
                            plugin = new Plugin(spec, coordinates, pluginClassLoader);
                            pluginCache.put(coordinates, plugin);
                        }
                    } catch (Exception e) {
                        error = new Exception(Messages.i18n.format("DefaultPluginRegistry.InvalidPlugin", pluginFile.getAbsolutePath()), e); //$NON-NLS-1$
                    }
                }
            }
        }
        if (error != null) {
            handler.handle(AsyncResultImpl.<Plugin>create(error));
        } else if (plugin != null) {
            handler.handle(AsyncResultImpl.create(plugin));
        } else {
            handler.handle(AsyncResultImpl.<Plugin>create(new Exception("Failed to load plugin (unknown reason)."))); //$NON-NLS-1$
        }
    }

    /**
     * Creates a plugin classloader for the given plugin file.
     * @param pluginFile
     * @throws IOException
     */
    protected PluginClassLoader createPluginClassLoader(final File pluginFile) throws IOException {
        PluginClassLoader cl = new PluginClassLoader(pluginFile, Thread.currentThread().getContextClassLoader()) {
            @Override
            protected File createWorkDir(File pluginArtifactFile) throws IOException {
                File workDir = new File(pluginFile.getParentFile(), ".work"); //$NON-NLS-1$
                workDir.mkdirs();
                return workDir;
            }
        };
        return cl;
    }

    /**
     * Try to copy the plugin from the current user's .m2 directory.  In production this should 
     * typically not do anything.
     * @param pluginFile
     * @param coordinates
     */
    protected void copyFromM2(File pluginFile, PluginCoordinates coordinates) {
        File m2Dir = PluginUtils.getUserM2Repository();
        String m2Override = System.getProperty("apiman.gateway.m2-repository-path"); //$NON-NLS-1$
        if (m2Override != null) {
            m2Dir = new File(m2Override).getAbsoluteFile();
        }
        if (m2Dir != null) {
            File artifactFile = PluginUtils.getM2Path(m2Dir, coordinates);
            if (artifactFile.isFile()) {
                try {
                    FileUtils.copyFile(artifactFile, pluginFile);
                    return;
                } catch (IOException e) {
                    artifactFile.delete();
                }
            }
        }
    }

    /**
     * Downloads the plugin via its maven GAV information.  This will first look in the local
     * .m2 directory.  If the plugin is not found there, then it will try to download the 
     * plugin from one of the configured remote maven repositories.
     * @param pluginFile
     * @param coordinates
     */
    protected void downloadPlugin(File pluginFile, PluginCoordinates coordinates) {
        // Didn't find it in .m2, so try downloading it.
        for (URL mavenRepoUrl : pluginRepositories) {
            if (downloadFromMavenRepo(pluginFile, coordinates, mavenRepoUrl)) {
                return;
            }
        }
    }

    /**
     * Tries to download the plugin from the given remote maven repository.
     * @param pluginFile
     * @param coordinates
     * @param mavenRepoUrl 
     */
    protected boolean downloadFromMavenRepo(File pluginFile, PluginCoordinates coordinates, URL mavenRepoUrl) {
        String artifactSubPath = PluginUtils.getMavenPath(coordinates);
        InputStream istream = null;
        OutputStream ostream = null;
        try {
            URL artifactUrl = new URL(mavenRepoUrl, artifactSubPath);
            istream = artifactUrl.openStream();
            ostream = new FileOutputStream(pluginFile);
            IOUtils.copy(istream, ostream);
            ostream.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            IOUtils.closeQuietly(istream);
            IOUtils.closeQuietly(ostream);
        }
    }
    
}
