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
package io.apiman.plugins.cors_policy.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A simple case-insensitive linked hash map
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class InsensitiveLinkedHashSet extends LinkedHashSet<String> implements Serializable, Set<String> {

    private static final long serialVersionUID = -3273143085866100001L;
    private Set<String> innerSet = new HashSet<>();

    public InsensitiveLinkedHashSet() {
        super();
    }

    public InsensitiveLinkedHashSet(Collection<? extends String> entries) {
        super(entries.size());
        addAll(entries);
    }

    @Override
    public boolean add(String entry) {
        if (entry == null || innerSet.contains(entry.toLowerCase())) {
            return false;
        }

        innerSet.add(entry.toLowerCase());
        return super.add(entry);
    }

    @Override
    public boolean addAll(Collection<? extends String> entries) {
        boolean success = true;

        for (String entry : entries) {
            success = success && add(entry);
        }

        return success;
    }

    @Override
    public boolean containsAll(Collection<?> entries) {
        boolean success = true;

        for (Object entry : entries) {
            if (entry instanceof String) {
                success = success && contains(entry);
            }
        }

        return success;
    }

    @Override
    public boolean contains(Object candidate) {

        if (candidate == null && !(candidate instanceof String))
            return false;

        return innerSet.contains(((String) candidate).toLowerCase());
    }

}
