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

    public static ConfigFileConfiguration create(String configFileName) {
        try {
            return new ConfigFileConfiguration(configFileName);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to find configuration file: " + configFileName, e); //$NON-NLS-1$
        }
    }

    private static URL discoverConfigFileUrl(String configFileName) {
        // Wildfly/EAP takes priority
        ///////////////////////////////////
        String jbossConfigDir = System.getProperty("jboss.server.config.dir"); //$NON-NLS-1$
        String jbossConfigUrl = System.getProperty("jboss.server.config.url"); //$NON-NLS-1$
        URL rval = null;
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

        // Next try tomcat
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
     * @throws ConfigurationException
     */
    private ConfigFileConfiguration(String configFileName) throws ConfigurationException {
        super(discoverConfigFileUrl(configFileName));
    }

}
