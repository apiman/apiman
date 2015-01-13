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
package io.apiman.common.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Some generally useful static methods.
 *
 * @author eric.wittmann@redhat.com
 */
public class PluginUtils {

    public static final String PLUGIN_SPEC_PATH = "META-INF/apiman/plugin.json"; //$NON-NLS-1$
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<URL> MAVEN_REPOSITORIES = new HashSet<>();
    static {
        try {
            MAVEN_REPOSITORIES.add(new URL("https://repo1.maven.org/maven2/")); //$NON-NLS-1$
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @return a set of default maven repositories to search for plugins
     */
    public static Set<URL> getDefaultMavenRepositories() {
        return MAVEN_REPOSITORIES;
    }
    
    /**
     * Returns the relative path (within a plugin registry's plugins directory) where
     * the plugin with the given coordinates can be found.  For example, if the plugin
     * coordinate are "io.apiman.sample:sample-plugin:1.0.1:classifier:war" then the 
     * path will be:
     * 
     * io.apiman.sample/sample-plugin/1.0.1/sample-plugin-classifier.war/
     * 
     * @param coordinates
     */
    public static String getPluginRelativePath(PluginCoordinates coordinates) {
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
        return pluginRelativePath.toString();
    }
    
    /**
     * Reads a plugin spec file and returns a {@link PluginSpec}.
     * @param pluginSpec
     * @throws Exception
     */
    public static PluginSpec readPluginSpecFile(URL pluginSpec) throws Exception {
        PluginSpec spec = (PluginSpec) mapper.reader(PluginSpec.class).readValue(pluginSpec);
        return spec;
    }

    /**
     * Gets the user's local m2 directory or null if not found.
     */
    public static File getUserM2Repository() {
        String userHome = System.getProperty("user.home"); //$NON-NLS-1$
        if (userHome != null) {
            File userHomeDir = new File(userHome);
            if (userHomeDir.isDirectory()) {
                File m2Dir = new File(userHome, ".m2/repository"); //$NON-NLS-1$
                if (m2Dir.isDirectory()) {
                    return m2Dir;
                }
            }
        }
        return null;
    }

    /**
     * Find the plugin artifact in the local .m2 directory.
     * @param m2Dir
     * @param coordinates
     */
    public static File getM2Path(File m2Dir, PluginCoordinates coordinates) {
        String artifactSubPath = getMavenPath(coordinates);
        File artifactFile = new File(m2Dir, artifactSubPath);
        return artifactFile;
    }
    
    /**
     * Calculates the relative path of the artifact from the given coordinates.
     * @param coordinates
     */
    public static String getMavenPath(PluginCoordinates coordinates) {
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

}
