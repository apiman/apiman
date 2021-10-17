package io.apiman.manager.api.beans.apis;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.Hibernate;
import org.hibernate.annotations.NaturalId;

/**
 * Simple tag with value.
 */
@Entity
public class KeyValueTag {
    @Id
    @GeneratedValue
    private Long id;

    @NaturalId
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