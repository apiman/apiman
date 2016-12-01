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
package io.apiman.plugins.jwt.beans;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Header
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "headers", "field" })
public class ForwardAuthInfo {

    /**
     * Header
     * <p>
     *
     *
     */
    @JsonProperty("headers")
    private String headers;
    /**
     * Field
     * <p>
     *
     *
     */
    @JsonProperty("field")
    private String field;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Header
     * <p>
     *
     *
     * @return The headers
     */
    @JsonProperty("headers")
    public String getHeaders() {
        return headers;
    }

    /**
     * Header
     * <p>
     *
     *
     * @param headers
     *            The headers
     */
    @JsonProperty("headers")
    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public ForwardAuthInfo withHeaders(String headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Field
     * <p>
     *
     *
     * @return The field
     */
    @JsonProperty("field")
    public String getField() {
        return field;
    }

    /**
     * Field
     * <p>
     *
     *
     * @param field
     *            The field
     */
    @JsonProperty("field")
    public void setField(String field) {
        this.field = field;
    }

    public ForwardAuthInfo withField(String field) {
        this.field = field;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public ForwardAuthInfo withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
