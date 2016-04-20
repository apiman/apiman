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
package io.apiman.gateway.engine.policy;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.beans.exceptions.ComponentNotFoundException;
import io.apiman.gateway.engine.beans.exceptions.InterceptorAlreadyRegisteredException;


/**
 * Context information provided to an executing policy.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public interface IPolicyContext {

    /**
     * Sets a conversation-scoped attribute, allowing policies to pass interesting
     * information to each other and to themselves.
     * @param name
     * @param value
     */
    void setAttribute(String name, Object value);

    /**
     * Fetches an attribute value from the conversation.
     * @param name
     * @param defaultValue
     * @return attribute if present, else default value
     */
    <T> T getAttribute(String name, T defaultValue);

    /**
     * Removes an attribute from the conversation.
     * @param name
     * @return whether attribute was removed
     */
    boolean removeAttribute(String name);

    /**
     * Gets a component by type.  Components are provided by the APIMan system for
     * use by policies during their execution.  Examples of components include the
     * Shared State Component and the HTTP Client Component.
     * @param componentClass
     * @return the component of type T
     * @throws ComponentNotFoundException
     */
    <T extends IComponent> T getComponent(Class<T> componentClass) throws ComponentNotFoundException;

    /**
     * Sets the {@link IConnectorInterceptor} to be used instead of the real connection.
     * @param connectorInterceptor the connector interceptor
     *
     * @throws InterceptorAlreadyRegisteredException
     */
    void setConnectorInterceptor(IConnectorInterceptor connectorInterceptor) throws InterceptorAlreadyRegisteredException;

    /**
     * @return {@link IConnectorInterceptor} set to the context or null otherwise
     */
    IConnectorInterceptor getConnectorInterceptor();

    /**
     * @param klazz the class
     * @return A logger associated with the conversation.
     */
    IApimanLogger getLogger(Class<?> klazz);
}
