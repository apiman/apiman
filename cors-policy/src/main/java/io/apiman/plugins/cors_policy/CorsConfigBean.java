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
package io.apiman.plugins.cors_policy;

import io.apiman.plugins.cors_policy.util.HttpHelper;
import io.apiman.plugins.cors_policy.util.InsensitiveLinkedHashSet;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * CORS Policy Configuration
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "errorOnCorsFailure", "allowOrigin", "allowCredentials", "exposeHeaders",
        "allowHeaders", "allowMethods", "maxAge" })
public class CorsConfigBean implements Serializable {
    private static final long serialVersionUID = 6655123241544127400L;

    /**
     * Terminate on CORS error
     * <p>
     * When true, any request that fails CORS validation will be terminated with an appropriate error. When
     * false, the request will still be sent to the backend API, but the browser will be left to enforce
     * the CORS failure. In both cases valid CORS headers will be set.
     *
     */
    @JsonProperty("errorOnCorsFailure")
    private boolean errorOnCorsFailure = true;
    /**
     * Access-Control-Allow-Origin
     * <p>
     * List of origins permitted to make CORS requests through the gateway. By default same-origin is
     * permitted, and cross-origin is forbidden. An entry of * permits all CORS requests.
     *
     */
    @JsonProperty("allowOrigin")
    @JsonDeserialize(as = InsensitiveLinkedHashSet.class)
    private Set<String> allowOrigin = new InsensitiveLinkedHashSet();
    /**
     * Access-Control-Allow-Credentials
     * <p>
     * Whether response may be exposed when the `credentials` flag is set to true on the request.
     *
     */
    @JsonProperty("allowCredentials")
    private boolean allowCredentials = false;
    /**
     * Access-Control-Expose-Headers
     * <p>
     * Which non-simple headers the browser may expose during CORS.
     *
     */
    @JsonProperty("exposeHeaders")
    @JsonDeserialize(as = InsensitiveLinkedHashSet.class)
    private Set<String> exposeHeaders = new InsensitiveLinkedHashSet();
    /**
     * Access-Control-Allow-Headers
     * <p>
     * In response to preflight request, which headers can be used during actual request.
     *
     */
    @JsonProperty("allowHeaders")
    @JsonDeserialize(as = InsensitiveLinkedHashSet.class)
    private Set<String> allowHeaders = new InsensitiveLinkedHashSet();
    /**
     * Access-Control-Allow-Methods
     * <p>
     * In response to preflight request, which methods can be used during actual request.
     *
     */
    @JsonProperty("allowMethods")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> allowMethods = new LinkedHashSet<>();

    /**
     * Access-Control-Max-Age
     * <p>
     * How long preflight request can be cached in delta seconds
     *
     */
    @JsonProperty("maxAge")
    private Integer maxAge;

    /**
     * Terminate on CORS error
     * <p>
     * When true, any request that fails CORS validation will be terminated with an appropriate error. When
     * false, the request will still be sent to the backend API, but the browser will be left to enforce
     * the CORS failure. In both cases valid CORS headers will be set.
     *
     * @return The errorOnCorsFailure
     */
    @JsonProperty("errorOnCorsFailure")
    public boolean isErrorOnCorsFailure() {
        return errorOnCorsFailure;
    }

    /**
     * Terminate on CORS error
     * <p>
     * When true, any request that fails CORS validation will be terminated with an appropriate error. When
     * false, the request will still be sent to the backend API, but the browser will be left to enforce
     * the CORS failure. In both cases valid CORS headers will be set.
     *
     * @param errorOnCorsFailure The errorOnCorsFailure
     */
    @JsonProperty("errorOnCorsFailure")
    public void setErrorOnCorsFailure(boolean errorOnCorsFailure) {
        this.errorOnCorsFailure = errorOnCorsFailure;
    }

    /**
     * Access-Control-Allow-Origin
     * <p>
     * List of origins permitted to make CORS requests through the gateway. By default same-origin is
     * permitted, and cross-origin is forbidden. An entry of * permits all CORS requests.
     *
     * @return The allowOrigin
     */
    @JsonProperty("allowOrigin")
    public Set<String> getAllowOrigin() {
        return allowOrigin;
    }

