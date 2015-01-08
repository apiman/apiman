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
import io.apiman.manager.api.core.IPluginRegistry;
import io.apiman.manager.api.core.exceptions.InvalidPluginException;
import io.apiman.manager.api.core.i18n.Messages;

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
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Servces as a common base class for concrete implementations of {@link IPluginRegistry}.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPluginRegistry implements IPluginRegistry {
    
    private static final String PLUGIN_SPEC_PATH = "META-INF/apiman/plugin.json"; //$NON-NLS-1$
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<URL> MAVEN_REPOSITORIES = new HashSet<>();
    static {
        try {
            MAVEN_REPOSITORIES.add(new URL("https://repo1.maven.org/maven2/")); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private File pluginsDir;
    private Map<PluginCoordinates, Plugin> pluginCache = new HashMap<>();
    
    /**
     * Constructor.
     */
    public AbstractPluginRegistry() {
    }
    
    /**
     * Constructor.
     * @param pluginsDir
     */
    public AbstractPluginRegistry(File pluginsDir) {
        this.setPluginsDir(pluginsDir);
    }

    /**
     * @see io.apiman.manager.api.core.IPluginRegistry#loadPlugin(io.apiman.common.plugin.PluginCoordinates)
     */
    @Override
    public Plugin loadPlugin(PluginCoordinates coordinates) throws InvalidPluginException {
        if (pluginCache.containsKey(coordinates)) {
            return pluginCache.get(coordinates);
        }
        
        StringBuilder pluginRelativePath = new StringBuilder();
        pluginRelativePath.append(coordinates.getGroupId());
        pluginRelativePath.append("/"); //$NON-NLS-1$
        pluginRelativePath.append(coordinates.getArtifactId());
        pluginRelativePath.append("/"); //$NON-NLS-1$
        pluginRelativePath.append(coordinates.getVersion());
        pluginRelativePath.append("/"); //$NON-NLS-1$
        pluginRelativePath.append(coordinates.getArtifactId());
        if (coordinates.getClassifier() != null) {
            pluginRelativePath.append("-").append(coordinates.getClassifier()); //$NON-NLS-1$
        }
        pluginRelativePath.append("."); //$NON-NLS-1$
        pluginRelativePath.append(coordinates.getType());
        
        File pluginDir = new File(pluginsDir, pluginRelativePath.toString());
        if (!pluginDir.exists()) {
            pluginDir.mkdirs();
        }
        File pluginFile = new File(pluginDir, "plugin." + coordinates.getType()); //$NON-NLS-1$
        // Doesn't exist?  Better download it
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
        URL specFile = pluginClassLoader.getResource(PLUGIN_SPEC_PATH);
        if (specFile == null) {
            throw new InvalidPluginException(Messages.i18n.format("AbstractPluginRegistry.MissingPluginSpecFile", PLUGIN_SPEC_PATH)); //$NON-NLS-1$
        }
        try {
            PluginSpec spec = (PluginSpec) mapper.reader(PluginSpec.class).readValue(specFile);
            Plugin plugin = new Plugin(spec, coordinates, pluginClassLoader);
            return plugin;
        } catch (Exception e) {
            throw new InvalidPluginException(Messages.i18n.format("AbstractPluginRegistry.FailedToReadSpecFile", PLUGIN_SPEC_PATH), e); //$NON-NLS-1$
        }
    }

    /**
     * Creates a plugin classloader for the given plugin file.
     * @param pluginFile
     * @throws IOException
     */
    protected PluginClassLoader createPluginClassLoader(final File pluginFile) throws IOException {
        PluginClassLoader cl = new PluginClassLoader(pluginFile) {
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
     * Downloads the plugin via its maven GAV information.  This will first look in the local
     * .m2 directory.  If the plugin is not found there, then it will try to download the 
     * plugin from one of the configured remote maven repositories.
     * @param pluginFile
     * @param coordinates
     */
    protected void downloadPlugin(File pluginFile, PluginCoordinates coordinates) {
        // First check the .m2 directory
        String userHome = System.getProperty("user.home"); //$NON-NLS-1$
        if (userHome != null) {
            File userHomeDir = new File(userHome);
            if (userHomeDir.isDirectory()) {
                File m2Dir = new File(userHome, ".m2/repository"); //$NON-NLS-1$
                if (m2Dir.isDirectory()) {
                    File artifactFile = getM2Path(m2Dir, coordinates);
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
            }
        }
        
        // Didn't find it in .m2, so try downloading it.
        Set<URL> repositories = getMavenRepositories();
        for (URL mavenRepoUrl : repositories) {
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
        String artifactSubPath = getMavenPath(coordinates);
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

    /**
     * Find the plugin artifact in the local .m2 directory.
     * @param m2Dir
     * @param coordinates
     */
    protected File getM2Path(File m2Dir, PluginCoordinates coordinates) {
        String artifactSubPath = getMavenPath(coordinates);
        File artifactFile = new File(m2Dir, artifactSubPath);
        return artifactFile;
    }
    
    /**
     * Calculates the relative path of the artifact from the given coordinates.
     * @param coordinates
     */
    protected String getMavenPath(PluginCoordinates coordinates) {
        StringBuilder artifactSubPath = new StringBuilder();
        artifactSubPath.append(coordinates.getGroupId().replace('.', '/'));
        artifactSubPath.append('/');
        artifactSubPath.append(coordinates.getArtifactId());
        artifactSubPath.append('/');
        artifactSubPath.append(coordinates.getVersion());
        artifactSubPath.append('/');
        artifactSubPath.append(coordinates.getArtifactId());
        artifactSubPath.append('-');
        artifactSubPath.append(coordinates.getVersion());
        if (coordinates.getClassifier() != null) {
            artifactSubPath.append('-');
            artifactSubPath.append(coordinates.getClassifier());
        }
        artifactSubPath.append('.');
        artifactSubPath.append(coordinates.getType());
        return artifactSubPath.toString();
    }

    /**
     * A valid set of remove maven repository URLs.
     */
    protected Set<URL> getMavenRepositories() {
        return MAVEN_REPOSITORIES;
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
