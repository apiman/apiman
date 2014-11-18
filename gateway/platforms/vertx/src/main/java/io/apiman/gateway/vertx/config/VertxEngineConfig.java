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
package io.apiman.gateway.vertx.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyFactory;

import org.vertx.java.core.json.JsonObject;

/**
 * Engine configuration, read simplistically from Vert'x JSON config.
 *
 * @see "http://vertx.io/manual.html#using-vertx-from-the-command-line"
 * @author Marc Savy <msavy@redhat.com>
 */
public class VertxEngineConfig implements IEngineConfig {

    public static final String APIMAN_RT_REGISTRY_PREFIX = "registry"; //$NON-NLS-1$
    public static final String APIMAN_RT_CONNECTOR_FACTORY_PREFIX = "connector-factory"; //$NON-NLS-1$
    public static final String APIMAN_RT_POLICY_FACTORY_PREFIX = "policy-factory"; //$NON-NLS-1$

    public static final String APIMAN_RT_COMPONENT_PREFIX = "components"; //$NON-NLS-1$

    private static final String APIMAN_RT_AUTH_PREFIX = "auth"; //$NON-NLS-1$

    public static final String APIMAN_RT_GATEWAY_SERVER_PORT = "server-port"; //$NON-NLS-1$

    public static final String APIMAN_RT_CONFIG = "config"; //$NON-NLS-1$
    public static final String APIMAN_RT_CLASS = "class"; //$NON-NLS-1$

    public static final String APIMAN_RT_EP_SERVICE_REQUEST = ".apiman.gateway.service.request"; //$NON-NLS-1$
    public static final String APIMAN_RT_EP_SERVICE_RESPONSE = ".apiman.gateway.service.response"; //$NON-NLS-1$

    public static final String APIMAN_RT_READY_SUFFIX = ".ready"; //$NON-NLS-1$
    public static final String APIMAN_RT_HEAD_SUFFIX = ".head"; //$NON-NLS-1$
    public static final String APIMAN_RT_BODY_SUFFIX = ".body"; //$NON-NLS-1$
    public static final String APIMAN_RT_END_SUFFIX = ".end"; //$NON-NLS-1$
    public static final String APIMAN_RT_ERROR_SUFFIX = ".error"; //$NON-NLS-1$
    public static final String APIMAN_RT_FAILURE_SUFFIX = ".failure"; //$NON-NLS-1$

    public static final String APIMAN_RT_GATEWAY_ROUTES = "routes"; //$NON-NLS-1$
    public static final String APIMAN_RT_EP_GATEWAY_REG_POLICY = "apiman.gateway.register.policy"; //$NON-NLS-1$

    public static final String APIMAN_API_APPLICATIONS_REGISTER = ".apiman.api.applications.register"; //$NON-NLS-1$
    public static final String APIMAN_API_APPLICATIONS_DELETE = ".apiman.api.applications.delete"; //$NON-NLS-1$
    public static final String APIMAN_API_SERVICES_REGISTER = ".apiman.api.services.register"; //$NON-NLS-1$
    public static final String APIMAN_API_SERVICES_DELETE = ".apiman.api.services.delete"; //$NON-NLS-1$
    public static final String APIMAN_API_SUBSCRIBE = "apiman.api.subscribe"; //$NON-NLS-1$

    private static final String APIMAN_RT_AUTH_BASIC = "file-basic";
    private static final String APIMAN_RT_AUTH_ENABLED = "authenticated";
    private static final String APIMAN_RT_AUTH_REALM = "realm";
    private static final String APIMAN_RT_HOSTNAME = "hostname";
    private static final String APIMAN_RT_ENDPOINT = "endpoint";

    private RouteMapper routeMap;
    private JsonObject config;

    public VertxEngineConfig(JsonObject config) {
        this.config = config;

        if(config.getObject(APIMAN_RT_GATEWAY_ROUTES) != null) {
            routeMap = new RouteMapper(config.getObject(APIMAN_RT_GATEWAY_ROUTES));
        } else {
            routeMap = new RouteMapper();
        }
    }

    public JsonObject getConfig() {
        return config;
    }

    @Override
    public Class<? extends IRegistry> getRegistryClass() {
        return loadConfigClass(getClassname(config, APIMAN_RT_REGISTRY_PREFIX),
                IRegistry.class);
    }

