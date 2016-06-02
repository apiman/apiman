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
package io.apiman.manager.api.core.plugin;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginClassLoader;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.plugin.PluginSpec;
import io.apiman.common.plugin.PluginUtils;
import io.apiman.manager.api.core.IPluginRegistry;
import io.apiman.manager.api.core.exceptions.InvalidPluginException;
import io.apiman.manager.api.core.i18n.Messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Serves as a common base class for concrete implementations of {@link IPluginRegistry}.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPluginRegistry implements IPluginRegistry {

    private File pluginsDir;
    private Map<PluginCoordinates, Plugin> pluginCache = new HashMap<>();
    private Object mutex = new Object();

    /**
     * Constructor.
     */
    public AbstractPluginRegistry() {
    }

    /**
     * Constructor.
     * @param pluginsDir the plugin's directory
     */
    public AbstractPluginRegistry(File pluginsDir) {
        this.setPluginsDir(pluginsDir);
    }

    /**
     * @see io.apiman.manager.api.core.IPluginRegistry#loadPlugin(io.apiman.common.plugin.PluginCoordinates)
     */
    @Override
    public Plugin loadPlugin(PluginCoordinates coordinates) throws InvalidPluginException {
        boolean isSnapshot = PluginUtils.isSnapshot(coordinates);
        synchronized (mutex) {
            if (pluginCache.containsKey(coordinates)) {
                Plugin cachedPlugin = pluginCache.get(coordinates);
                if (isSnapshot) {
                    pluginCache.remove(coordinates);
                    try { cachedPlugin.getLoader().close(); } catch (IOException e) { e.printStackTrace(); }
                } else {
                    return cachedPlugin;
                }
            }
            String pluginRelativePath = PluginUtils.getPluginRelativePath(coordinates);
            File pluginDir = new File(pluginsDir, pluginRelativePath);
            if (!pluginDir.exists()) {
                pluginDir.mkdirs();
            }
            File pluginFile = new File(pluginDir, "plugin." + coordinates.getType()); //$NON-NLS-1$

            // Clean up stale files in the case of a snapshot.
            if (pluginFile.exists() && isSnapshot) {
                try {
                    FileUtils.deleteDirectory(pluginFile.getParentFile());
                    pluginFile.getParentFile().mkdirs();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Doesn't exist (or it's a snapshot)?  Better download it.
            if (!pluginFile.exists()) {
                downloadPlugin(pluginFile, coordinates);
            }
            // Still doesn't exist?  That's a failure.
            if (!pluginFile.exists()) {
                throw new InvalidPluginException(Messages.i18n.format("AbstractPluginRegistry.PluginNotFound")); //$NON-NLS-1$
            }
            PluginClassLoader pluginClassLoader;
            try {
                pluginClassLoader = createPluginClassLoader(pluginFile);
            } catch (IOException e) {
                throw new InvalidPluginException(Messages.i18n.format("AbstractPluginRegistry.InvalidPlugin", pluginFile.getAbsolutePath()), e); //$NON-NLS-1$
            }
            URL specFile = pluginClassLoader.getResource(PluginUtils.PLUGIN_SPEC_PATH);
            if (specFile == null) {
                throw new InvalidPluginException(Messages.i18n.format("AbstractPluginRegistry.MissingPluginSpecFile", PluginUtils.PLUGIN_SPEC_PATH)); //$NON-NLS-1$
            }
            try {
                PluginSpec spec = PluginUtils.readPluginSpecFile(specFile);
                Plugin plugin = new Plugin(spec, coordinates, pluginClassLoader);
                pluginCache.put(coordinates, plugin);
                return plugin;
            } catch (Exception e) {
                throw new InvalidPluginException(Messages.i18n.format("AbstractPluginRegistry.FailedToReadSpecFile", PluginUtils.PLUGIN_SPEC_PATH), e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Creates a plugin classloader for the given plugin file.
     */
    protected PluginClassLoader createPluginClassLoader(final File pluginFile) throws IOException {
        return new PluginClassLoader(pluginFile, Thread.currentThread().getContextClassLoader()) {
            @Override
            protected File createWorkDir(File pluginArtifactFile) throws IOException {
                File workDir = new File(pluginFile.getParentFile(), ".work"); //$NON-NLS-1$
                workDir.mkdirs();
                return workDir;
            }
        };
    }

    /**
     * Downloads the plugin via its maven GAV information.  This will first look in the local
     * .m2 directory.  If the plugin is not found there, then it will try to download the
     * plugin from one of the configured remote maven repositories.
     */
    protected void downloadPlugin(File pluginFile, PluginCoordinates coordinates) {
        // First check the .m2 directory
        File m2Dir = PluginUtils.getUserM2Repository();
        if (m2Dir != null) {
            File artifactFile = PluginUtils.getM2Path(m2Dir, coordinates);
            if (artifactFile.isFile()) {
                try {
                    FileUtils.copyFile(artifactFile, pluginFile);
                    return;
                } catch (IOException e) {
                    artifactFile.delete();
                    throw new RuntimeException(e);
                }
            }
        }

        // Didn't find it in .m2, so try downloading it.
        Set<URI> repositories = getMavenRepositories();
        for (URI mavenRepoUrl : repositories) {
            if (downloadFromMavenRepo(pluginFile, coordinates, mavenRepoUrl)) {
                return;
            }
        }
    }

    /**
     * Tries to download the plugin from the given remote maven repository.
     */
    protected boolean downloadFromMavenRepo(File pluginFile, PluginCoordinates coordinates, URI mavenRepoUrl) {
        String artifactSubPath = PluginUtils.getMavenPath(coordinates);
        InputStream istream = null;
        OutputStream ostream = null;
        try {
            URL artifactUrl = new URL(mavenRepoUrl.toURL(), artifactSubPath);
            URLConnection connection = artifactUrl.openConnection();
            connection.connect();
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                if (httpConnection.getResponseCode() != 200) {
                    throw new IOException();
                }
            }

            istream = connection.getInputStream();
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

    /**
     * A valid set of remove maven repository URLs.
     */
    protected Set<URI> getMavenRepositories() {
        return PluginUtils.getDefaultMavenRepositories();
    }

    /**
     * @param pluginsDir the pluginsDir to set
     */
    public void setPluginsDir(File pluginsDir) {
        this.pluginsDir = pluginsDir;
        if (!this.pluginsDir.exists()) {
            this.pluginsDir.mkdirs();
        }
    }

}
