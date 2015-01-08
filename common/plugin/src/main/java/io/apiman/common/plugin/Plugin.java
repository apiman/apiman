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
 * An apiman plugin.  This represents a plugin that has been downloaded to some local 
 * registry and successfully loaded.  A plugin must contain at least the following
 * file:
 * 
 * META-INF/apiman/plugin.json
 * 
 * It may also contain java classes, resources, and other configuration files.
 * 
 * For example, multiple policy definition files may exist here:
 * 
 * META-INF/apiman/policyDefs/*.json
 *
 * @author eric.wittmann@redhat.com
 */
public class Plugin {
    
    private PluginSpec spec;
    private PluginCoordinates coordinates;
    private PluginClassLoader loader;
    
    /**
     * Constructor.
     * @param spec
     * @param coordinates
     * @param loader
     */
    public Plugin(PluginSpec spec, PluginCoordinates coordinates, PluginClassLoader loader) {
        setSpec(spec);
        setCoordinates(coordinates);
        setLoader(loader);
    }

    /**
     * @return the name
     */
    public String getName() {
        return spec.getName();
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return spec.getDescription();
    }

    /**
     * @return the coordinates
     */
    public PluginCoordinates getCoordinates() {
        return coordinates;
    }

    /**
     * @param coordinates the coordinates to set
     */
    protected void setCoordinates(PluginCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * @return the loader
     */
    public PluginClassLoader getLoader() {
        return loader;
    }

    /**
     * @param loader the loader to set
     */
    protected void setLoader(PluginClassLoader loader) {
        this.loader = loader;
    }

    /**
     * @return the spec
     */
    public PluginSpec getSpec() {
        return spec;
    }

    /**
     * @param spec the spec to set
     */
    public void setSpec(PluginSpec spec) {
        this.spec = spec;
    }

}
