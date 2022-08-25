/*
 * Copyright 2022, Black Parrot Labs Ltd
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

package io.apiman.manager.ui.server.kc;

import java.util.Map;
import java.util.concurrent.Callable;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.authentication.ClientCredentialsProvider;
import org.keycloak.adapters.authorization.PolicyEnforcer;
import org.keycloak.adapters.rotation.PublicKeyLocator;
import org.keycloak.common.enums.RelativeUrlsUsed;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.enums.TokenStore;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.representations.adapters.config.AdapterConfig;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class DeploymentWithLogoutViaBackend extends KeycloakDeployment {
    private final KeycloakDeployment delegate;

    public DeploymentWithLogoutViaBackend(KeycloakDeployment delegate) {
        this.delegate = delegate;
        this.relativeUrls = delegate.getRelativeUrls();
    }


    @Override
    public KeycloakUriBuilder getLogoutUrl() {
        delegate.getLogoutUrl();
        // Thanks to @kernalex-exx https://github.com/keycloak/keycloak/discussions/10657#discussioncomment-2437915
        return KeycloakUriBuilder.fromUri(KeycloakUriBuilder.fromUri(delegate.getAuthServerBaseUrl()).clone()
                                                  .path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH).build(getRealm()).toString());
    }

    // All the rest are just delegates

    @Override
    public boolean isConfigured() {
        return delegate.isConfigured();
    }

    @Override
    public String getResourceName() {
        return delegate.getResourceName();
    }

    @Override
    public String getRealm() {
        return delegate.getRealm();
    }

    @Override
    public void setRealm(String realm) {
        delegate.setRealm(realm);
    }

    @Override
    public PublicKeyLocator getPublicKeyLocator() {
        return delegate.getPublicKeyLocator();
    }

    @Override
    public void setPublicKeyLocator(PublicKeyLocator publicKeyLocator) {
        delegate.setPublicKeyLocator(publicKeyLocator);
    }

    @Override
    public String getAuthServerBaseUrl() {
        return delegate.getAuthServerBaseUrl();
    }

    @Override
    public void setAuthServerBaseUrl(AdapterConfig config) {
        delegate.setAuthServerBaseUrl(config);
    }

    @Override
    public void resolveUrls() {
        delegate.getRealmInfoUrl();
    } // getRealmInfo triggers resolution of data; it's a protected URL so we can't use it.

    @Override
    public void resolveUrls(KeycloakUriBuilder authUrlBuilder) {
    }

    @Override
    protected OIDCConfigurationRepresentation getOidcConfiguration(String discoveryUrl) throws Exception {
        return null;
    }

    @Override
    public RelativeUrlsUsed getRelativeUrls() {
        return delegate.getRelativeUrls();
    }

    @Override
    public String getRealmInfoUrl() {
        return delegate.getRealmInfoUrl();
    }

    @Override
    public KeycloakUriBuilder getAuthUrl() {
        return delegate.getAuthUrl();
    }

    @Override
    public String getTokenUrl() {
        return delegate.getTokenUrl();
    }

    @Override
    public String getAccountUrl() {
        return delegate.getAccountUrl();
    }

    @Override
    public String getRegisterNodeUrl() {
        return delegate.getRegisterNodeUrl();
    }

    @Override
    public String getUnregisterNodeUrl() {
        return delegate.getUnregisterNodeUrl();
    }

    @Override
    public String getJwksUrl() {
        return delegate.getJwksUrl();
    }

    @Override
    public void setResourceName(String resourceName) {
        delegate.setResourceName(resourceName);
    }

    @Override
    public boolean isBearerOnly() {
        return delegate.isBearerOnly();
    }

    @Override
    public void setBearerOnly(boolean bearerOnly) {
        delegate.setBearerOnly(bearerOnly);
    }

    @Override
    public boolean isAutodetectBearerOnly() {
        return delegate.isAutodetectBearerOnly();
    }

    @Override
    public void setAutodetectBearerOnly(boolean autodetectBearerOnly) {
        delegate.setAutodetectBearerOnly(autodetectBearerOnly);
    }

    @Override
    public boolean isEnableBasicAuth() {
        return delegate.isEnableBasicAuth();
    }

    @Override
    public void setEnableBasicAuth(boolean enableBasicAuth) {
        delegate.setEnableBasicAuth(enableBasicAuth);
    }

    @Override
    public boolean isPublicClient() {
        return delegate.isPublicClient();
    }

    @Override
    public void setPublicClient(boolean publicClient) {
        delegate.setPublicClient(publicClient);
    }

    @Override
    public Map<String, Object> getResourceCredentials() {
        return delegate.getResourceCredentials();
    }

    @Override
    public void setResourceCredentials(Map<String, Object> resourceCredentials) {
        delegate.setResourceCredentials(resourceCredentials);
    }

    @Override
    public ClientCredentialsProvider getClientAuthenticator() {
        return delegate.getClientAuthenticator();
    }

    @Override
    public void setClientAuthenticator(ClientCredentialsProvider clientAuthenticator) {
        delegate.setClientAuthenticator(clientAuthenticator);
    }

    @Override
    public org.apache.http.client.HttpClient getClient() {
        return delegate.getClient();
    }

    public void setClient(org.apache.http.client.HttpClient client) {
        delegate.setClient(client);
    }

    @Override
    public String getScope() {
        return delegate.getScope();
    }

    @Override
    public void setScope(String scope) {
        delegate.setScope(scope);
    }

    @Override
    public SslRequired getSslRequired() {
        return delegate.getSslRequired();
    }

    @Override
    public void setSslRequired(SslRequired sslRequired) {
        delegate.setSslRequired(sslRequired);
    }

    @Override
    public boolean isSSLEnabled() {
        return delegate.isSSLEnabled();
    }

    @Override
    public int getConfidentialPort() {
        return delegate.getConfidentialPort();
    }

    @Override
    public void setConfidentialPort(int confidentialPort) {
        delegate.setConfidentialPort(confidentialPort);
    }

    @Override
    public TokenStore getTokenStore() {
        return delegate.getTokenStore();
    }

    @Override
    public void setTokenStore(TokenStore tokenStore) {
        delegate.setTokenStore(tokenStore);
    }

    @Override
    public String getAdapterStateCookiePath() {
        return delegate.getAdapterStateCookiePath();
    }

    @Override
    public void setAdapterStateCookiePath(String adapterStateCookiePath) {
        delegate.setAdapterStateCookiePath(adapterStateCookiePath);
    }

    @Override
    public String getStateCookieName() {
        return delegate.getStateCookieName();
    }

    @Override
    public void setStateCookieName(String stateCookieName) {
        delegate.setStateCookieName(stateCookieName);
    }

    @Override
    public boolean isUseResourceRoleMappings() {
        return delegate.isUseResourceRoleMappings();
    }

    @Override
    public void setUseResourceRoleMappings(boolean useResourceRoleMappings) {
        delegate.setUseResourceRoleMappings(useResourceRoleMappings);
    }

    @Override
    public boolean isCors() {
        return delegate.isCors();
    }

    @Override
    public void setCors(boolean cors) {
        delegate.setCors(cors);
    }

    @Override
    public int getCorsMaxAge() {
        return delegate.getCorsMaxAge();
    }

    @Override
    public void setCorsMaxAge(int corsMaxAge) {
        delegate.setCorsMaxAge(corsMaxAge);
    }

    @Override
    public String getCorsAllowedHeaders() {
        return delegate.getCorsAllowedHeaders();
    }

    @Override
    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
        delegate.setCorsAllowedHeaders(corsAllowedHeaders);
    }

    @Override
    public String getCorsAllowedMethods() {
        return delegate.getCorsAllowedMethods();
    }

    @Override
    public void setCorsAllowedMethods(String corsAllowedMethods) {
        delegate.setCorsAllowedMethods(corsAllowedMethods);
    }

    @Override
    public String getCorsExposedHeaders() {
        return delegate.getCorsExposedHeaders();
    }

    @Override
    public void setCorsExposedHeaders(String corsExposedHeaders) {
        delegate.setCorsExposedHeaders(corsExposedHeaders);
    }

    @Override
    public boolean isExposeToken() {
        return delegate.isExposeToken();
    }

    @Override
    public void setExposeToken(boolean exposeToken) {
        delegate.setExposeToken(exposeToken);
    }

    @Override
    public int getNotBefore() {
        return delegate.getNotBefore();
    }

    @Override
    public void setNotBefore(int notBefore) {
        delegate.setNotBefore(notBefore);
    }

    @Override
    public void updateNotBefore(int notBefore) {
        delegate.updateNotBefore(notBefore);
    }

    @Override
    public boolean isAlwaysRefreshToken() {
        return delegate.isAlwaysRefreshToken();
    }

    @Override
    public void setAlwaysRefreshToken(boolean alwaysRefreshToken) {
        delegate.setAlwaysRefreshToken(alwaysRefreshToken);
    }

    @Override
    public boolean isRegisterNodeAtStartup() {
        return delegate.isRegisterNodeAtStartup();
    }

    @Override
    public void setRegisterNodeAtStartup(boolean registerNodeAtStartup) {
        delegate.setRegisterNodeAtStartup(registerNodeAtStartup);
    }

    @Override
    public int getRegisterNodePeriod() {
        return delegate.getRegisterNodePeriod();
    }

    @Override
    public void setRegisterNodePeriod(int registerNodePeriod) {
        delegate.setRegisterNodePeriod(registerNodePeriod);
    }

    @Override
    public String getPrincipalAttribute() {
        return delegate.getPrincipalAttribute();
    }

    @Override
    public void setPrincipalAttribute(String principalAttribute) {
        delegate.setPrincipalAttribute(principalAttribute);
    }

    @Override
    public boolean isTurnOffChangeSessionIdOnLogin() {
        return delegate.isTurnOffChangeSessionIdOnLogin();
    }

    @Override
    public void setTurnOffChangeSessionIdOnLogin(boolean turnOffChangeSessionIdOnLogin) {
        delegate.setTurnOffChangeSessionIdOnLogin(turnOffChangeSessionIdOnLogin);
    }

    @Override
    public int getTokenMinimumTimeToLive() {
        return delegate.getTokenMinimumTimeToLive();
    }

    @Override
    public void setTokenMinimumTimeToLive(int tokenMinimumTimeToLive) {
        delegate.setTokenMinimumTimeToLive(tokenMinimumTimeToLive);
    }

    @Override
    public int getMinTimeBetweenJwksRequests() {
        return delegate.getMinTimeBetweenJwksRequests();
    }

    @Override
    public void setMinTimeBetweenJwksRequests(int minTimeBetweenJwksRequests) {
        delegate.setMinTimeBetweenJwksRequests(minTimeBetweenJwksRequests);
    }

    @Override
    public int getPublicKeyCacheTtl() {
        return delegate.getPublicKeyCacheTtl();
    }

    @Override
    public void setPublicKeyCacheTtl(int publicKeyCacheTtl) {
        delegate.setPublicKeyCacheTtl(publicKeyCacheTtl);
    }

    @Override
    public void setPolicyEnforcer(Callable<PolicyEnforcer> policyEnforcer) {
        delegate.setPolicyEnforcer(policyEnforcer);
    }

    @Override
    public PolicyEnforcer getPolicyEnforcer() {
        return delegate.getPolicyEnforcer();
    }

    @Override
    public boolean isPkce() {
        return delegate.isPkce();
    }

    @Override
    public void setPkce(boolean pkce) {
        delegate.setPkce(pkce);
    }

    @Override
    public void setIgnoreOAuthQueryParameter(boolean ignoreOAuthQueryParameter) {
        delegate.setIgnoreOAuthQueryParameter(ignoreOAuthQueryParameter);
    }

    @Override
    public boolean isOAuthQueryParameterEnabled() {
        return delegate.isOAuthQueryParameterEnabled();
    }

    @Override
    public Map<String, String> getRedirectRewriteRules() {
        return delegate.getRedirectRewriteRules();
    }

    @Override
    public void setRewriteRedirectRules(Map<String, String> redirectRewriteRules) {
        delegate.setRewriteRedirectRules(redirectRewriteRules);
    }

    @Override
    public boolean isDelegateBearerErrorResponseSending() {
        return delegate.isDelegateBearerErrorResponseSending();
    }

    @Override
    public void setDelegateBearerErrorResponseSending(boolean delegateBearerErrorResponseSending) {
        delegate.setDelegateBearerErrorResponseSending(delegateBearerErrorResponseSending);
    }

    @Override
    public boolean isVerifyTokenAudience() {
        return delegate.isVerifyTokenAudience();
    }

    @Override
    public void setVerifyTokenAudience(boolean verifyTokenAudience) {
        delegate.setVerifyTokenAudience(verifyTokenAudience);
    }

    public void setClient(Callable<org.apache.http.client.HttpClient> callable) {
        delegate.setClient(callable);
    }
}
