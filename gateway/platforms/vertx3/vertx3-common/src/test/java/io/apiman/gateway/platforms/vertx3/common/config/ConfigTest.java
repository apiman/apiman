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
package io.apiman.gateway.platforms.vertx3.common.config;

import static org.hamcrest.core.Is.is;

import io.vertx.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test config loading
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class ConfigTest {

    // Remember - only the config section will be parsed when getting #getMetricsConfig
    @Test
    public void simpleConfig() {
        String jsonStr = "{\"metrics\": {\n" +
                "    \"class\": \"io.apiman.gateway.engine.prometheus.PrometheusScrapeMetrics\",\n" +
                "    \"config\": {\n" +
                "      \"port\": 8083\n" +
                "    }\n" +
                "  }}";

        JsonObject jsonObject = new JsonObject(jsonStr);

        @SuppressWarnings("serial")
        Map<String, String> expected = new LinkedHashMap<String, String>(){{
            put("port", "8083");
        }};

        VertxEngineConfig config = new VertxEngineConfig(jsonObject);
        Assert.assertThat(config.getMetricsConfig(), is(expected));
    }

    @Test
    public void simpleNestedConfig() {
        String jsonStr = "{\n" +
                "    \"metrics\": {\n" +
                "        \"class\": \"xyz\",\n" +
                "        \"config\": {\n" +
                "            \"port\": \"ankh morpork\",\n" +
                "            \"favourite\": {\n" +
                "                \"food\": \"prime rat fillet\",\n" +
                "                \"drink\": \"quirmian cognac\"\n" +
                "            },\n" +
                "            \"races\": [\"human\",\"goblin\",\"dwarf\",\"elf\",\"gnome\",\n" +
                "                \"troll\",\"golem\",\"vampire\",\"werewolf\",\"zombie\"]\n" +
                "            }\n" +
                "        }\n" +
                "    }";

        JsonObject jsonObject = new JsonObject(jsonStr);

        @SuppressWarnings("serial")
        Map<String, String> expected = new LinkedHashMap<String, String>(){{
            put("port", "ankh morpork");
            put("favourite.food", "prime rat fillet");
            put("favourite.drink", "quirmian cognac");
            put("races", "human,goblin,dwarf,elf,gnome,troll,golem,vampire,werewolf,zombie");
        }};

        VertxEngineConfig config = new VertxEngineConfig(jsonObject);
        Assert.assertThat(config.getMetricsConfig(), is(expected));
    }

    @Test
    public void connectorNestedConfig() {
        String jsonStr = " { \"connector-factory\": {\n" +
                "    \"class\": \"io.apiman.gateway.platforms.vertx3.connector.ConnectorFactory\",\n" +
                "    \"config\": {\n" +
                "      \"tls\": {\n" +
                "        \"allowSelfSigned\": true,\n" +
                "        \"devMode\": true,\n" +
                "        \"trustStore\": \"/path/to/your/truststore.jks\",\n" +
                "        \"keyStore\": \"/path/to/your/keystore.jks\",\n" +
                "        \"keyStorePassword\": \"abc123\"\n" +
                "      }\n" +
                "    }\n" +
                "  }}";

        JsonObject jsonObject = new JsonObject(jsonStr);

        @SuppressWarnings("serial")
        Map<String, String> expected = new LinkedHashMap<String, String>(){{
            put("tls.allowSelfSigned", "true");
            put("tls.devMode", "true");
            put("tls.trustStore", "/path/to/your/truststore.jks");
            put("tls.keyStore", "/path/to/your/keystore.jks");
            put("tls.keyStorePassword", "abc123");
        }};

        VertxEngineConfig config = new VertxEngineConfig(jsonObject);
        Assert.assertThat(config.getConnectorFactoryConfig(), is(expected));
    }

}
