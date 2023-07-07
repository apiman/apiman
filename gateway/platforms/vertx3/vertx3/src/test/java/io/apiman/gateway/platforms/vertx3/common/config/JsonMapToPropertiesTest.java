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

import io.vertx.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test conversion of JSON to properties map
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class JsonMapToPropertiesTest {

    @Test
    public void simpleArray() {
        String str = "{ \"races\": [\"human\",\"goblin\",\"dwarf\",\"elf\",\"gnome\",\n" +
                " \"troll\",\"golem\",\"vampire\",\"werewolf\",\"zombie\"]\n" +
                " }";

        JsonObject jso = new JsonObject(str);
        VertxEngineConfig vxConf = new VertxEngineConfig(jso);


        Map<String, String> expected = Map.of(
            "races", "human,goblin,dwarf,elf,gnome,troll,golem,vampire,werewolf,zombie"
        );

        Map<String, String> actual = new LinkedHashMap<>();
        vxConf.jsonMapToProperties("", jso, actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void simpleObject() {
        String str = "{\"favourite\": {\n" +
                "  \"food\": \"prime rat fillet\",\n" +
                "  \"drink\": \"quirmian cognac\"\n" +
                "}}";

        JsonObject jso = new JsonObject(str);
        VertxEngineConfig vxConf = new VertxEngineConfig(jso);

        Map<String, String> expected = Map.of(
            "favourite.food", "prime rat fillet",
            "favourite.drink", "quirmian cognac"
        );

        Map<String, String> actual = new LinkedHashMap<>();
        vxConf.jsonMapToProperties("", jso, actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void nestedObject() {
        String str = "{\n" +
                "    \"config\": {\n" +
                "        \"port\": \"ankh morpork\",\n" +
                "        \"favourite\": {\n" +
                "            \"food\": \"prime rat fillet\",\n" +
                "            \"drink\": \"quirmian cognac\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        JsonObject jso = new JsonObject(str);
        VertxEngineConfig vxConf = new VertxEngineConfig(jso);


        Map<String, String> expected = Map.of(
            "config.port", "ankh morpork",
            "config.favourite.food", "prime rat fillet",
            "config.favourite.drink", "quirmian cognac"
        );

        Map<String, String> actual = new LinkedHashMap<>();
        vxConf.jsonMapToProperties("", jso, actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void nestedObjectAndArray() {
        String str = "{\n" +
                "    \"config\": {\n" +
                "        \"port\": \"victoria\",\n" +
                "        \"favourite\": {\n" +
                "            \"food\": \"kari koko\",\n" +
                "            \"drink\": \"kalou\"\n" +
                "        },\n" +
                "        \"places\": [\"anse boileau\", \"anse royale\"]\n" +
                "    }\n" +
                "}";

        JsonObject jso = new JsonObject(str);
        VertxEngineConfig vxConf = new VertxEngineConfig(jso);


        Map<String, String> expected = Map.of(
                "config.port", "victoria",
                "config.favourite.food", "kari koko",
                "config.favourite.drink", "kalou",
                "config.places", "anse boileau,anse royale"
        );

        Map<String, String> actual = new LinkedHashMap<>();
        vxConf.jsonMapToProperties("", jso, actual);
        Assert.assertEquals(expected, actual);
    }

}
