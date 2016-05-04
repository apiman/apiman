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
package io.apiman.gateway.platforms.war;

import io.apiman.gateway.engine.GatewayConfigProperties;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IPolicyErrorWriter;
import io.apiman.gateway.engine.IPolicyFailureWriter;
import io.apiman.gateway.engine.impl.ConfigDrivenEngineFactory;
import io.apiman.gateway.engine.impl.DefaultPolicyErrorWriter;
import io.apiman.gateway.engine.impl.DefaultPolicyFailureWriter;

import java.util.Map;

/**
 * Top level gateway.  Used when the API Management Runtime Engine is being used
 * in a standard Web Application gateway scenario.
 *
 * @author eric.wittmann@redhat.com
 */
public class WarGateway {

    public static WarEngineConfig config;
    public static IEngine engine;
    public static IPolicyFailureWriter failureFormatter;
    public static IPolicyErrorWriter errorFormatter;

    /**
     * Initialize the gateway.
     */
    public static void init() {
        config = new WarEngineConfig();
        // Surface the max-payload-buffer-size property as a system property, if it exists in the apiman.properties file
        if (System.getProperty(GatewayConfigProperties.MAX_PAYLOAD_BUFFER_SIZE) == null) {
            String propVal = config.getConfigProperty(GatewayConfigProperties.MAX_PAYLOAD_BUFFER_SIZE, null);
            if (propVal != null) {
                System.setProperty(GatewayConfigProperties.MAX_PAYLOAD_BUFFER_SIZE, propVal);
            }
        }
        
        ConfigDrivenEngineFactory factory = new ConfigDrivenEngineFactory(config);
        engine = factory.createEngine();
        failureFormatter = loadFailureFormatter();
        errorFormatter = loadErrorFormatter();
    }

    private static IPolicyErrorWriter loadErrorFormatter() {
        Class<? extends IPolicyErrorWriter> clazz = config.getPolicyErrorWriterClass(engine.getPluginRegistry());
        if (clazz == null) {
            clazz = DefaultPolicyErrorWriter.class;
        }
        Map<String, String> conf = config.getPolicyErrorWriterConfig();
        return ConfigDrivenEngineFactory.instantiate(clazz, conf);
    }

    private static IPolicyFailureWriter loadFailureFormatter() {
        Class<? extends IPolicyFailureWriter> clazz = config.getPolicyFailureWriterClass(engine.getPluginRegistry());
        if (clazz == null) {
            clazz = DefaultPolicyFailureWriter.class;
        }
        Map<String, String> conf = config.getPolicyFailureWriterConfig();
        return ConfigDrivenEngineFactory.instantiate(clazz, conf);
    }

    /**
     * Shuts down the gateway.
     */
    public static void shutdown() {
        engine = null;
    }

}
