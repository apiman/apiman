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
package io.apiman.common.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * A configuration that comes from a properties configuration file.  This
 * implementation will load the properties config file from wherever it can
 * reasonably find it.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfigFileConfiguration extends PropertiesConfiguration {

    /**
     * Returns a URL to a file with the given name inside the given directory.
     * @param directory
     */
    protected static URL findConfigUrlInDirectory(File directory, String configName) {
        if (directory.isDirectory()) {
            File cfile = new File(directory, configName);
            if (cfile.isFile()) {
                try {
                    return cfile.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public static ConfigFileConfiguration create(String configFileName, String customConfigPropertyName) {
        try {
            return new ConfigFileConfiguration(configFileName, customConfigPropertyName);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to find configuration file: " + configFileName, e); //$NON-NLS-1$
        }
    }

    /**
     * Discover the location of the apiman.properties (for example) file by checking
     * in various likely locations.
     * 
     * @param configFileName
     */
    private static URL discoverConfigFileUrl(String configFileName, String customConfigPropertyName) {
        URL rval = null;

        // User Defined
        ///////////////////////////////////
        String userConfig = System.getProperty(customConfigPropertyName);
        if (userConfig != null) {
            // Treat it as a URL
            try {
                rval = new URL(userConfig);
                return rval;
            } catch (Throwable t) {
            }
            // Treat it as a file
            try {
                File f = new File(userConfig);
                if (f.isFile()) {
                    rval = f.toURI().toURL();
                    return rval;
                }
            } catch (Throwable t) {
            }
            throw new RuntimeException("Apiman configuration provided at [" + userConfig + "] but could not be loaded."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        // Wildfly/EAP
        ///////////////////////////////////
        String jbossConfigDir = System.getProperty("jboss.server.config.dir"); //$NON-NLS-1$
        String jbossConfigUrl = System.getProperty("jboss.server.config.url"); //$NON-NLS-1$
        if (jbossConfigDir != null) {
            File dirFile = new File(jbossConfigDir);
            rval = findConfigUrlInDirectory(dirFile, configFileName);
            if (rval != null) {
                return rval;
            }
        }
        if (jbossConfigUrl != null) {
            File dirFile = new File(jbossConfigUrl);
            rval = findConfigUrlInDirectory(dirFile, configFileName);
            if (rval != null) {
                return rval;
            }
        }

        // Apache Tomcat
        ///////////////////////
        String tomcatHomeDir = System.getProperty("catalina.home"); //$NON-NLS-1$
        if (tomcatHomeDir != null) {
            File dirFile = new File(tomcatHomeDir, "conf"); //$NON-NLS-1$
            rval = findConfigUrlInDirectory(dirFile, configFileName);
            if (rval != null) {
                return rval;
            }
        }

        // If not found, use an empty file.
        ////////////////////////////////////////
        return ConfigFileConfiguration.class.getResource("empty.properties"); //$NON-NLS-1$
    }

    /**
     * Constructor.
     * @param configFileName
     * @param customConfigPropertyName
     * @throws ConfigurationException
     */
    private ConfigFileConfiguration(String configFileName, String customConfigPropertyName) throws ConfigurationException {
        super(discoverConfigFileUrl(configFileName, customConfigPropertyName));
    }

}
