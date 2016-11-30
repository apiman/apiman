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

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.keycloak.common.util.PemUtils;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * JWT Authentication Policy Configuration
 * <p>
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "requireJWT", "requireTransportSecurity", "stripTokens", "signingKeyString", "requiredClaims", "forwardAuthInfo" })
public class JWTPolicyBean {

    /**
     * Require JWT
     * <p>
     * Terminate request if no JWT provided.
     *
     */
    @JsonProperty("requireJWT")
    private Boolean requireJWT = true;
    /**
     * Require Transport Security
     * <p>
     * Any request used without transport security will be rejected. JWT requires transport
     * security (e.g. TLS, SSL) to provide protection against a variety of attacks. It is
     * strongly advised this option be switched on.
     *
     */
    @JsonProperty("requireTransportSecurity")
    private Boolean requireTransportSecurity = true;
    /**
     * Strip Tokens
     * <p>
     * Remove any Authorization header or token query parameter before forwarding traffic to
     * the API.
     *
     */
    @JsonProperty("stripTokens")
    private Boolean stripTokens = false;
    /**
     * Signing Key
     * <p>
     * To validate JWT. Must be Base-64 encoded.
     *
     */
    @JsonProperty("signingKeyString")
    private String signingKeyString;
    private Key signingKey;
    /**
     * Required Claims
     * <p>
     * Require claims
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims, custom claims and ID token fields (case sensitive).
     *
     */
    @JsonProperty("requiredClaims")
    private List<RequiredClaim> requiredClaims = new ArrayList<>();
    /**
     * Forward Keycloak Token Information
     * <p>
     * Fields from the token can be set as headers and forwarded to the API. All
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims, custom claims and ID token fields are available (case
     * sensitive). A special value of access_token will forward the entire encoded token.
     *
     */
    @JsonProperty("forwardAuthInfo")
    private List<ForwardAuthInfo> forwardAuthInfo = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private long allowedClockSkew;

    /**
     * Require JWT
     * <p>
     * Terminate request if no JWT provided.
     *
     * @return The requireJWT
     */
    @JsonProperty("requireJWT")
    public Boolean getRequireJWT() {
        return requireJWT;
    }

    /**
     * Require JWT
     * <p>
     * Terminate request if no JWT provided.
     *
     * @param requireJWT
     *            The requireJWT
     */
    @JsonProperty("requireJWT")
    public void setRequireJWT(Boolean requireJWT) {
        this.requireJWT = requireJWT;
    }

    public JWTPolicyBean withRequireJWT(Boolean requireJWT) {
        this.requireJWT = requireJWT;
        return this;
    }

    /**
     * Require Transport Security
     * <p>
     * Any request used without transport security will be rejected. JWT requires transport
     * security (e.g. TLS, SSL) to provide protection against a variety of attacks. It is
     * strongly advised this option be switched on.
     *
     * @return The requireTransportSecurity
     */
    @JsonProperty("requireTransportSecurity")
    public Boolean getRequireTransportSecurity() {
        return requireTransportSecurity;
    }

    /**
     * Require Transport Security
     * <p>
     * Any request used without transport security will be rejected. JWT requires transport
     * security (e.g. TLS, SSL) to provide protection against a variety of attacks. It is
     * strongly advised this option be switched on.
     *
     * @param requireTransportSecurity
     *            The requireTransportSecurity
     */
    @JsonProperty("requireTransportSecurity")
    public void setRequireTransportSecurity(Boolean requireTransportSecurity) {
        this.requireTransportSecurity = requireTransportSecurity;
    }

    public JWTPolicyBean withRequireTransportSecurity(Boolean requireTransportSecurity) {
        this.requireTransportSecurity = requireTransportSecurity;
        return this;
    }

    /**
     * Strip Tokens
     * <p>
     * Remove any Authorization header or token query parameter before forwarding traffic to
     * the API.
     *
     * @return The stripTokens
     */
    @JsonProperty("stripTokens")
    public Boolean getStripTokens() {
        return stripTokens;
    }

