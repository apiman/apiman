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
 * Claim
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "claimName", "claimValue" })
public class RequiredClaim {

    /**
     * Claim
     * <p>
     *
     *
     */
    @JsonProperty("claimName")
    private String claimName;
    /**
     * Value
     * <p>
     *
     *
     */
    @JsonProperty("claimValue")
    private String claimValue;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Claim
     * <p>
     *
     *
     * @return The claimName
     */
    @JsonProperty("claimName")
    public String getClaimName() {
        return claimName;
    }

    /**
     * Claim
     * <p>
     *
     *
     * @param claimName
     *            The claimName
     */
    @JsonProperty("claimName")
    public void setClaimName(String claimName) {
        this.claimName = claimName;
    }

    public RequiredClaim withClaimName(String claimName) {
        this.claimName = claimName;
        return this;
    }

    /**
     * Value
     * <p>
     *
     *
     * @return The claimValue
     */
    @JsonProperty("claimValue")
    public String getClaimValue() {
        return claimValue;
    }

    /**
     * Value
     * <p>
     *
     *
     * @param claimValue
     *            The claimValue
     */
    @JsonProperty("claimValue")
    public void setClaimValue(String claimValue) {
        this.claimValue = claimValue;
    }

    public RequiredClaim withClaimValue(String claimValue) {
        this.claimValue = claimValue;
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

    public RequiredClaim withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
