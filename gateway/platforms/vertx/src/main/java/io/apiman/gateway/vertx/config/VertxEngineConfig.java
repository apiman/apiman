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

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.vertx.java.core.json.JsonObject;

/**
 * Engine configuration, read simplistically from Vert'x JSON config.
 *
 * @see "http://vertx.io/manual.html#using-vertx-from-the-command-line"
 * @author Marc Savy <msavy@redhat.com>
 */
public class VertxEngineConfig implements IEngineConfig {

    public static final String API_GATEWAY_REGISTRY_PREFIX = "registry"; //$NON-NLS-1$
    public static final String API_GATEWAY_PLUGIN_REGISTRY_PREFIX = "plugin-registry"; //$NON-NLS-1$
    public static final String API_GATEWAY_CONNECTOR_FACTORY_PREFIX = "connector-factory"; //$NON-NLS-1$
    public static final String API_GATEWAY_POLICY_FACTORY_PREFIX = "policy-factory"; //$NON-NLS-1$
    public static final String API_GATEWAY_METRICS_PREFIX = "metrics"; //$NON-NLS-1$

    public static final String API_GATEWAY_COMPONENT_PREFIX = "components"; //$NON-NLS-1$

    private static final String API_GATEWAY_AUTH_PREFIX = "auth"; //$NON-NLS-1$

    public static final String API_GATEWAY_GATEWAY_SERVER_PORT = "server-port"; //$NON-NLS-1$

    public static final String API_GATEWAY_CONFIG = "config"; //$NON-NLS-1$
    public static final String API_GATEWAY_CLASS = "class"; //$NON-NLS-1$

    public static final String API_GATEWAY_EP_SERVICE_REQUEST = ".apiman.gateway.service.request"; //$NON-NLS-1$
    public static final String API_GATEWAY_EP_SERVICE_RESPONSE = ".apiman.gateway.service.response"; //$NON-NLS-1$

    public static final String API_GATEWAY_READY_SUFFIX = ".ready"; //$NON-NLS-1$
    public static final String API_GATEWAY_HEAD_SUFFIX = ".head"; //$NON-NLS-1$
    public static final String API_GATEWAY_BODY_SUFFIX = ".body"; //$NON-NLS-1$
    public static final String API_GATEWAY_END_SUFFIX = ".end"; //$NON-NLS-1$
    public static final String API_GATEWAY_ERROR_SUFFIX = ".error"; //$NON-NLS-1$
    public static final String API_GATEWAY_FAILURE_SUFFIX = ".failure"; //$NON-NLS-1$

    public static final String API_GATEWAY_GATEWAY_ROUTES = "routes"; //$NON-NLS-1$
    public static final String API_GATEWAY_EP_GATEWAY_REG_POLICY = "apiman.gateway.register.policy"; //$NON-NLS-1$

    public static final String APIMAN_API_APPLICATIONS_REGISTER = ".apiman.api.applications.register"; //$NON-NLS-1$
    public static final String APIMAN_API_APPLICATIONS_DELETE = ".apiman.api.applications.delete"; //$NON-NLS-1$
    public static final String APIMAN_API_SERVICES_REGISTER = ".apiman.api.services.register"; //$NON-NLS-1$
    public static final String APIMAN_API_SERVICES_DELETE = ".apiman.api.services.delete"; //$NON-NLS-1$
    public static final String APIMAN_API_SUBSCRIBE = "apiman.api.subscribe"; //$NON-NLS-1$

    private static final String API_GATEWAY_AUTH_BASIC = "file-basic"; //$NON-NLS-1$
    private static final String API_GATEWAY_AUTH_ENABLED = "authenticated"; //$NON-NLS-1$
    private static final String API_GATEWAY_AUTH_REALM = "realm"; //$NON-NLS-1$
    private static final String API_GATEWAY_HOSTNAME = "hostname"; //$NON-NLS-1$
    private static final String API_GATEWAY_ENDPOINT = "endpoint"; //$NON-NLS-1$

