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
package io.apiman.gateway.engine;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.gateway.engine.async.IAsyncResultHandler;


/**
 * A simple registry that can be used to lookup a plugin.  The plugin is loaded
 * asynchronously - a provided handler is invoked when the plugin has been loaded
 * and is ready to use.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IPluginRegistry {

    /**
     * Asynchronously loads a plugin.
     * @param coordinates
     */
    public void loadPlugin(PluginCoordinates coordinates, IAsyncResultHandler<Plugin> handler);

}
