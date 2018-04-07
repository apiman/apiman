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
package io.apiman.gateway.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

/**
 * Accessor - used to get the current version of the engine.
 *
 * @author eric.wittmann@redhat.com
 */
public class Version {

    private static final Version instance = new Version();
    public static final Version get() {
        return instance;
    }

    private Properties allProperties;
    private String versionString;
    private String versionDate;
    private String vcsDescribe;

    /**
     * Constructor.
     */
    private Version() {
        load();
    }

    /**
     * Loads the version info from version.properties.
     */
    @SuppressWarnings("nls")
    private void load() {
        URL url = Version.class.getResource("version.properties");
        if (url == null) {
            this.versionString = "Unknown";
            this.versionDate = new Date().toString();
        } else {
            allProperties = new Properties();
            try(InputStream is = url.openStream()){
                allProperties.load(is);
                this.versionString = allProperties.getProperty("version", "Unknown");
                this.versionDate = allProperties.getProperty("date", new Date().toString());
                this.vcsDescribe = allProperties.getProperty("git.commit.id.describe", "Non-Git Build");
            } catch (IOException e) {
                throw new RuntimeException(e);
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

    /**
     * @return the verbose version output
     */
    public String getVerbose() {
        return allProperties.toString();
    }

    /**
     * The version control system (VCS) commit description. Particularly useful for identifying which
     * code point a SNAPSHOT was using. Presently git.
     *
     * @return the VCS commit description
     */
    public String getVcsCommitDescription() {
        return vcsDescribe;
    }

}
