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
package io.apiman.gateway.platforms.vertx3.common.config;

import io.apiman.common.util.SimpleStringUtils;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Engine configuration, read simplistically from Vert'x JSON config.
 *
 * @see "http://vertx.io/manual.html#using-vertx-from-the-command-line"
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class VertxEngineConfig implements IEngineConfig {
    public static final String GATEWAY_ENDPOINT_POLICY_INGESTION = "io.apiman.gateway.platforms.vertx2.policy";
    public static final String GATEWAY_ENDPOINT_REQUEST = ".request";
    public static final String GATEWAY_ENDPOINT_RESPONSE = ".response";

    private static final String VERTICLES = "verticles";
    private static final String VERTICLE_PORT = "port";
    private static final String VERTICLE_COUNT = "count";

    private static final String GATEWAY_HOSTNAME = "hostname";
    private static final String GATEWAY_ENDPOINT = "endpoint";
    private static final String GATEWAY_PREFER_SECURE = "preferSecure";

    private static final String API_AUTH = "auth";
    private static final String API_PASSWORD = "password";
    private static final String API_REQUIRED = "required";
    private static final String API_REALM = "realm";

    private static final String GATEWAY_REGISTRY_PREFIX = "registry";
    private static final String GATEWAY_ENCRYPTER_PREFIX = "encrypter";
    private static final String GATEWAY_PLUGIN_REGISTRY_PREFIX = "plugin-registry";
    private static final String GATEWAY_CONNECTOR_FACTORY_PREFIX = "connector-factory";
    private static final String GATEWAY_POLICY_FACTORY_PREFIX = "policy-factory";
    private static final String GATEWAY_METRICS_PREFIX = "metrics";
    private static final String GATEWAY_COMPONENT_PREFIX = "components";

    private static final String GATEWAY_CONFIG = "config";
    private static final String GATEWAY_CLASS = "class";

    private static final String SSL = "ssl";
    private static final String SSL_TRUSTSTORE = "truststore";
    private static final String SSL_KEYSTORE = "keystore";
    private static final String SSL_PATH = "path";

    private JsonObject config;
    private HashMap<String, String> basicAuthMap = new HashMap<>();

    public VertxEngineConfig(JsonObject config) {
        this.config = config;
    }

    public JsonObject getConfig() {
        return config;
    }

    @Override
    public Class<? extends IRegistry> getRegistryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_REGISTRY_PREFIX),
                IRegistry.class);
    }

    @Override
    public Class<? extends IDataEncrypter> getDataEncrypterClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_ENCRYPTER_PREFIX), IDataEncrypter.class);
    }

    @Override
    public Map<String, String> getRegistryConfig() {
        return toFlatStringMap(getConfig(config, GATEWAY_REGISTRY_PREFIX));
    }

    @Override
    public Map<String, String> getDataEncrypterConfig() {
        return toFlatStringMap(getConfig(config, GATEWAY_ENCRYPTER_PREFIX));
    }

    @Override
    public Class<? extends IPluginRegistry> getPluginRegistryClass() {
        return loadConfigClass(getClassname(config, GATEWAY_PLUGIN_REGISTRY_PREFIX),
                IPluginRegistry.class);
    }

    @Override
    public Map<String, String> getPluginRegistryConfig() {
        return toFlatStringMap(getConfig(config, GATEWAY_PLUGIN_REGISTRY_PREFIX));
    }

    @Override
    public Class<? extends IConnectorFactory> getConnectorFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_CONNECTOR_FACTORY_PREFIX),
                IConnectorFactory.class);
    }

    @Override
    public Map<String, String> getConnectorFactoryConfig() {
        return toFlatStringMap(getConfig(config, GATEWAY_CONNECTOR_FACTORY_PREFIX));
    }

    @Override
    public Class<? extends IPolicyFactory> getPolicyFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_POLICY_FACTORY_PREFIX),
                IPolicyFactory.class);
    }

    @Override
    public Map<String, String> getPolicyFactoryConfig() {
        return toFlatStringMap(getConfig(config, GATEWAY_POLICY_FACTORY_PREFIX));
    }

    @Override
    public Class<? extends IMetrics> getMetricsClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_METRICS_PREFIX),
                IMetrics.class);
    }

    @Override
    public Map<String, String> getMetricsConfig() {
        return toFlatStringMap(getConfig(config, GATEWAY_METRICS_PREFIX));
    }

    @Override
    public <T extends IComponent> Class<T> getComponentClass(Class<T> componentType, IPluginRegistry pluginRegistry) {
        String className = config.getJsonObject(GATEWAY_COMPONENT_PREFIX).
                getJsonObject(componentType.getSimpleName()).
                getString(GATEWAY_CLASS);

        return loadConfigClass(className, componentType);
    }

    @Override
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType) {
        JsonObject componentConfig = config.getJsonObject(GATEWAY_COMPONENT_PREFIX).
                getJsonObject(componentType.getSimpleName()).
                getJsonObject(GATEWAY_CONFIG);

        return toFlatStringMap(componentConfig);
    }

    public Boolean isAuthenticationEnabled() {
        return config.getJsonObject(API_AUTH).getString(API_REQUIRED) != null;
    }

    public String getRealm() {
        return config.getJsonObject(API_AUTH).getString(API_REALM);
    }

    public String getHostname() {
        return stringConfigWithDefault(GATEWAY_HOSTNAME, "localhost");
    }

    public String getEndpoint() {
        return config.getString(GATEWAY_ENDPOINT);
    }

    public Boolean preferSecure() {
        return config.getBoolean(GATEWAY_PREFER_SECURE);
    }

    public Map<String, String> getBasicAuthCredentials() {
        if (!basicAuthMap.isEmpty())
            return basicAuthMap;

        JsonObject pairs = config.getJsonObject(API_AUTH).getJsonObject("basic");

        for (String username : pairs.fieldNames()) {
            basicAuthMap.put(username, pairs.getString(username));
        }

        return basicAuthMap;
    }

    protected Map<String, String> toFlatStringMap(JsonObject jsonObject) {
        Map<String, String> outMap = new LinkedHashMap<>();
        // TODO figure out why this workaround is necessary.
        jsonMapToProperties("", new JsonObject(jsonObject.encode()).getMap(), outMap);
        return outMap;
    }

    @SuppressWarnings("unchecked")
    protected void jsonMapToProperties(String pathSoFar, Object value, Map<String, String> output) {
        if (value instanceof Map) { // Descend again
            Map<String, Object> map = (Map<String, Object>) value;
            map.entrySet()
                .forEach(elem -> jsonMapToProperties(determineKey(pathSoFar, elem.getKey()), elem.getValue(), output));
        } else if (value instanceof List) { // Join objects and descend
            List<Object> list = (List<Object>) value;
            list.forEach(elem -> jsonMapToProperties(pathSoFar, elem, output));
        } else { // Value
            if (output.containsKey(pathSoFar)) {
                output.put(pathSoFar, SimpleStringUtils.join(",", output.get(pathSoFar), value.toString()));
            } else {
                output.put(pathSoFar, value.toString());
            }
        }
    }

    private String determineKey(String pathSoFar, String key) {
        return pathSoFar.length() == 0 ? key : pathSoFar + "." + key;
    }

    protected String getClassname(JsonObject obj, String prefix) {
        return obj.getJsonObject(prefix).getString(GATEWAY_CLASS);
    }

    protected JsonObject getConfig(JsonObject obj, String prefix) {
        return obj.getJsonObject(prefix).getJsonObject(GATEWAY_CONFIG);
    }

    /**
     * @return a loaded class
     */
    @SuppressWarnings("unchecked")
    protected <T> Class<T> loadConfigClass(String classname, Class<T> type) {

        if (classname == null) {
            throw new RuntimeException("No " + type.getSimpleName() + " class configured.");  //$NON-NLS-2$
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

        System.err.println("COULD NOT LOAD " + classname);

        throw new RuntimeException(Messages.i18n.format("EngineConfig.FailedToLoadClass", classname));
    }

    protected String stringConfigWithDefault(String name, String defaultValue) {
        String str = config.getString(name);
        return str == null ? defaultValue : str;
    }

    protected Boolean boolConfigWithDefault(String name, Boolean defaultValue) {
        Boolean bool = config.containsKey(name);
        return bool == null ? defaultValue : bool;
    }

    public JsonObject getVerticleConfig(String verticleType) {
        return config.getJsonObject(VERTICLES).getJsonObject(verticleType.toLowerCase());
    }

    public int getPort(String name) {
        return getVerticleConfig(name).getInteger(VERTICLE_PORT);
    }

    public int getPort(VerticleType verticleType) {
        return getPort(verticleType.name());
    }

    public int getVerticleCount(VerticleType verticleType) {
        return getVerticleConfig(verticleType.name()).getInteger(VERTICLE_COUNT);
    }

    public boolean isSSL() {
        return config.containsKey(SSL);
    }

    public String getKeyStore() {
        return config.getJsonObject(SSL, new JsonObject()).getJsonObject(SSL_KEYSTORE, new JsonObject()).getString(SSL_PATH);
    }

    public String getKeyStorePassword() {
        return config.getJsonObject(SSL, new JsonObject()).getJsonObject(SSL_KEYSTORE, new JsonObject()).getString(API_PASSWORD);
    }

    public String getTrustStore() {
        return config.getJsonObject(SSL, new JsonObject()).getJsonObject(SSL_TRUSTSTORE, new JsonObject()).getString(SSL_PATH);
    }

    public String getTrustStorePassword() {
        return config.getJsonObject(SSL, new JsonObject()).getJsonObject(SSL_TRUSTSTORE, new JsonObject()).getString(API_PASSWORD);
    }

}
