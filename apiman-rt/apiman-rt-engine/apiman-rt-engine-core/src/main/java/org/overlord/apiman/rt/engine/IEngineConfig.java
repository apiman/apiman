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
package org.overlord.apiman.rt.engine;

import java.util.Map;

import org.overlord.apiman.rt.engine.policy.IPolicyFactory;

/**
 * Engine configuration - used by the engine factory when creating the engine.  :)
 *
 * @author eric.wittmann@redhat.com
 */
public interface IEngineConfig {

    /**
     * @return the class to use as the {@link IRegistry}
     */
    public Class<IRegistry> getRegistryClass();

    /**
     * @return all properties to be passed to the registry
     */
    public Map<String, String> getRegistryConfig();

    /**
     * @return the class to use as the {@link IConnectorFactory}
     */
    public Class<IConnectorFactory> getConnectorFactoryClass();

    /**
     * @return all properties to be passed to the factory
     */
    public Map<String, String> getConnectorFactoryConfig();

    /**
     * @return the class to use as the {@link IPolicyFactory}
     */
    public Class<IPolicyFactory> getPolicyFactoryClass();

    /**
     * @return all properties to be passed to the factory
     */
    public Map<String, String> getPolicyFactoryConfig();

    /**
     * @return the class to use for the given component
     */
    public <T extends IComponent> Class<T> getComponentClass(Class<T> componentType);

    /**
     * @return all properties to be passed to the factory
     */
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType);

    /**
     * @return the configured server port
     */
    public int getServerPort();
}
