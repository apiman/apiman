/*
 * Copyright 2013 JBoss Inc
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
import io.apiman.common.logging.IDelegateFactory;
import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.beans.exceptions.ComponentNotFoundException;
import io.apiman.gateway.engine.beans.exceptions.InterceptorAlreadyRegisteredException;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of a {@link IPolicyContext}.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyContextImpl implements IPolicyContext {

    private final IComponentRegistry componentRegistry;
    private final Map<String, Object> conversation = new HashMap<>();
    private final IDelegateFactory logFactory;
    // Using String instead of Class to avoid any accidental memory leak issues.
    private final static Map<String, IApimanLogger> loggers = new HashMap<>();
    private IConnectorInterceptor connectorInterceptor;

    /**
     * Constructor.
     * @param componentRegistry the component registry
     * @param logFactory the log factory
     */
    public PolicyContextImpl(IComponentRegistry componentRegistry, IDelegateFactory logFactory) {
        this.componentRegistry = componentRegistry;
        this.logFactory = logFactory;
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicyContext#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) {
        conversation.put(name, value);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicyContext#getAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public <T> T getAttribute(String name, T defaultValue) {
        @SuppressWarnings("unchecked")
        T value = (T) conversation.get(name);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicyContext#removeAttribute(java.lang.String)
     */
    @Override
    public boolean removeAttribute(String name) {
        return conversation.remove(name) != null;
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicyContext#getComponent(java.lang.Class)
     */
    @Override
    public <T extends IComponent> T getComponent(Class<T> componentClass) throws ComponentNotFoundException {
        return this.componentRegistry.getComponent(componentClass);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicyContext#setConnectorInterceptor(IConnectorInterceptor)
     */
    @Override
    public void setConnectorInterceptor(IConnectorInterceptor connectorInterceptor) throws InterceptorAlreadyRegisteredException {
        if (this.connectorInterceptor != null) {
            throw new InterceptorAlreadyRegisteredException(connectorInterceptor.getClass());
        }
        this.connectorInterceptor = connectorInterceptor;
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicyContext#getConnectorInterceptor()
     */
    @Override
    public IConnectorInterceptor getConnectorInterceptor() {
        return connectorInterceptor;
    }

    @Override
    public IApimanLogger getLogger(Class<?> klazz) {
        if (loggers.containsKey(klazz.getCanonicalName())) {
            return loggers.get(klazz.getCanonicalName());
        } else {
            IApimanLogger logger = logFactory.createLogger(klazz);
            loggers.put(klazz.getCanonicalName(), logger);
            return logger;
        }
    }

}
