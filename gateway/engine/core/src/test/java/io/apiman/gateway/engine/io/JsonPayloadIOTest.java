/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.gateway.engine.io;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit Test for {@link JsonPayloadIO}.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({"nls", "rawtypes"})
public class JsonPayloadIOTest {

    @Test
    public void testUnmarshall_Simple() throws Exception {
        String json = "{\r\n" +
                "  \"hello\" : \"world\",\r\n" +
                "  \"foo\" : \"bar\"\r\n" +
                "}";

        JsonPayloadIO io = new JsonPayloadIO();
        Map data = io.unmarshall(new ReaderInputStream(new StringReader(json)));
        Assert.assertNotNull(data);
        Assert.assertEquals("world", data.get("hello"));
        Assert.assertEquals("bar", data.get("foo"));
        Assert.assertNull(data.get("other"));

        data = io.unmarshall(json.getBytes("UTF-8"));
        Assert.assertNotNull(data);
        Assert.assertEquals("world", data.get("hello"));
        Assert.assertEquals("bar", data.get("foo"));
        Assert.assertNull(data.get("other"));
    }


    @Test
    public void testUnmarshall_Complex() throws Exception {
        String json = "{\r\n" +
                "    \"hello\": \"world\",\r\n" +
                "    \"foo\": \"bar\",\r\n" +
                "    \"someBool\": true,\r\n" +
                "    \"someLong\": 123847281437,\r\n" +
                "    \"object\": {\r\n" +
                "        \"prop1\": \"value-1\",\r\n" +
                "        \"prop2\": \"value-2\"\r\n" +
                "    },\r\n" +
                "    \"intArray\": [\r\n" +
                "        1,\r\n" +
                "        2,\r\n" +
                "        3,\r\n" +
                "        4,\r\n" +
                "        5\r\n" +
                "    ],\r\n" +
                "    \"strArray\": [\r\n" +
                "        \"one\",\r\n" +
                "        \"two\",\r\n" +
                "        \"three\"\r\n" +
                "    ],\r\n" +
                "    \"objArray\": [\r\n" +
                "        {\r\n" +
                "            \"f1\": \"fv1\",\r\n" +
                "            \"f2\": \"fv2\"\r\n" +
                "        },\r\n" +
                "        {\r\n" +
                "            \"f3\": \"fv3\",\r\n" +
                "            \"f4\": \"fv4\"\r\n" +
                "        },\r\n" +
                "        {\r\n" +
                "            \"f5\": \"fv5\",\r\n" +
                "            \"f6\": \"fv6\"\r\n" +
                "        }\r\n" +
                "    ],\r\n" +
                "    \"complexObject\": {\r\n" +
                "        \"child1\": {\r\n" +
                "            \"p1\": \"v1\",\r\n" +
                "            \"p2\": \"v2\"\r\n" +
                "        },\r\n" +
                "        \"child2\": {\r\n" +
                "            \"p3\": \"v3\",\r\n" +
                "            \"p4\": \"v4\"\r\n" +
                "        }\r\n" +
                "    }\r\n" +
                "}";

        JsonPayloadIO io = new JsonPayloadIO();
        Map data = io.unmarshall(new ReaderInputStream(new StringReader(json)));
        Assert.assertNotNull(data);
        Assert.assertEquals("world", data.get("hello"));
        Assert.assertEquals("bar", data.get("foo"));
        Assert.assertNull(data.get("other"));
        Assert.assertTrue((Boolean) data.get("someBool"));
        Assert.assertEquals(123847281437L, data.get("someLong"));
        Assert.assertEquals("value-1", ((Map) data.get("object")).get("prop1"));
        Assert.assertEquals(toList(1,2,3,4,5), data.get("intArray"));
        Assert.assertEquals(toList("one", "two", "three"), data.get("strArray"));

        data = io.unmarshall(json.getBytes("UTF-8"));
        Assert.assertNotNull(data);
        Assert.assertEquals("world", data.get("hello"));
        Assert.assertEquals("bar", data.get("foo"));
        Assert.assertNull(data.get("other"));
        Assert.assertTrue((Boolean) data.get("someBool"));
        Assert.assertEquals(123847281437L, data.get("someLong"));
        Assert.assertEquals("value-1", ((Map) data.get("object")).get("prop1"));
        Assert.assertEquals(toList(1,2,3,4,5), data.get("intArray"));
        Assert.assertEquals(toList("one", "two", "three"), data.get("strArray"));
    }

    @Test
    public void testMarshall_Simple() throws Exception {
        Map<String, Comparable> data = new LinkedHashMap<>();
        data.put("hello", "world");
        data.put("foo", "bar");
        data.put("bool", true);

        JsonPayloadIO io = new JsonPayloadIO();
        byte[] bytes = io.marshall(data);
        String actual = new String(bytes);

        String expected = "{\"hello\":\"world\",\"foo\":\"bar\",\"bool\":true}";
        Assert.assertEquals(expected, actual);
    }

    @SuppressWarnings("unchecked")
    private List toList(Object ... items) {
        List rval = new ArrayList();
        for (Object item : items) {
            rval.add(item);
        }
        return rval;
    }

}
