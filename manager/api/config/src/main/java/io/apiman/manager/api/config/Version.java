/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.manager.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.IOUtils;

/**
 * Accessor - used to get the current version of the app.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class Version {
    
    private String versionString;
    private String versionDate;
    
    /**
     * Constructor.
     */
    public Version() {
    }
    
    @PostConstruct
    public void postConstruct() {
        load();
    }

    /**
     * Loads the version info from version.properties.
     */
    private void load() {
        URL url = Version.class.getResource("version.properties"); //$NON-NLS-1$
        if (url == null) {
            this.versionString = "Unknown"; //$NON-NLS-1$
            this.versionDate = new Date().toString();
        } else {
            InputStream is = null;
            Properties props = new Properties();
            try {
                is = url.openStream();
                props.load(is);
                this.versionString = props.getProperty("version", "Unknown"); //$NON-NLS-1$ //$NON-NLS-2$
                this.versionDate = props.getProperty("date", new Date().toString()); //$NON-NLS-1$
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    /**
     * @return the versionString
     */
    public String getVersionString() {
        return versionString;
    }

    /**
     * @return the versionDate
     */
    public String getVersionDate() {
        return versionDate;
    }

}