    @Override
    public Map<String, String> getRegistryConfig() {
        return toFlatStringMap(getConfig(config, APIMAN_RT_REGISTRY_PREFIX));
    }

    @Override
    public Class<? extends IConnectorFactory> getConnectorFactoryClass() {
        return loadConfigClass(getClassname(config, APIMAN_RT_CONNECTOR_FACTORY_PREFIX),
                IConnectorFactory.class);
    }

    @Override
    public Map<String, String> getConnectorFactoryConfig() {
        return toFlatStringMap(getConfig(config, APIMAN_RT_CONNECTOR_FACTORY_PREFIX));
    }

    @Override
    public Class<? extends IPolicyFactory> getPolicyFactoryClass() {
        return loadConfigClass(getClassname(config, APIMAN_RT_POLICY_FACTORY_PREFIX),
                IPolicyFactory.class);
    }

    @Override
    public Map<String, String> getPolicyFactoryConfig() {
        return toFlatStringMap(getConfig(config, APIMAN_RT_POLICY_FACTORY_PREFIX));
    }

    @Override
    public <T extends IComponent> Class<T> getComponentClass(Class<T> componentType) {
        String className = config.getObject(APIMAN_RT_COMPONENT_PREFIX).
                getObject(componentType.getSimpleName()).
                getString(APIMAN_RT_CLASS);

        return loadConfigClass(className, componentType);
    }

    @Override
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType) {
        JsonObject componentConfig = config.getObject(APIMAN_RT_COMPONENT_PREFIX).
                getObject(componentType.getSimpleName()).
                getObject(APIMAN_RT_CONFIG);

        return toFlatStringMap(componentConfig);
    }


    public Boolean isAuthenticationEnabled() {
        return boolConfigWithDefault(APIMAN_RT_AUTH_ENABLED, false);
    }

    public String getRealm() {
        return stringConfigWithDefault(APIMAN_RT_AUTH_REALM, "apiman-realm");
    }

    public RouteMapper getRouteMap() {
        return routeMap;
    }
    
    public String hostname() {
        return stringConfigWithDefault(APIMAN_RT_HOSTNAME, "localhost");
    }
    
    public String getEndpoint() {
        return stringConfigWithDefault(APIMAN_RT_ENDPOINT, "localhost");   
    }

    public Map<String, String> loadFileBasicAuth() {
        JsonObject pairs = config.getObject(APIMAN_RT_AUTH_PREFIX).getObject(APIMAN_RT_AUTH_BASIC);

        Map<String, String> map = new HashMap<>();

        for (String username : pairs.getFieldNames()) {
            map.put(username, pairs.getString(username));
        }

        return map;
    }

    protected Map<String, String> toFlatStringMap(JsonObject jsonObject) {
        Map<String, String> outMap = new HashMap<>();

        for(Entry<String, Object> pair : jsonObject.toMap().entrySet()) {
            outMap.put(pair.getKey(), pair.getValue().toString());
        }

        return outMap;
    }

    protected String getClassname(JsonObject obj, String prefix) {
        return obj.getObject(prefix).getString(APIMAN_RT_CLASS);
    }

    protected JsonObject getConfig(JsonObject obj, String prefix) {
        return obj.getObject(prefix).getObject(APIMAN_RT_CONFIG);
    }

    /**
     * @return a loaded class
     */
    @SuppressWarnings("unchecked")
    protected <T> Class<T> loadConfigClass(String classname, Class<T> type) {

        if (classname == null) {
            throw new RuntimeException("No " + type.getSimpleName() + " class configured."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        try {
            Class<T> c = (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(classname);
            return c;
        } catch (ClassNotFoundException e) {
            // Not found via Class.forName() - try other mechanisms.
        }
        try {
            Class<T> c = (Class<T>) Class.forName(classname);
            return c;
        } catch (ClassNotFoundException e) {
            // Not found via Class.forName() - try other mechanisms.
        }
        throw new RuntimeException(Messages.i18n.format("EngineConfig.FailedToLoadClass", classname)); //$NON-NLS-1$
    }
    
    protected String stringConfigWithDefault(String name, String defaultValue) {
        String str = config.getString(name);
        return str == null ? defaultValue : str;
    }
    
    protected Boolean boolConfigWithDefault(String name, Boolean defaultValue) {
        Boolean bool = config.containsField(name);
         
        return bool == null ? defaultValue : bool;
    }
}
