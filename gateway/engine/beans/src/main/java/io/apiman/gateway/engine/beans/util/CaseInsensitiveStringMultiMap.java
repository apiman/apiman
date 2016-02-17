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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple multimap able to accept multiple values for given key.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class CaseInsensitiveStringMultiMap implements IStringMultiMap {
    private Map<String, Element> elemMap;

    public CaseInsensitiveStringMultiMap() {
        elemMap = new LinkedHashMap<>();
    }

    public CaseInsensitiveStringMultiMap(int sizeHint) {
        elemMap = new LinkedHashMap<>(sizeHint);
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return getEntries().iterator();
    }

    @Override
    public IStringMultiMap put(String key, String value) {
        elemMap.put(key.toLowerCase(), new Element(key, value));
        return this;
    }

    @Override
    public IStringMultiMap putAll(Map<String, String> map) {
        map.entrySet().stream()
                      .forEachOrdered(pair -> add(pair.getKey(), pair.getValue()));
        return this;
    }

    @Override
    public IStringMultiMap add(String key, String value) {
        String lowerKey = key.toLowerCase();
        if (elemMap.containsKey(lowerKey)) {
            elemMap.get(lowerKey).add(key, value);
        } else {
            elemMap.put(lowerKey, new Element(key, value));
        }
        return this;
    }

    @Override
    public IStringMultiMap remove(String key) {
        elemMap.remove(key.toLowerCase());
        return this;
    }

    @Override
    public IStringMultiMap addAll(Map<String, String> inmap) {
        inmap.entrySet().stream()
             .forEachOrdered(pair -> put(pair.getKey(), pair.getValue()));
        return this;
    }

    @Override
    public String get(String key) {
        String lowerKey = key.toLowerCase();
        if (elemMap.containsKey(lowerKey)) {
            return elemMap.get(lowerKey).getValue(); // Just return the FIRST value, ignore all others
        }
        return null;
    }

    @Override
    public List<Entry<String, String>> getAllEntries(String key) {
        String lowerKey = key.toLowerCase();
        if (elemMap.containsKey(lowerKey)) {
            return elemMap.get(lowerKey).getAllEntries();
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getAll(String key) {
        String lowerKey = key.toLowerCase();
        if (elemMap.containsKey(lowerKey)) {
            return elemMap.get(lowerKey).getAllValues();
        }
        return Collections.emptyList();
    }

    @Override
    public int size() {
        return elemMap.size();
    }

    private static final class Element implements Iterable<Entry<String, String>> {
        private AbstractMap.SimpleImmutableEntry<String, String> entry;
        private Element next = null;

        public Element(String key, String value) {
            entry = new AbstractMap.SimpleImmutableEntry<>(key, value);
        }

        @Override
        public Iterator<Entry<String, String>> iterator() {
            return getAllEntries().iterator();
        }

        public List<Entry<String, String>> getAllEntries() {
            List<Entry<String, String>> allElems = new ArrayList<>();
            for (Element elem = this; elem != null; elem = elem.getNext()) {
              allElems.add(elem.getEntry());
            }
            return allElems;
        }

        public List<String> getAllValues() {
            List<String> allElems = new ArrayList<>();
            for (Element elem = this; elem != null; elem = elem.getNext()) {
              allElems.add(elem.getValue());
            }
            return allElems;
        }

        public void add(String key, String value) {
            Element oldLastElem = getLast();
            Element newElem = new Element(key, value);
            oldLastElem.next = newElem;
        }

        public Element getLast() {
            Element elem = this;
            while (elem.next != null) {
                elem = elem.next;
            }
            return elem;
        }

        public Element getNext() {
            return next;
        }

        Entry<String, String> getEntry() {
            return entry;
        }

        String getValue() {
            return entry.getValue();
        }

        public String getKey() {
            return entry.getKey();
        }
    }

    @Override
    public List<Entry<String, String>> getEntries() {
        List<Entry<String, String>> entryList = new ArrayList<>(elemMap.size());
        // Inspect all pairs of String to List Head Element
        for (Entry<String, Element> elemMapPair : elemMap.entrySet()) {
            // Retrieve all Elements and use Name and Value from *Element* to reconstruct original construction of K and V.
            for(Element elem = elemMapPair.getValue(); elem != null; elem = elem.getNext()) {
                entryList.add(elem.getEntry());
            }
        }
        return entryList;
    }

    @Override
    public Map<String, String> toMap() {
        return elemMap.entrySet().stream()
                                 .collect(Collectors.toMap(k -> k.getValue().getKey(),  // Take only head key
                                                           g -> g.getValue().getValue())); // Take only head value
    }

    @Override
    public boolean containsKey(String key) {
        return elemMap.containsKey(key.toLowerCase());
    }

    @Override
    public Set<String> keySet() {
        return elemMap.keySet();
    }

    @Override
    public IStringMultiMap clear() {
        elemMap.clear();
        return this;
    }

    @Override
    public String toString() {
        String elems = keySet().stream()
                .map(key -> getAllEntries(key))
                .map(pairs -> pairs.get(0).getKey() + " => [" + joinValues(pairs) + "]")
                .collect(Collectors.joining(", "));
        return "{" + elems + "}";
    }

    private String joinValues(List<Entry<String, String>> pairs) {
        return pairs.stream().map(Entry::getValue).collect(Collectors.joining(", "));
    }
}
