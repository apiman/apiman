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

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class CaseInsensitiveStringMultiMapTest {

    @Test
    public void isNotEmpty() {
        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        actual.add("x", "y");
        Assert.assertFalse(actual.isEmpty());
    }

    @Test
    public void isEmpty() {
        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        actual.add("x", "y").add("x", "z");
        actual.remove("x");
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void shouldAddAllEntriesFromMultiMap() {
        CaseInsensitiveStringMultiMap putMap = new CaseInsensitiveStringMultiMap();
        putMap.add("foo", "bar").add("api", "man").add("marc", "red-hat").add("marc", "savy");

        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        actual.addAll(putMap);

        Assert.assertEquals(
                expected(ent("foo", "bar"), ent("api", "man"), ent("marc", "red-hat"), ent("marc", "savy")),
                toSet(actual.getEntries()));
    }

    @Test
    public void shouldPutAllEntriesFromMap() {
        Map<String, String> putAllMap = new HashMap<>();
        putAllMap.put("a", "x");
        putAllMap.put("b", "y");
        putAllMap.put("c", "z");

        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        actual.putAll(putAllMap);

        Assert.assertEquals(expected(ent("a", "x"), ent("b", "y"), ent("c", "z")),
                toSet(actual.getEntries()));
    }

    @Test
    public void shouldReturnTrueWhenContainsKey() throws Exception {
        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        actual.put("a", "b").put("c", "d").put("a", "x");
        Assert.assertTrue(actual.containsKey("a"));
    }

    @Test
    public void shouldReturnFalseWhenNotContainsKey() throws Exception {
        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap(1);
        actual.put("a", "b").put("c", "d").put("a", "x");
        Assert.assertFalse(actual.containsKey("cc"));
    }

    @Test
    public void shouldReturnFalseWhenContainsKeyOnEmptyMap() {
        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        Assert.assertFalse(actual.containsKey("z"));
    }

    // Note: even with multiple values for a given key, still only counts as 1 in keyset.
    @Test
    public void keySet() throws Exception {
        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        actual.put("a", "b").put("c", "d").put("a", "x");
        Assert.assertEquals(expected("a", "c"), actual.keySet());
        actual.remove("c");
        Assert.assertEquals(expected("a"), actual.keySet());
    }

    @Test
    public void shouldGenerateSensibleToString() throws Exception {
        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        actual.add("a", "b").add("c", "d").add("a", "x");
        Assert.assertEquals("{a => [b, x], c => [d]}", actual.toString());
    }

    @Test
    public void shouldReturnEmptySetWhenNoEntries() {
        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        Assert.assertEquals(Collections.emptyList(), actual.getAllEntries("Foo"));
        Assert.assertEquals(Collections.emptyList(), actual.getAll("Foo"));
        actual.put("Not", "Thingie");
        Assert.assertEquals(Collections.emptyList(), actual.getAllEntries("Foo"));
        Assert.assertEquals(Collections.emptyList(), actual.getAll("Foo"));
    }

    @Test
    public void shouldAllowMultipleValuesWhenUsingAdd() {
        Set<Entry<String, String>> expected = expected(ent("x", "foo"), ent("x", "boo"), ent("x", "woo"), ent("x", "new"), ent("X", "SHOUTING"));

        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        actual.add("x", "foo").add("x", "boo").add("x", "woo").add("x", "new").add("Y", "blerg").add("X", "SHOUTING");

        Assert.assertEquals(expected, toSet(actual.getAllEntries("x")));
    }

    @Test
    public void shouldOverwriteValueWhenUsingPut() {
        Set<Entry<String, String>> expected = expected(ent("x", "foo"));

        CaseInsensitiveStringMultiMap actual = new CaseInsensitiveStringMultiMap();
        actual.add("x", "y").add("x", "z").put("x", "foo");

        Assert.assertEquals(expected, toSet(actual.getAllEntries("x")));
    }

    @Test // SHOULD include multiple values if they were defined (and matching keys with preserved formatting)
    public void shouldIterateOverEntries() {
        Set<Entry<String, String>> expected = expected(ent("x", "foo"), ent("hello", "goodbye"),
                ent("b", "x"), ent("B", "X"));

        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap();
        mmap.add("x", "foo").add("hello", "goodbye").add("b", "x").add("B", "X");

        Set<Entry<String, String>> actual = toSet(StreamSupport.stream(mmap.spliterator(), false).collect(Collectors.toList()));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAllValuesForKey() {
        Set<String> expected = expected("foo", "goodbye", "justvalue");

        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap();
        mmap.add("x", "foo").add("x", "goodbye").add("X", "justvalue");

        Assert.assertEquals(expected, toSet(mmap.getAll("x")));
    }

    @Test
    public void shouldAddEntriesFromMap() {
        List<Entry<String, String>> expected = Arrays.asList(ent("a", "b"), ent("x", "y"), ent("y", "z"));

        Map<String, String> in = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L; {
            put("x", "y");
            put("y", "z");
            put("a", "b");
        }};

        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap();
        mmap.addAll(in);

        List<Entry<String, String>> c = mmap.getEntries();

        c.sort((e1, e2) -> {
            int kComp = e1.getKey().compareTo(e2.getKey());
            if (kComp == 0) return 0;
            return e1.getValue().compareTo(e2.getValue());
        });

        Assert.assertEquals(expected, c);
    }

    @Test // Last head/value pair should be taken, others ignored.
    public void convertToMap() {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("a", "x");
        expected.put("b", "y");
        expected.put("C", "X_X"); // Last in should win

        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap();
        // Additional entries should be ignored
        mmap.add("a", "x").add("b", "y").add("c", "z").add("c", "XX").add("C", "X_X");
        Map<String, String> actual = mmap.toMap();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void removeEntries() {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("C", "X_X");

        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap();
        // Additional entries should be ignored
        mmap.add("a", "x").add("a", "y").add("A", "z").add("a", "XX").add("C", "X_X");
        mmap.remove("a");
        Map<String, String> actual = mmap.toMap();

        Assert.assertEquals(expected, actual);
    }

    @Test // A and C will have same bucket by virtue of size 1 array
    public void removeEntriesWithCollision() {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("C", "X_X");
        expected.put("b", "b");
        expected.put("aa", "x");

        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap(1);
        // Additional entries should be ignored
        mmap.add("a", "x").add("a", "y").add("A", "z").add("a", "Problem").add("C", "X_X").add("aa", "x").add("b", "b");

        mmap.remove("a");
        Map<String, String> actual = mmap.toMap();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldDistinguishBetweenCollisionsWithGetAll() {
        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap(1);
        mmap.add("Access-Control-Allow-Origin", "*").add("Server", "WildFly/10");
        Assert.assertEquals(2, mmap.size());
        Assert.assertEquals(mmap.getAll("Access-Control-Allow-Origin").size(), 1);
        Assert.assertEquals(mmap.getAll("Access-Control-Allow-Origin").get(0), "*");
        Assert.assertEquals(mmap.getAll("Server").size(), 1);
    }

    @Test
    public void shouldDistinguishBetweenCollisionsWithGetEntries() {
        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap(1);
        mmap.add("Access-Control-Allow-Origin", "*").add("Server", "WildFly/10");
        Assert.assertEquals(2, mmap.size());
        Assert.assertEquals(mmap.getAllEntries("Access-Control-Allow-Origin").size(), 1);
        Assert.assertEquals(mmap.getAllEntries("Access-Control-Allow-Origin").get(0).getValue(), "*");
        Assert.assertEquals(mmap.getAllEntries("Server").size(), 1);
    }

    @Test
    public void getShouldReturnTheLastValueOnly() {
        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap();
        // Additional entries should be ignored
        mmap.add("a", "x").add("b", "y").add("c", "z").add("c", "XX").add("C", "X_X");
        Assert.assertEquals("X_X", mmap.get("c"));
    }

    @Test
    public void getKeyCount() {
        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap();
        // Additional entries should be ignored
        mmap.add("a", "x").add("b", "y").add("c", "z").add("c", "XX").add("C", "X_X");
        Assert.assertEquals(3, mmap.size());
    }

    @Test
    public void getKeyCountAfterRemove() {
        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap();
        // Additional entries should be ignored
        mmap.add("a", "x").add("b", "y").add("c", "z").add("c", "XX").add("C", "X_X");
        Assert.assertEquals(3, mmap.size());
        mmap.remove("c");
        Assert.assertEquals(2, mmap.size());
        mmap.remove("a");
        Assert.assertEquals(1, mmap.size());
        mmap.add("a", "x").add("c", "c");
        Assert.assertEquals(3, mmap.size());
        mmap.add("d", "d").add("d", "f");
        Assert.assertEquals(4, mmap.size());
        mmap.remove("c");
        Assert.assertEquals(3, mmap.size());
        // No such key
        mmap.remove("XXXXXXX");
        Assert.assertEquals(3, mmap.size());
    }

    @Test
    public void emptyAndRefill() {
        CaseInsensitiveStringMultiMap mmap = new CaseInsensitiveStringMultiMap();
        // Additional entries should be ignored
        mmap.add("a", "x").add("b", "y").add("c", "z").add("c", "XX").add("C", "X_X");
        mmap.remove("a");
        mmap.remove("b");
        mmap.remove("c");
        // Try to remove an element, even when the map is completely empty
        mmap.remove("a");
        mmap.remove("b");
        mmap.remove("c");
        Assert.assertEquals(0, mmap.size());
        mmap.add("a", "x");
        mmap.clear();
        mmap.add("a", "x");
        Assert.assertEquals(1, mmap.size());
    }

    private Entry<String, String> ent(String k, String v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    @SafeVarargs
    private static <T> Set<T> expected(T... entries) {
        return new HashSet<>(Arrays.asList(entries));
    }

    private static <T> Set<T> toSet(List<T> list) {
        return new HashSet<>(list);
    }
}