    /**
     * Strip Tokens
     * <p>
     * Remove any Authorization header or token query parameter before forwarding traffic to
     * the API.
     *
     * @param stripTokens
     *            The stripTokens
     */
    @JsonProperty("stripTokens")
    public void setStripTokens(Boolean stripTokens) {
        this.stripTokens = stripTokens;
    }

    public JWTPolicyBean withStripTokens(Boolean stripTokens) {
        this.stripTokens = stripTokens;
        return this;
    }

    /**
     * Signing Key
     * <p>
     * To validate JWT. Must be Base-64 encoded.
     *
     * @return The signingKeyString
     */
    @JsonProperty("signingKeyString")
    public String getSigningKeyString() {
        return signingKeyString;
    }

    public Key getSigningKey() {
        return signingKey;
    }

    /**
     * Signing Key String
     * <p>
     * To validate JWT. Must be Base-64 encoded.
     *
     * @param signingKeyString
     *            The signingKeyString
     * @throws Exception key parsing exceptions
     */
    @JsonProperty("signingKeyString")
    public void setSigningKeyString(String signingKeyString) throws Exception {
        if (signingKey == null) {
            signingKey = PemUtils.decodePublicKey(signingKeyString);
        }
        this.signingKeyString = signingKeyString;
    }

    public JWTPolicyBean withSigningKeyString(String signingKeyString) {
        this.signingKeyString = signingKeyString;
        return this;
    }

    /**
     * Required Claims
     * <p>
     * Require claims
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims, custom claims and ID token fields (case sensitive).
     *
     * @return The requiredClaims
     */
    @JsonProperty("requiredClaims")
    public List<RequiredClaim> getRequiredClaims() {
        return requiredClaims;
    }

    /**
     * Required Claims
     * <p>
     * Require claims
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims, custom claims and ID token fields (case sensitive).
     *
     * @param requiredClaims
     *            The requiredClaims
     */
    @JsonProperty("requiredClaims")
    public void setRequiredClaims(List<RequiredClaim> requiredClaims) {
        this.requiredClaims = requiredClaims;
    }

    public JWTPolicyBean withRequiredClaims(List<RequiredClaim> requiredClaims) {
        this.requiredClaims = requiredClaims;
        return this;
    }

    /**
     * Forward Keycloak Token Information
     * <p>
     * Fields from the token can be set as headers and forwarded to the API. All
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims, custom claims and ID token fields are available (case
     * sensitive). A special value of access_token will forward the entire encoded token.
     *
     * @return The forwardAuthInfo
     */
    @JsonProperty("forwardAuthInfo")
    public List<ForwardAuthInfo> getForwardAuthInfo() {
        return forwardAuthInfo;
    }

    /**
     * Forward Keycloak Token Information
     * <p>
     * Fields from the token can be set as headers and forwarded to the API. All
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims, custom claims and ID token fields are available (case
     * sensitive). A special value of access_token will forward the entire encoded token.
     *
     * @param forwardAuthInfo
     *            The forwardAuthInfo
     */
    @JsonProperty("forwardAuthInfo")
    public void setForwardAuthInfo(List<ForwardAuthInfo> forwardAuthInfo) {
        this.forwardAuthInfo = forwardAuthInfo;
    }

    public JWTPolicyBean withForwardAuthInfo(List<ForwardAuthInfo> forwardAuthInfo) {
        this.forwardAuthInfo = forwardAuthInfo;
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

    public JWTPolicyBean withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @JsonProperty("allowedClockSkew")
    public long getAllowedClockSkew() {
        return allowedClockSkew;
    }

    @JsonProperty("allowedClockSkew")
    public JWTPolicyBean setAllowedClockSkew(long allowedClockSkew) {
        this.allowedClockSkew = allowedClockSkew;
        return this;
    }

}