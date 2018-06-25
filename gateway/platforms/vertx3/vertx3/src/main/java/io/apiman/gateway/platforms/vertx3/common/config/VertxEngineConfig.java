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

import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginClassLoader;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.util.ApimanStrLookup;
import io.apiman.common.util.ReflectionUtils;
import io.apiman.common.util.SimpleStringUtils;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.EngineConfigTuple;
import io.apiman.gateway.engine.GatewayConfigProperties;
import io.apiman.gateway.engine.IApiRequestPathParser;
import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngineConfig;
import io.apiman.gateway.engine.IGatewayInitializer;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IPolicyErrorWriter;
import io.apiman.gateway.engine.IPolicyFailureWriter;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.impl.DefaultDataEncrypter;
import io.apiman.gateway.engine.impl.DefaultPolicyErrorWriter;
import io.apiman.gateway.engine.impl.DefaultPolicyFailureWriter;
import io.apiman.gateway.engine.impl.DefaultRequestPathParser;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.vertx3.common.verticles.VerticleType;
import io.apiman.gateway.platforms.vertx3.connector.ConnectorFactory;
import io.apiman.gateway.platforms.vertx3.engine.VertxPluginRegistry;
import io.apiman.gateway.platforms.vertx3.i18n.Messages;
import io.apiman.gateway.platforms.vertx3.logging.VertxLoggerDelegate;
import io.vertx.core.json.JsonObject;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;


