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
 * distributed under the License is distributed on an "AS I/me S" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.plugins.keycloak_oauth_policy.beans;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.keycloak.util.PemUtils;

/**
 * Keycloak OAuth Policy Configuration
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "requireOauth", "requireTransportSecurity", "blacklistUnsafeTokens", "stripTokens",
        "realm", "realmCertificateString", "forwardRoles", "delegateKerberosTicket", "forwardAuthInfo" })
public class KeycloakOauthConfigBean {

    /**
     * Require OAuth
     * <p>
     * Terminate request if no OAuth is provided.
     */
    @JsonProperty("requireOauth")
    private Boolean requireOauth = true;

    /**
     * Require Transport Security
     * <p>
     * OAuth2 requires transport security such as TLS or SSL in order to be secure. Terminate request if none
     * provided.
     */
    @JsonProperty("requireTransportSecurity")
    private boolean requireTransportSecurity = true;

    /**
     * Blacklist unsafe tokens
     * <p>
     * Any tokens used without transport security will be blackedlisted to mitigate associated security risks.
     */
    @JsonProperty("blacklistUnsafeTokens")
    private Boolean blacklistUnsafeTokens = false;
    /**
     * Strip tokens
     * <p>
     * Remove any Authorization header or token query parameter before forwarding traffic to the Api.
     *
     */
    @JsonProperty("stripTokens")
    private Boolean stripTokens = false;
    /**
     * Realm
     * <p>
     * Realm name
     */
    @JsonProperty("realm")
    private String realm;
    /**
     * Keycloak realm certificate
     * <p>
     * To validate OAuth requests. Must be a PEM-encoded X.509 certificate, including bounding strings.
     */
    @JsonProperty("realmCertificateString")
    private String realmCertificateString;

    /**
     * Forward Authorization Roles
     * <p>
     * Forward KeyCloak roles to the Authorization policy. In your Authorization policy you should specify
     * your required role(s).
     *
     */
    @JsonProperty("forwardRoles")
    private ForwardRoles forwardRoles;

    /**
     * Delegate Kerberos Ticket
     */
    @JsonProperty("delegateKerberosTicket")
    private boolean delegateKerberosTicket = false;

    /**
     * Forward Keycloak token information
     * <p>
     * Fields from the token can be set as headers and forwarded to the Api. Access_token corresponds to
     * the full token.
     */
    @JsonProperty("forwardAuthInfo")
    private List<ForwardAuthInfo> forwardAuthInfo = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private Certificate realmCertificate;

    /**
     * Require OAuth
     * <p>
     * Terminate request if no OAuth is provided.
     *
     * @return The requireOauth
     */
    @JsonProperty("requireOauth")
    public Boolean getRequireOauth() {
        return requireOauth;
    }

    /**
     * Require OAuth
     * <p>
     * Any request used without transport security will be rejected. OAuth2 requires transport security (e.g.
     * TLS, SSL) to provide protection against replay attacks. It is strongly advised for this option to be
     * switched on.
     *
     * @param requireOauth The requireOauth
     */
    @JsonProperty("requireOauth")
    public void setRequireOauth(Boolean requireOauth) {
        this.requireOauth = requireOauth;
    }

    /**
     * Require Transport Security
     * <p>
     * Any request used without transport security will be rejected. OAuth2 requires transport security (e.g.
     * TLS, SSL) to provide protection against replay attacks. It is strongly advised for this option to be
     * switched on.
     *
     * @return whether transport security is required
     */
    @JsonProperty("requireTransportSecurity")
    public boolean getRequireTransportSecurity() {
        return requireTransportSecurity;
    }

    /**
     * @param requireTransportSecurity status
     */
    @JsonProperty("requireTransportSecurity")
    public void setRequireTransportSecurity(boolean requireTransportSecurity) {
        this.requireTransportSecurity = requireTransportSecurity;
    }

    /**
     * Blacklist unsafe tokens
     * <p>
     * Any tokens used without transport security will be blackedlisted to mitigate associated security risks.
     *
     * @return The blacklistUnsafeTokens
     */
    @JsonProperty("blacklistUnsafeTokens")
    public Boolean getBlacklistUnsafeTokens() {
        return blacklistUnsafeTokens;
    }

    /**
     * Blacklist unsafe tokens
     * <p>
     * Any tokens used without transport security will be blackedlisted to mitigate associated security risks.
     *
     * @param blacklistUnsafeTokens The blacklistUnsafeTokens
     */
    @JsonProperty("blacklistUnsafeTokens")
    public void setBlacklistUnsafeTokens(Boolean blacklistUnsafeTokens) {
        this.blacklistUnsafeTokens = blacklistUnsafeTokens;
    }

    /**
     * Strip tokens
     * <p>
     * Remove any Authorization header or token query parameter before forwarding traffic to the Api.
     *
     * @return The stripTokens
     */
    @JsonProperty("stripTokens")
    public Boolean getStripTokens() {
        return stripTokens;
    }

    /**
     * Strip tokens
     * <p>
     * Remove any Authorization header or token query parameter before forwarding traffic to the Api.
     *
     * @param stripTokens The stripTokens
     */
    @JsonProperty("stripTokens")
    public void setStripTokens(Boolean stripTokens) {
        this.stripTokens = stripTokens;
    }

    /**
     * Realm
     * <p>
     * Realm name
     *
     * @return The realm
     */
    @JsonProperty("realm")
    public String getRealm() {
        return realm;
    }

    /**
     * Realm
     * <p>
     * Realm name
     *
     * @param realm The realm
     */
    @JsonProperty("realm")
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Keycloak realm certificate
     * <p>
     * To validate OAuth requests. Must be a PEM-encoded X.509 certificate, including bounding strings.
     *
     * @return The realmCertificateString
     */
    @JsonProperty("realmCertificateString")
    public String getRealmCertificateString() {
        return realmCertificateString;
    }

    /**
     * Keycloak realm certificate
     * <p>
     * To validate OAuth requests. Must be a PEM-encoded X.509 certificate, including bounding strings.
     *
     * @param realmCertificateString The realmCertificateString
     */
    @JsonProperty("realmCertificateString")
    public void setRealmCertificateString(String realmCertificateString) {
        this.realmCertificateString = realmCertificateString;
        realmCertificate = generateCertificate(realmCertificateString);
    }

    /**
     * Forward Authorization Roles
     * <p>
     * Forward KeyCloak roles to the Authorization policy. In your Authorization policy you should specify
     * your required role(s).
     *
     * @return The forwardRoles
     */
    @JsonProperty("forwardRoles")
    public ForwardRoles getForwardRoles() {
        return forwardRoles;
    }

    /**
     * Forward Authorization Roles
     * <p>
     * Forward KeyCloak roles to the Authorization policy. In your Authorization policy you should specify
     * your required role(s).
     *
     * @param forwardRoles The forwardRoles
     */
    @JsonProperty("forwardRoles")
    public void setForwardRoles(ForwardRoles forwardRoles) {
        this.forwardRoles = forwardRoles;
    }

    /**
     * Delegate Kerberos Ticket
     * <p>
     * Delegate the embedded Kerberos Ticket to the Api (via the Authorization header).
     *
     * @return true if should delegate token; else false
     */
    @JsonProperty("delegateKerberosTicket")
    public boolean getDelegateKerberosTicket() {
        return delegateKerberosTicket;
    }

    /**
     * Delegate Kerberos Ticket
     * <p>
     * Delegate the embedded Kerberos Ticket to the Api (via the Authorization header).
     *
     * @param delegateKerberosTicket true if should delegate token; else false
     */
    @JsonProperty("delegateKerberosTicket")
    public void setDelegateKerberosTicket(boolean delegateKerberosTicket) {
        this.delegateKerberosTicket = delegateKerberosTicket;
    }

    /**
     * Forward Keycloak token information
     * <p>
     * Fields from the token can be set as headers and forwarded to the Api. Access_token corresponds to
     * the full token.
     *
     * @return The forwardAuthInfo
     */
    @JsonProperty("forwardAuthInfo")
    public List<ForwardAuthInfo> getForwardAuthInfo() {
        return forwardAuthInfo;
    }

    /**
     * Forward Keycloak token information
     * <p>
     * Fields from the token can be set as headers and forwarded to the Api. Access_token corresponds to
     * the full token.
     *
     * @param forwardAuthInfo The forwardAuthInfo
     */
    @JsonProperty("forwardAuthInfo")
    public void setForwardAuthInfo(List<ForwardAuthInfo> forwardAuthInfo) {
        this.forwardAuthInfo = forwardAuthInfo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Certificate getRealmCertificate() {
        return realmCertificate;
    }

    private Certificate generateCertificate(String data) {
        try {
            return PemUtils.decodeCertificate(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
