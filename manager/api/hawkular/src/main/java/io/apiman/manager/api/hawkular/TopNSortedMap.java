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

package io.apiman.manager.api.hawkular;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author eric.wittmann@gmail.com
 */
public class TopNSortedMap<K extends Comparable, V extends Comparable> implements Map<K, V> {
    
    private final int maxItems;
    private final Map<K, V> index;
    private final SortedSet<KeyValue<K, V>> items;
    
    /**
     * Constructor.
     */
    public TopNSortedMap(int size) {
        this.maxItems = size;
        this.index = new HashMap<K, V>(size+1);
        this.items = new TreeSet<KeyValue<K, V>>();
    }

    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return index.size();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return index.isEmpty();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return index.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return index.containsValue(value);
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public V get(Object key) {
        return index.get(key);
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put(K key, V value) {
        V oldItem = index.put(key, value);
        items.add(new KeyValue<>(key, value));
        
        if (size() > this.maxItems) {
            popLastItem();
        }
        
        return oldItem;
    }

    /**
     * Removes the last item in the list.
     */
    private void popLastItem() {
        KeyValue<K,V> lastKV = items.last();
        items.remove(lastKV);
        index.remove(lastKV.key);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public V remove(Object key) {
        KeyValue<K, V> foundKV = null;
        
        for (KeyValue<K, V> kv : this.items) {
            if (kv.key.equals(key)) {
                foundKV = kv;
                break;
            }
        }
        
        if (foundKV == null) {
            return null;
        } else {
            index.remove(foundKV.key);
            items.remove(foundKV);
            return foundKV.value;
        }
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (java.util.Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        index.clear();
        items.clear();
    }

    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<K> keySet() {
        return index.keySet();
    }

    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<V> values() {
        return index.values();
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return index.entrySet();
    }

    /**
     * @return a simple map of the data
     */
    public Map<K, V> toMap() {
        return index;
    }


    private static final class KeyValue<K extends Comparable, V extends Comparable> implements Comparable<KeyValue<K, V>> {
        
        protected final K key;
        protected final V value;
        
        /**
         * Constructor.
         */
        public KeyValue(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(KeyValue other) {
            int rval = this.value.compareTo(other.value);
            if (rval == 0) {
                rval = this.key.compareTo(other.key);
            }
            return rval * -1;
        }
        
        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return this.key.equals(((KeyValue) obj).key);
        }
    }

}
