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

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.validator.routines.UrlValidator;
import org.keycloak.common.util.PemUtils;

/**
 * JWT Authentication Policy Configuration
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "requireJWT", "requireSigned", "requireTransportSecurity", "stripTokens", "signingKeyString", "kid", "allowedClockSkew",
        "requiredClaims", "forwardAuthInfo" })
public class JWTPolicyBean {

    /**
     * Require JWT
     * <p>
     * Terminate request if no JWT provided.
     */
    @JsonProperty("requireJWT")
    private Boolean requireJWT = true;
    /**
     * Require Signed JWT (JWS).
     * <p>
     * Require JWTs be cryptographically signed and verified (JWS). It is strongly recommended
     * this be enabled.
     */
    @JsonProperty("requireSigned")
    private Boolean requireSigned = true;
    /**
     * Require Transport Security
     * <p>
     * Any request used without transport security will be rejected. JWT requires transport
     * security (e.g. TLS, SSL) to provide protection against a variety of attacks. It is
     * strongly advised this option be switched on.
     */
    @JsonProperty("requireTransportSecurity")
    private Boolean requireTransportSecurity = true;
    /**
     * Strip Tokens
     * <p>
     * Remove any Authorization header or token query parameter before forwarding traffic to
     * the API.
     */
    @JsonProperty("stripTokens")
    private Boolean stripTokens = false;
    /**
     * Signing Key
     * <p>
     * To validate JWT. Must be Base-64 encoded.
     */
    @JsonProperty("signingKeyString")
    private String signingKeyString;
    private Key signingKey;
    /**
     * Key ID (kid) of JWK(S)
     * <p>
     * If you provided a JWK(S) URL above you can specify here the kid of the JWK(S)
     *
     */
    @JsonProperty("kid")
    @JsonPropertyDescription("If you provided a JWK(S) URL above you can specify here the kid of the JWK(S)")
    private String kid;
    /**
     * Maximum Clock Skew
     * <p>
     * Maximum allowed clock skew in seconds when validating exp (expiry) and nbf (not before)
     * claims. Zero implies default behaviour.
     */
    @JsonProperty("allowedClockSkew")
    private Integer allowedClockSkew = 0;
    /**
     * Required Claims
     * <p>
     * Require claims
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims</a>, custom claims and
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#IDToken" target=
     * "_blank">ID token fields</a> (case sensitive).
     */
    @JsonProperty("requiredClaims")
    private List<RequiredClaim> requiredClaims = new ArrayList<>();
    /**
     * Forward Claim Information
     * <p>
     * Fields from the JWT can be set as headers and forwarded to the API. All
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims</a>, custom claims and
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#IDToken" target=
     * "_blank">ID token fields</a> are available (case sensitive). A special value of
     * <strong><tt>access_token</tt></strong> will forward the entire encoded token. Nested
     * claims can be accessed by using javascript dot syntax (e.g: <tt>address.country</tt>,
     * <tt>address.formatted</tt>).
     */
    @JsonProperty("forwardAuthInfo")
    private List<ForwardAuthInfo> forwardAuthInfo = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

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
     * Require Signed JWT (JWS).
     * <p>
     * Require JWTs be cryptographically signed and verified (JWS). It is strongly recommended
     * this be enabled.
     *
     * @return The requireSigned
     */
    @JsonProperty("requireSigned")
    public Boolean getRequireSigned() {
        return requireSigned;
    }

    /**
     * Require Signed JWT (JWS).
     * <p>
     * Require JWTs be cryptographically signed and verified (JWS). It is strongly recommended
     * this be enabled.
     *
     * @param requireSigned
     *            The requireSigned
     */
    @JsonProperty("requireSigned")
    public void setRequireSigned(Boolean requireSigned) {
        this.requireSigned = requireSigned;
    }

    public JWTPolicyBean withRequireSigned(Boolean requireSigned) {
        this.requireSigned = requireSigned;
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
     * Signing Key or URL to a JWK(S)
     * <p>
     * To validate JWT. Must be Base-64 encoded or you specify a URL to a JWK(S)
     *
     */
    @JsonProperty("signingKeyString")
    public String getSigningKeyString() {
        return signingKeyString;
    }

    /**
     * Signing Key
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
            String[] schemes = {"http","https"};
            UrlValidator urlValidator = new UrlValidator(schemes);
            // If it is a jwk(s) url we don't set the signingKey
            if (!urlValidator.isValid(signingKeyString)){
                signingKey = PemUtils.decodePublicKey(signingKeyString);
            }
        }
        this.signingKeyString = signingKeyString;
    }

    public JWTPolicyBean withSigningKeyString(String signingKeyString) {
        this.signingKeyString = signingKeyString;
        return this;
    }

    /**
     * Key ID (kid) of JWK(S)
     * <p>
     * If you provided a JWK(S) URL above you can specify here the kid of the JWK(S)
     *
     */
    @JsonProperty("kid")
    public String getKid() { return kid; }

    /**
     * Key ID (kid) of JWK(S)
     * <p>
     * If you provided a JWK(S) URL above you can specify here the kid of the JWK(S)
     *
     */
    @JsonProperty("kid")
    public void setKid(String kid){
        this.kid = kid.equalsIgnoreCase("null") || kid.isEmpty() ? null : kid;
    }

    @JsonProperty("kid")
    public JWTPolicyBean withKid (String kid){
        this.kid = kid;
        return this;
    }

    /**
     * Maximum Clock Skew
     * <p>
     * Maximum allowed clock skew in seconds when validating exp (expiry) and nbf (not before)
     * claims. Zero implies default behaviour.
     *
     * @return The allowedClockSkew
     */
    @JsonProperty("allowedClockSkew")
    public Integer getAllowedClockSkew() {
        return allowedClockSkew;
    }

    /**
     * Maximum Clock Skew
     * <p>
     * Maximum allowed clock skew in seconds when validating exp (expiry) and nbf (not before)
     * claims. Zero implies default behaviour.
     *
     * @param allowedClockSkew
     *            The allowedClockSkew
     */
    @JsonProperty("allowedClockSkew")
    public void setAllowedClockSkew(Integer allowedClockSkew) {
        this.allowedClockSkew = allowedClockSkew;
    }

    public JWTPolicyBean withAllowedClockSkew(Integer allowedClockSkew) {
        this.allowedClockSkew = allowedClockSkew;
        return this;
    }

    /**
     * Required Claims
     * <p>
     * Require claims
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims</a>, custom claims and
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#IDToken" target=
     * "_blank">ID token fields</a> (case sensitive).
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
     * "_blank">standard claims</a>, custom claims and
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#IDToken" target=
     * "_blank">ID token fields</a> (case sensitive).
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
     * Forward Claim Information
     * <p>
     * Fields from the JWT can be set as headers and forwarded to the API. All
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims</a>, custom claims and
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#IDToken" target=
     * "_blank">ID token fields</a> are available (case sensitive). A special value of
     * <strong><tt>access_token</tt></strong> will forward the entire encoded token. Nested
     * claims can be accessed by using javascript dot syntax (e.g: <tt>address.country</tt>,
     * <tt>address.formatted</tt>).
     *
     * @return The forwardAuthInfo
     */
    @JsonProperty("forwardAuthInfo")
    public List<ForwardAuthInfo> getForwardAuthInfo() {
        return forwardAuthInfo;
    }

    /**
     * Forward Claim Information
     * <p>
     * Fields from the JWT can be set as headers and forwarded to the API. All
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#StandardClaims" target=
     * "_blank">standard claims</a>, custom claims and
     * <a href="https://openid.net/specs/openid-connect-basic-1_0.html#IDToken" target=
     * "_blank">ID token fields</a> are available (case sensitive). A special value of
     * <strong><tt>access_token</tt></strong> will forward the entire encoded token. Nested
     * claims can be accessed by using javascript dot syntax (e.g: <tt>address.country</tt>,
     * <tt>address.formatted</tt>).
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

    public Key getSigningKey() { return signingKey; }

    public void setSigningKey(Key signingKey){
        this.signingKey = signingKey;
    }
}
