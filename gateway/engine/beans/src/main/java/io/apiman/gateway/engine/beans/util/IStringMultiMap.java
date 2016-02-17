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

package io.apiman.gateway.engine.beans.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A simple string multimap in which, optionally, multiple values can be stored
 * for a given key.
 *
 * Take note of the differences in behaviour between methods such as
 * {@link #put(String, String)}, which behaves like a traditional 1:1 map,
 * versus {@link #add(String, String)} which behaves like a 1:N multimap.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface IStringMultiMap extends Iterable<Map.Entry<String, String>> {

    /**
     * Set the entry indicated by <tt>key</tt> to <tt>value</tt>.
     * Any existing element(s) will be overwritten.
     *
     * @param key the key
     * @param value the value to put, which will overwrite any existing value(s)
     * @return a fluent reference to this map
     */
    IStringMultiMap put(String key, String value);

    /**
     * Set the entries indicated by <tt>key</tt> to mapped <tt>values</tt>.
     * Any existing element(s) will be overwritten.
     *
     * @param map the map
     * @return a fluent reference to this map
     */
    IStringMultiMap putAll(Map<String, String> map);

    /**
     * Add all entries from given map. This can include instances
     * where a given key maps to a list containing multiple
     * elements {@code f(x) => [A, B, C, D, ...]}.
     *
     *
     * @param map the map
     * @return a fluent reference to this map
     */
    IStringMultiMap addAll(Map<String, String> map);

    /**
     * Add all entries from given map. This can include instances
     * where a given key maps to a list containing multiple
     * elements {@code f(x) => [A, B, C, D, ...]}.
     *
     * @param map the CaseInsensitiveStringMultiMap
     * @return a fluent reference to this map
     */
    IStringMultiMap addAll(IStringMultiMap map);

    /**
     * Append an entry to the values mapped by <tt>key</tt> to <tt>value</tt>.
     * This is additive to any existing <tt>value</tt> elements.
     *
     * @param key the key
     * @param value the value, which will be inserted in addition to any existing element(s)
     * @return a fluent reference to this map
     */
    IStringMultiMap add(String key, String value);

    /**
     * Remove all value(s) for given <tt>key</tt>.
     *
     * @param key the key
     * @return a fluent reference to this map
     */
    IStringMultiMap remove(String key);

    /**
     * Get the first value for given <tt>key</tt>.
     *
     * @param key the key
     * @return the value if present, else null
     */
    String get(String key);

    /**
     * Get all value(s) for given <tt>key</tt>. Else an empty map.
     *
     * @param key the key
     * @return the list of value(s)
     */
    List<String> getAll(String key);

    /**
     * The size of map's keyset
     *
     * @return the number of keys
     */
    int size();

    /**
     * @return the list of all entries
     */
    List<Entry<String, String>> getEntries();

    /**
     * Whether a given key is present in the keyset.
     *
     * @param key the key
     * @return true if present, else false
     */
    boolean containsKey(String key);

    /**
     * Converts this multimap into a plain map. Note that this method will be lossy
     * if multiple values have been associated with any given key. It will select
     * only the first value.
     *
     * @return the map
     */
    Map<String, String> toMap();

    /**
     * Clears all entries from the multimap
     *
     * @return a fluent reference to this map
     */
    IStringMultiMap clear();

    /**
     * Whether the multimap is empty
     *
     * @return true if empty, else false.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the set of keys
     *
     * @return the set of keys
     */
    Set<String> keySet();

    /**
     * Get all entries for a given key. Notice that retrieving the entries in
     * this way allows the format of each key to be inspected.
     * <p>
     * For example:
     *
     * <pre>
     * {@code
     * mmap.add("test", "whispering");
     * mmap.add("TEST", "SHOUTING");
     * mmap.getAllEntries("test"); // => [test=whispering, TEST=SHOUTING]
     * }
     * </pre>
     *
     * @param key the keyname
     * @return the list of entries for a given key
     */
    List<Entry<String, String>> getAllEntries(String key);
}
