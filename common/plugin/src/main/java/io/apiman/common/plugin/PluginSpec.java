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

/**
 * The plugin spec.
 *
 * @author eric.wittmann@redhat.com
 */
public class PluginSpec {
    
    private double frameworkVersion = 1.0;
    private String name;
    private String description;
    private String version;

    /**
     * Constructor.
     */
    public PluginSpec() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the frameworkVersion
     */
    public double getFrameworkVersion() {
        return frameworkVersion;
    }

    /**
     * @param frameworkVersion the frameworkVersion to set
     */
    public void setFrameworkVersion(double frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }
}
