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
package io.apiman.manager.api.beans.apis.dto;

import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A KV tag pair DTO.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Key value tag pair")
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
