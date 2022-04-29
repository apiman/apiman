/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.beans.apis;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Hibernate;

/**
 * Simple tag with value.
 */
@Entity
@Table(name = "kv_tags")
public class KeyValueTag {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Nullable
    @Column(name = "value", nullable = true, updatable = true)
    private String value;

    public KeyValueTag() {
    }

    public Long getId() {
        return id;
    }

    public KeyValueTag setId(Long id) {
        this.id = id;
        return this;
    }

    public String getKey() {
        return key;
    }

    public KeyValueTag setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public KeyValueTag setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        KeyValueTag that = (KeyValueTag) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}