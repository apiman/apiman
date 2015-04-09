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
package io.apiman.gateway.engine.components;

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.impl.CachedResponse;

/**
 * A Component that allows policies to share data across invocations and potentially
 * across nodes (depending on the implementation). The data managed will be related to
 * requests and/or responses such as {@link CachedResponse} instances
 *
 * It is up to the implementation of this component to determine how transactional
 * it might be (there are time vs. accuracy tradeoffs to be made here).  Users
 * of API Management will need to ensure they use an appropriate implementation
 * of this component based on the data integrity/accuracy guarantees they require.
 * 
 * All operations in this component are assumed to be asyncrhonous, so a handler
 * must be provided if the policy implementation needs the operation to finish
 * prior to moving on.
 * 
 * @author rubenrm1@gmail.com
 */
public interface IDataStoreComponent extends IComponent {

    /**
     * Checks whether the requested property exists in the Data Store
     * @param namespace
     * @param propertyName
     * @return true if has property; else false.
     */
    <T> boolean hasProperty(String namespace, String propertyName); 
    
    /**
     * Gets the value of a single property stored in the shared state 
     * environment.  Null is returned if the property is not set.
     * @param namespace
     * @param propertyName
     * @param defaultValue
     * @param handler
     */
     <T> void getProperty(String namespace, String propertyName, T defaultValue, IAsyncResultHandler<T> handler);
    
    /**
     * Sets a single property in the shared state environment, returning
     * the previous value of the property or null if it was not previously set.
     * @param namespace
     * @param propertyName
     * @param value
     * @param handler
     */
    <T> void setProperty(String namespace, String propertyName, T value, IAsyncResultHandler<T> handler);
    
    /**
     * Sets a single property in the shared state environment for the given period of time, 
     * returning the previous value of the property or null if it was not previously set.
     * @param namespace
     * @param propertyName
     * @param value
     * @param expiration time for the property to be kept in the DataStore before being considered as expired
     * @param handler
     */
    <T> void setProperty(String namespace, String propertyName, T value, Long expiration, IAsyncResultHandler<T> handler);
    
    /**
     * Clears a property from the shared state environment, returning the previous 
     * value of the property or null if it was not previously set.
     * @param namespace
     * @param propertyName
     * @param handler
     */
    <T> void clearProperty(String namespace, String propertyName, IAsyncResultHandler<T> handler);
    
}
