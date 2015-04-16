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
package io.apiman.plugins.keycloak_oauth_policy.util;

/**
 * Hold an item.
 *
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <T> item type to hold
 */
public class Holder<T> {
    private T value;

    /**
     * Hold an item
     *
     * @param value the item to hold
     */
    public Holder(T value) {
        this.setValue(value);
    }

    public Holder() {
    }

    /**
     * @return the item
     */
    public T getValue() {
        return value;
    }

    /**
     * @param item the item to set
     */
    public Holder<T> setValue(T item) {
        this.value = item;
        return this;
    }
}