    /**
     * Access-Control-Allow-Origin
     * <p>
     * List of origins permitted to make CORS requests through the gateway. By default same-origin is
     * permitted, and cross-origin is forbidden. An entry of * permits all CORS requests.
     *
     * @param allowOrigin The allowOrigin
     */
    @JsonProperty("allowOrigin")
    public void setAllowOrigin(Set<String> allowOrigin) {
        this.allowOrigin = allowOrigin;
    }

    /**
     * Access-Control-Allow-Credentials
     * <p>
     * Whether response may be exposed when the `credentials` flag is set to true on the request.
     *
     * @return The allowCredentials
     */
    @JsonProperty("allowCredentials")
    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    /**
     * Access-Control-Allow-Credentials
     * <p>
     * Whether response may be exposed when the `credentials` flag is set to true on the request.
     *
     * @param allowCredentials The allowCredentials
     */
    @JsonProperty("allowCredentials")
    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    /**
     * Access-Control-Expose-Headers
     * <p>
     * Which non-simple headers the browser may expose during CORS.
     *
     * @return The exposeHeaders
     */
    @JsonProperty("exposeHeaders")
    public Set<String> getExposeHeaders() {
        return exposeHeaders;
    }

    /**
     * Access-Control-Expose-Headers
     * <p>
     * Which non-simple headers the browser may expose during CORS.
     *
     * @param exposeHeaders The exposeHeaders
     */
    @JsonProperty("exposeHeaders")
    public void setExposeHeaders(Set<String> exposeHeaders) {
        this.exposeHeaders = exposeHeaders;
    }

    /**
     * Access-Control-Allow-Headers
     * <p>
     * In response to preflight request, which headers can be used during actual request.
     *
     * @return The allowHeaders
     */
    @JsonProperty("allowHeaders")
    public Set<String> getAllowHeaders() {
        return allowHeaders;
    }

    /**
     * Access-Control-Allow-Headers
     * <p>
     * In response to preflight request, which headers can be used during actual request.
     *
     * @param allowHeaders The allowHeaders
     */
    @JsonProperty("allowHeaders")
    public void setAllowHeaders(Set<String> allowHeaders) {
        this.allowHeaders = allowHeaders;
    }

    /**
     * Access-Control-Allow-Methods
     * <p>
     * In response to preflight request, which methods can be used during actual request.
     *
     * @return The allowMethods
     */
    @JsonProperty("allowMethods")
    public Set<String> getAllowMethods() {
        return allowMethods;
    }

    /**
     * Access-Control-Allow-Methods
     * <p>
     * In response to preflight request, which methods can be used during actual request.
     *
     * @param allowMethods The allowMethods
     */
    @JsonProperty("allowMethods")
    public void setAllowMethods(Set<String> allowMethods) {
        this.allowMethods.addAll(allowMethods);
    }

    /**
     * Access-Control-Max-Age
     * <p>
     * How long preflight request can be cached in delta seconds
     *
     * @return The maxAge
     */
    @JsonProperty("maxAge")
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * Access-Control-Max-Age
     * <p>
     * How long preflight request can be cached in delta seconds
     *
     * @param maxAge The maxAge
     */
    @JsonProperty("maxAge")
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Is allowed if: * (any), host == origin (not all browsers do this), is on allowed list.
     *
     * @param host the host
     * @param origin the origin of request
     * @return true if origin is allowed
     */
    public boolean isAllowedOrigin(String origin, String host) {
        return (host.equals(origin) || allowOrigin.contains("*") || allowOrigin.contains(origin)); //$NON-NLS-1$
    }

    /**
     * A simple method as defined by the spec.
     *
     * @param method the methods
     * @return true if simple method(s)
     */
    public boolean isAllowedMethod(String... method) {
        return method != null && HttpHelper.containsAll(allowMethods, method);
    }

    /**
     * Is an allowed header, either by user definition or the spec. Generally browsers should not ask for
     * fields which are always allowed but we handle that anyway.
     *
     * @param header the headers
     * @return true if simple header(s)
     */
    public boolean isAllowedHeader(String... header) {
        return header != null &&
                (HttpHelper.containsAll(allowHeaders, header) || HttpHelper.isSimpleHeader(header));
    }
}
