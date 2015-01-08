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
package io.apiman.manager.api.core;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.manager.api.core.exceptions.InvalidPluginException;

/**
 * The plugin registry used by the API Manager.  The plugin registry provides a way to 
 * download plugins and crack them open to discover features they provide.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IPluginRegistry {
    
    /**
     * Loads a plugin with the given coordinates.
     * @param coordinates
     */
    public Plugin loadPlugin(PluginCoordinates coordinates) throws InvalidPluginException;

}
