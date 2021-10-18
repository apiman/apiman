package io.apiman.manager.api.beans.apis.dto;

import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyValueTagDto {
    @NotNull
    private String key;
    @Nullable
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