/**
 * Engine configuration, read simplistically from Vert'x JSON config.
 *
 * @see "http://vertx.io/manual.html#using-vertx-from-the-command-line"
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class VertxEngineConfig implements IEngineConfig {
    private static final String VERTICLES = "verticles";
    private static final String VERTICLE_HOST = "host";
    private static final String VERTICLE_PORT = "port";
    private static final String VERTICLE_COUNT = "count";

    private static final String GATEWAY_HOSTNAME = "hostname";
    private static final String GATEWAY_PUBLIC_ENDPOINT = "publicEndpoint";
    private static final String GATEWAY_PREFER_SECURE = "preferSecure";

    private static final String API_AUTH = "auth";
    private static final String API_PASSWORD = "password";

    private static final String GATEWAY_REGISTRY_PREFIX = "registry";
    private static final String GATEWAY_ENCRYPTER_PREFIX = "encrypter";
    private static final String GATEWAY_PLUGIN_REGISTRY_PREFIX = "plugin-registry";
    private static final String GATEWAY_CONNECTOR_FACTORY_PREFIX = "connector-factory";
    private static final String GATEWAY_POLICY_FACTORY_PREFIX = "policy-factory";
    private static final String GATEWAY_METRICS_PREFIX = "metrics";
    private static final String GATEWAY_COMPONENT_PREFIX = "components";
    private static final String GATEWAY_REQUEST_PARSER_PREFIX = "request-parser";

    private static final String GATEWAY_CONFIG = "config";
    private static final String GATEWAY_CLASS = "class";

    private static final String SSL = "ssl";
    private static final String SSL_TRUSTSTORE = "truststore";
    private static final String SSL_KEYSTORE = "keystore";
    private static final String SSL_PATH = "path";
    private static final String VARIABLES = "variables";

    private JsonObject config;

    public VertxEngineConfig(JsonObject config) {
        this.config = config;
        substituteLeafVars(config);
    }

    public JsonObject getConfig() {
        return config;
    }

    @Override
    public Class<? extends IRegistry> getRegistryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_REGISTRY_PREFIX),
                IRegistry.class, pluginRegistry, null);
    }

    @Override
    public Class<? extends IDataEncrypter> getDataEncrypterClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_ENCRYPTER_PREFIX),
                IDataEncrypter.class, pluginRegistry, DefaultDataEncrypter.class);
    }

    @Override
    public Map<String, String> getRegistryConfig() {
        return getConfig(config, GATEWAY_REGISTRY_PREFIX);
    }

    @Override
    public Map<String, String> getDataEncrypterConfig() {
        return getConfig(config, GATEWAY_ENCRYPTER_PREFIX);
    }

    @Override
    public Class<? extends IPluginRegistry> getPluginRegistryClass() {
        return loadConfigClass(getClassname(config, GATEWAY_PLUGIN_REGISTRY_PREFIX),
                IPluginRegistry.class, null, VertxPluginRegistry.class);
    }

    @Override
    public Map<String, String> getPluginRegistryConfig() {
        return getConfig(config, GATEWAY_PLUGIN_REGISTRY_PREFIX);
    }

    @Override
    public Class<? extends IConnectorFactory> getConnectorFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_CONNECTOR_FACTORY_PREFIX),
                IConnectorFactory.class, pluginRegistry, ConnectorFactory.class);
    }

    @Override
    public Map<String, String> getConnectorFactoryConfig() {
        return getConfig(config, GATEWAY_CONNECTOR_FACTORY_PREFIX);
    }

    @Override
    public Class<? extends IPolicyFactory> getPolicyFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_POLICY_FACTORY_PREFIX),
                IPolicyFactory.class, pluginRegistry, PolicyFactoryImpl.class);
    }

    @Override
    public Map<String, String> getPolicyFactoryConfig() {
        return getConfig(config, GATEWAY_POLICY_FACTORY_PREFIX);
    }


    @Override
    public Class<? extends IApiRequestPathParser> getApiRequestPathParserClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_REQUEST_PARSER_PREFIX),
                IApiRequestPathParser.class, pluginRegistry, DefaultRequestPathParser.class);
    }

    @Override
    public Map<String, String> getApiRequestPathParserConfig() { // Probably will not be used
        return getConfig(config, GATEWAY_REQUEST_PARSER_PREFIX);
    }

    @Override
    public Class<? extends IMetrics> getMetricsClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GATEWAY_METRICS_PREFIX),
                IMetrics.class, pluginRegistry, null);
    }

    @Override
    public Map<String, String> getMetricsConfig() {
        return getConfig(config, GATEWAY_METRICS_PREFIX);
    }

    @Override
    public Class<? extends IDelegateFactory> getLoggerFactoryClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GatewayConfigProperties.LOGGER_FACTORY_CLASS),
                IDelegateFactory.class, pluginRegistry, VertxLoggerDelegate.class);
    }

    @Override
    public Map<String, String> getLoggerFactoryConfig() {
        return getConfig(config, GatewayConfigProperties.LOGGER_FACTORY_CLASS);
    }

    @Override
    public Class<? extends IPolicyErrorWriter> getPolicyErrorWriterClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GatewayConfigProperties.ERROR_WRITER_CLASS),
                IPolicyErrorWriter.class, pluginRegistry, DefaultPolicyErrorWriter.class);
    }

    @Override
    public Map<String, String> getPolicyErrorWriterConfig() {
        return getConfig(config, GatewayConfigProperties.ERROR_WRITER_CLASS);
    }

    @Override
    public Class<? extends IPolicyFailureWriter> getPolicyFailureWriterClass(IPluginRegistry pluginRegistry) {
        return loadConfigClass(getClassname(config, GatewayConfigProperties.FAILURE_WRITER_CLASS),
                IPolicyFailureWriter.class, pluginRegistry, DefaultPolicyFailureWriter.class);
    }

    @Override
    public Map<String, String> getPolicyFailureWriterConfig() {
        return getConfig(config, GatewayConfigProperties.FAILURE_WRITER_CLASS);
    }

    @Override
    public <T extends IComponent> Class<? extends T> getComponentClass(Class<? extends T> componentType, IPluginRegistry pluginRegistry) {
        String className = config.getJsonObject(GATEWAY_COMPONENT_PREFIX).
                getJsonObject(componentType.getSimpleName()).
                getString(GATEWAY_CLASS);

        return loadConfigClass(className, componentType, pluginRegistry, null);
    }

    @Override
    public <T extends IComponent> Map<String, String> getComponentConfig(Class<T> componentType) {
        JsonObject componentConfig = config.getJsonObject(GATEWAY_COMPONENT_PREFIX).
                getJsonObject(componentType.getSimpleName()).
                getJsonObject(GATEWAY_CONFIG);

        return toFlatStringMap(componentConfig);
    }

    /**
     * @see io.apiman.gateway.engine.IEngineConfig#getGatewayInitializers(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public List<EngineConfigTuple<? extends IGatewayInitializer>> getGatewayInitializers(
            IPluginRegistry pluginRegistry) {
        // TODO support gateway initializers in vert.x
        return Collections.emptyList();
    }

    public String getHostname() {
        return stringConfigWithDefault(GATEWAY_HOSTNAME, "0.0.0.0");
    }

    public String getPublicEndpoint() {
        return config.getString(GATEWAY_PUBLIC_ENDPOINT);
    }

    public Boolean preferSecure() {
        return config.getBoolean(GATEWAY_PREFER_SECURE);
    }

    public int getPort(String name) {
        return getValueAsInteger(getVerticleConfig(name), VERTICLE_PORT, -1);
    }

    public int getPort(VerticleType verticleType) {
        return getPort(verticleType.name());
    }

    public int getVerticleCount(VerticleType verticleType) {
        // If the field is set as "auto", then number of verticles == number of cores
        // TODO should be decent default choice, but should benchmark to find optimum.
        // See: VertxOptions
        if ("auto".equalsIgnoreCase(getVerticleConfig(verticleType.name()).getValue(VERTICLE_COUNT).toString())) {
            return Runtime.getRuntime().availableProcessors();
        }
        return getValueAsInteger(getVerticleConfig(verticleType.name()), VERTICLE_COUNT, 1);
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

    public JsonObject getAuth() {
        return config.getJsonObject(API_AUTH, new JsonObject());
    }

    protected Map<String, String> toFlatStringMap(JsonObject jsonObject) {
        if (jsonObject == null)
            return Collections.emptyMap();

        Map<String, String> outMap = new LinkedHashMap<>();
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
                output.put(pathSoFar, SimpleStringUtils.join(",", output.get(pathSoFar), valueOrNull(value)));
            } else {
                output.put(pathSoFar, valueOrNull(value));
            }
        }
    }

    private String valueOrNull(Object value) {
        return value == null ? null : value.toString();
    }

    private String determineKey(String pathSoFar, String key) {
        return pathSoFar.length() == 0 ? key : pathSoFar + "." + key;
    }

    protected String getClassname(JsonObject obj, String prefix) {
        String clazzName = System.getProperty(prefix);
        // TODO Something of a hack because the constants may assume apiman-gateway prefix, which isn't in the vert.x JSON.
        String strippedPrefix = StringUtils.substringAfter(prefix, "apiman-gateway.");
        String filteredPrefix = strippedPrefix.isEmpty() ? prefix : strippedPrefix;

        if (clazzName == null) {
            Map<String, String> mfp = toFlatStringMap(obj);
            return mfp.get(filteredPrefix + "." + GATEWAY_CLASS);
        }

        return clazzName;
    }

    protected Map<String, String> getConfig(JsonObject obj, String prefix) {
        // First, check whether there's something interesting in System properties.
        Map<String, String> mfp = getConfigMapFromProperties("apiman-gateway." + prefix);

        if (mfp != null && !mfp.isEmpty()) { // TODO
            return mfp;
        }
        return toFlatStringMap(obj.getJsonObject(prefix, new JsonObject()).getJsonObject(GATEWAY_CONFIG));
    }

    private int getValueAsInteger(JsonObject obj, String key, int defaultValue) {
        Object val = obj.getValue(key);
        if (val == null) {
            return defaultValue;
        } else {
            if (val instanceof Number) {
                return (int) val;
            } else if (val instanceof String) {
                return Integer.valueOf((String)val);
            }
            throw new IllegalArgumentException("Expected integer, got " + obj.getClass().getName());
        }
    }

    /**
     * @return a loaded class
     */
    @SuppressWarnings("unchecked")
    protected <T> Class<? extends T> loadConfigClass(String className, Class<T> type, IPluginRegistry pluginRegistry, Class<? extends T> defaultClass) {
        String componentSpec = className;
        if (componentSpec == null) {
            return defaultClass;
        }

        try {
            if (componentSpec.startsWith("class:")) { //$NON-NLS-1$
                Class<?> c = ReflectionUtils.loadClass(componentSpec.substring("class:".length())); //$NON-NLS-1$
                return (Class<T>) c;
            } else if (componentSpec.startsWith("plugin:")) { //$NON-NLS-1$
                PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(componentSpec);
                if (coordinates == null) {
                    throw new IllegalArgumentException("Invalid plugin component spec: " + componentSpec); //$NON-NLS-1$
                }
                int ssidx = componentSpec.indexOf('/');
                if (ssidx == -1) {
                    throw new IllegalArgumentException("Invalid plugin component spec: " + componentSpec); //$NON-NLS-1$
                }
                String classname = componentSpec.substring(ssidx + 1);
                Future<IAsyncResult<Plugin>> pluginF = pluginRegistry.loadPlugin(coordinates, null);
                IAsyncResult<Plugin> pluginR = pluginF.get();
                if (pluginR.isError()) {
                    throw new RuntimeException(pluginR.getError());
                }
                Plugin plugin = pluginR.getResult();
                PluginClassLoader classLoader = plugin.getLoader();
                Class<?> class1 = classLoader.loadClass(classname);
                return (Class<T>) class1;
            } else {
                Class<?> c = ReflectionUtils.loadClass(componentSpec);
                return (Class<T>) c;
            }
        } catch (ClassNotFoundException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IllegalStateException(Messages.getString("EngineConfig.FailedToLoadClass") + className);
        }

    }

    protected String stringConfigWithDefault(String name, String defaultValue) {
        String str = config.getString(name);
        return str == null ? defaultValue : str;
    }

    public JsonObject getVerticleConfig(String verticleType) {
        return config.getJsonObject(VERTICLES).getJsonObject(verticleType.toLowerCase());
    }

    /**
     * Gets all properties in the engine configuration that are prefixed
     * with the given prefix.
     * @param prefix the prefix
     * @return all prefixed properties
     */
    private Map<String, String> getConfigMapFromProperties(String prefix) {
        Map<String, String> rval = new HashMap<>();
        getKeys(prefix).forEach(pair -> {
            if (!pair.getKey().equals(prefix)) {
                String shortKey = pair.getKey().substring(prefix.length() + 1);
                rval.put(shortKey, pair.getValue());
            }
        });
        return rval;
    }

    private List<Entry<String, String>> getKeys(String prefix) {
        return System.getProperties().entrySet().stream()
                .map(pair -> new AbstractMap.SimpleEntry<>(String.valueOf(pair.getKey()), String.valueOf(pair.getValue())))
                .filter(pair -> StringUtils.startsWith(pair.getKey(), prefix))
                .collect(Collectors.toList());
    }

    protected void substituteLeafVars(JsonObject node) {
        node.forEach(elem -> {
           Object value = elem.getValue();
           if (value instanceof JsonObject) {
               substituteLeafVars((JsonObject) value);
           } else if (value instanceof String) {
               node.put(elem.getKey(), SUBSTITUTOR.replace((String) value));
           }
        });
    }

    private final StrSubstitutor SUBSTITUTOR = new StrSubstitutor(new VertxEngineStrSubstitutor());

    private class VertxEngineStrSubstitutor extends ApimanStrLookup {

        @Override
        public String lookup(String key) {
            // Upside of this approach is that we can capture any dynamically defined variables.
            // Downside is that it's slightly expensive but should only happen at startup.
            Map<String, String> flattenedMap = new LinkedHashMap<>();
            jsonMapToProperties("", new JsonObject(getVariables().encode()).getMap(), flattenedMap);
            if (flattenedMap.containsKey(key)) {
                return flattenedMap.get(key);
            } else {
                return super.lookup(key);
            }
        }

        private JsonObject getVariables() {
            return config.getJsonObject(VARIABLES, new JsonObject());
        }
    }

}
