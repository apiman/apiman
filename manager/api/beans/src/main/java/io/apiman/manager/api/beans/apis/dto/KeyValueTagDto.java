package io.apiman.manager.api.beans.apis.dto;

import java.util.StringJoiner;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class KeyValueTagDto {
    @NotNull
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public KeyValueTagDto setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public KeyValueTagDto setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", KeyValueTagDto.class.getSimpleName() + "[", "]")
                .add("key='" + key + "'")
                .add("value='" + value + "'")
                .toString();
    }
}
