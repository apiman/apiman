package io.apiman.gateway.engine.hazelcast.model;

import java.util.Objects;

/**
 * @author Pete Cornish
 */
public class Example {
    private String data;

    public Example() {
    }

    public Example(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Example)) return false;
        Example example = (Example) o;
        return Objects.equals(data, example.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
