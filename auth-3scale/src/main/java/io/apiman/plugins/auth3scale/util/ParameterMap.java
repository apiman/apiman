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
package io.apiman.plugins.auth3scale.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * <p>
 * Hold a set of parameter and metrics for an AuthRep, Authorize, OAuth Authorize or Report.
 * <p/>
 * <p>
 * Each item consists of a name/value pair, where the value can be a String, An Array of ParameterMaps or another Parameter Map.
 * <p/>
 * <p>
 * E.g.  For an AuthRep:
 * </p>
 * <code>
 * ParameterMap params = new ParameterMap();<br/>
 * params.add("app_id", "app_1234");<br/>
 * ParameterMap usage = new ParameterMap();<br/>
 * usage.add("hits", "3");<br/>
 * params.add("usage", usage);<br/>
 * AuthorizeResponse response = serviceApi.authrep(params);<br/>
 * </code>
 * <p>
 * An example for a report might be:
 * <p/>
 * <code>
 * ParameterMap params = new ParameterMap();<br/>
 * params.add("app_id", "foo");<br/>
 * params.add("timestamp", fmt.print(new DateTime(2010, 4, 27, 15, 0)));<br/>
 * ParameterMap usage = new ParameterMap();<br/>
 * usage.add("hits", "1");<br/>
 * params.add("usage", usage);<br/>
 * ReportResponse response = serviceApi.report(params);<br/>
 * </code>
 */
public class ParameterMap {

    private HashMap<String, Object> data;
    private static final ParameterEncoder encoder = new ParameterEncoder();

    /**
     * Construct and empty ParameterMap
     */
    public ParameterMap() {
        // Note: use a linked hash map for more predictable serialization of the parameters (mostly for testing)
        data = new LinkedHashMap<>();
    }

    /**
     * Add a string value
     *
     * @param key the key
     * @param value the value
     */
    public void add(String key, String value) {
        data.put(key, value);
    }

    public <T> ParameterMap add(String key, T value) {
        data.put(key, value);
        return this;
    }

    /**
     * Add another ParameterMap
     *
     * @param key the key
     * @param map the map to add
     */
    public void add(String key, ParameterMap map) {
        data.put(key, map);
    }

    /**
     * Add an array of parameter maps
     *
     * @param key the key
     * @param array to add
     */
    public void add(String key, ParameterMap[] array) {
        data.put(key, array);
    }

    /**
     * Return the keys in a ParameterMap
     *
     * @return the keys
     */
    public Set<String> getKeys() {
        return data.keySet();
    }

    /**
     * Get the type of data item associated with the key
     *
     * @param key the key
     * @return STRING, MAP, ARRAY
     */
    public ParameterMapType getType(String key) {
        Class<?> clazz = data.get(key).getClass();
        if (clazz == String.class) {
            return ParameterMapType.STRING;
        }
        if (clazz == ParameterMap[].class) {
            return ParameterMapType.ARRAY;
        }
        if (clazz == ParameterMap.class) {
            return ParameterMapType.MAP;
        }
        if (clazz == Long.class) {
            return ParameterMapType.LONG;
        }
        throw new RuntimeException("Unknown object in parameters"); //$NON-NLS-1$
    }

    /**
     * Get the String associated with a key
     *
     * @param key the key
     * @return the value as string
     */
    public String getStringValue(String key) {
        switch (getType(key)) {
        case ARRAY:
            return Arrays.toString((ParameterMap[]) data.get(key));
        case LONG:
            return Long.toString((Long) data.get(key));
        case MAP:
            return ((ParameterMap) data.get(key)).toString(); //
        case STRING:
            return (String) data.get(key);
        }
        return null;
    }

    /**
     * Get the map associated with a key
     *
     * @param key the key
     * @return the map value
     */
    public ParameterMap getMapValue(String key) {
        return (ParameterMap) data.get(key);
    }

    /**
     * Get the array associated with a key.
     *
     * @param key the key
     * @return the array associated with a key
     */
    public ParameterMap[] getArrayValue(String key) {
        return (ParameterMap[]) data.get(key);
    }

    public long getLongValue(String key) {
        return (long) data.get(key);
    }

    public void setLongValue(String key, long value) {
        data.put(key, value);
    }

    /**
     * Return the number of elements in the map.
     *
     * @return the size
     */
    public int size() {
        return data.size();
    }

    public String encode() {
        return encoder.encode(this);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "ParameterMap [data=" + data + "]";
    }
}