    private RouteMapper routeMap;
    private JsonObject config;

    public VertxEngineConfig(JsonObject config) {
        this.config = config;

        if(config.getObject(API_GATEWAY_GATEWAY_ROUTES) != null) {
            routeMap = new RouteMapper(config.getObject(API_GATEWAY_GATEWAY_ROUTES));
        } else {
            routeMap = new RouteMapper();
        }
    }

    public JsonObject getConfig() {
        return config;
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getRegistryClass(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public Class<? extends IRegistry> getRegistryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, API_GATEWAY_REGISTRY_PREFIX),
                IRegistry.class);
    }

    @Override
    public Map<String, String> getRegistryConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_REGISTRY_PREFIX));
    }

    @Override
    public Class<? extends IPluginRegistry> getPluginRegistryClass() {
        return loadConfigClass(getClassname(config, API_GATEWAY_PLUGIN_REGISTRY_PREFIX),
                IPluginRegistry.class);
    }

    @Override
    public Map<String, String> getPluginRegistryConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_PLUGIN_REGISTRY_PREFIX));
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getConnectorFactoryClass(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public Class<? extends IConnectorFactory> getConnectorFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, API_GATEWAY_CONNECTOR_FACTORY_PREFIX),
                IConnectorFactory.class);
    }

    @Override
    public Map<String, String> getConnectorFactoryConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_CONNECTOR_FACTORY_PREFIX));
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getPolicyFactoryClass(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public Class<? extends IPolicyFactory> getPolicyFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, API_GATEWAY_POLICY_FACTORY_PREFIX),
                IPolicyFactory.class);
    }

    @Override
    public Map<String, String> getPolicyFactoryConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_POLICY_FACTORY_PREFIX));
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getMetricsClass(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public Class<? extends IMetrics> getMetricsClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, API_GATEWAY_METRICS_PREFIX),
                IMetrics.class);
    }

    @Override
    public Map<String, String> getMetricsConfig() {
        return toFlatStringMap(getConfig(config, API_GATEWAY_METRICS_PREFIX));
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getComponentClass(java.lang.Class, io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public <T extends IComponent> Class<T> getComponentClass(Class<T> componentType,
            IPluginRegistry pluginRegistry) {
        String className = config.getObject(API_GATEWAY_COMPONENT_PREFIX).
                getObject(componentType.getSimpleName()).
                getString(API_GATEWAY_CLASS);

        return loadConfigClass(className, componentType);
    }

    @Override
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType) {
        JsonObject componentConfig = config.getObject(API_GATEWAY_COMPONENT_PREFIX).
                getObject(componentType.getSimpleName()).
                getObject(API_GATEWAY_CONFIG);

        return toFlatStringMap(componentConfig);
    }


    public Boolean isAuthenticationEnabled() {
        return boolConfigWithDefault(API_GATEWAY_AUTH_ENABLED, false);
    }

    public String getRealm() {
        return stringConfigWithDefault(API_GATEWAY_AUTH_REALM, "apiman-realm"); //$NON-NLS-1$
    }

    public RouteMapper getRouteMap() {
        return routeMap;
    }

    public String hostname() {
        return stringConfigWithDefault(API_GATEWAY_HOSTNAME, "localhost"); //$NON-NLS-1$
    }

    public String getEndpoint() {
        return stringConfigWithDefault(API_GATEWAY_ENDPOINT, "localhost");    //$NON-NLS-1$
    }

    public Map<String, String> loadFileBasicAuth() {
        JsonObject pairs = config.getObject(API_GATEWAY_AUTH_PREFIX).getObject(API_GATEWAY_AUTH_BASIC);

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
        return obj.getObject(prefix).getString(API_GATEWAY_CLASS);
    }

    protected JsonObject getConfig(JsonObject obj, String prefix) {
        return obj.getObject(prefix).getObject(API_GATEWAY_CONFIG);
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
