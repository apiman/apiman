/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.BasicAuthenticationConfig;
import io.apiman.gateway.engine.policies.config.basicauth.PasswordHashAlgorithmType;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls", "javadoc" })
public class BasicAuthenticationPolicyTest {


    /**
     * Test method for {@link io.apiman.gateway.engine.policies.BasicAuthenticationPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testParseConfiguration() {
        BasicAuthenticationPolicy policy = new BasicAuthenticationPolicy();

        // Basic properties
        String config =
                "{\r\n" +
                "    \"realm\" : \"TestRealm\",\r\n" +
                "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
                "    \"requireTransportSecurity\" : true,\r\n" +
                "    \"requireBasicAuth\" : true\r\n" +
                "}";
        Object parsed = policy.parseConfiguration(config);
        Assert.assertNotNull(parsed);
        Assert.assertEquals(BasicAuthenticationConfig.class, parsed.getClass());
        BasicAuthenticationConfig parsedConfig = (BasicAuthenticationConfig) parsed;
        Assert.assertEquals("TestRealm", parsedConfig.getRealm());
        Assert.assertEquals("X-Authenticated-Identity", parsedConfig.getForwardIdentityHttpHeader());
        Assert.assertEquals(Boolean.TRUE, parsedConfig.isRequireTransportSecurity());
        Assert.assertEquals(Boolean.TRUE, parsedConfig.getRequireBasicAuth());

        // Static identities
        config =
                "{\r\n" +
                "    \"realm\" : \"TestRealm\",\r\n" +
                "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
                "    \"staticIdentity\" : {\r\n" +
                "      \"identities\" : [\r\n" +
                "        { \"username\" : \"ckent\", \"password\" : \"ckent123!\" },\r\n" +
                "        { \"username\" : \"bwayne\", \"password\" : \"bwayne123!\" },\r\n" +
                "        { \"username\" : \"dprince\", \"password\" : \"dprince123!\" }\r\n" +
                "      ]\r\n" +
                "    }\r\n" +
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (BasicAuthenticationConfig) parsed;
        Assert.assertNotNull(parsedConfig.getStaticIdentity());
        Assert.assertEquals(3, parsedConfig.getStaticIdentity().getIdentities().size());
        Assert.assertEquals("bwayne", parsedConfig.getStaticIdentity().getIdentities().get(1).getUsername());
        Assert.assertEquals("bwayne123!", parsedConfig.getStaticIdentity().getIdentities().get(1).getPassword());

        // Multiple IP addresses
        config =
                "{\r\n" +
                "    \"realm\" : \"TestRealm\",\r\n" +
                "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
                "    \"ldapIdentity\" : {\r\n" +
                "        \"url\" : \"ldap://example.org:389\",\r\n" +
                "        \"dnPattern\" : \"cn=${username},dc=overlord,dc=org\"\r\n" +
                "    }\r\n" +
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (BasicAuthenticationConfig) parsed;
        Assert.assertNotNull(parsedConfig.getLdapIdentity());
        Assert.assertEquals("ldap://example.org:389", parsedConfig.getLdapIdentity().getUrl());
        Assert.assertEquals("cn=${username},dc=overlord,dc=org", parsedConfig.getLdapIdentity().getDnPattern());


        // Multiple IP addresses
        config =
                "{\r\n" +
                "    \"realm\" : \"TestRealm\",\r\n" +
                "    \"jdbcIdentity\" : {\r\n" +
                "        \"datasourcePath\" : \"jdbc/TestAuthDS\",\r\n" +
                "        \"query\" : \"SELECT * FROM users WHERE username = ? AND password = ?\",\r\n" +
                "        \"hashAlgorithm\" : \"SHA1\"\r\n" +
                "    }\r\n" +
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (BasicAuthenticationConfig) parsed;
        Assert.assertNotNull(parsedConfig.getJdbcIdentity());
        Assert.assertEquals("jdbc/TestAuthDS", parsedConfig.getJdbcIdentity().getDatasourcePath());
        Assert.assertEquals("SELECT * FROM users WHERE username = ? AND password = ?", parsedConfig.getJdbcIdentity().getQuery());
        Assert.assertEquals(PasswordHashAlgorithmType.SHA1, parsedConfig.getJdbcIdentity().getHashAlgorithm());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.BasicAuthenticationPolicy#apply(ServiceRequest, IPolicyContext, Object, IPolicyChain)}.
     */
    @Test
    public void testApplyStatic() {
        BasicAuthenticationPolicy policy = new BasicAuthenticationPolicy();
        String json = "{\r\n" +
                "    \"realm\" : \"TestRealm\",\r\n" +
                "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
                "    \"staticIdentity\" : {\r\n" +
                "      \"identities\" : [\r\n" +
                "        { \"username\" : \"ckent\", \"password\" : \"ckent123!\" },\r\n" +
                "        { \"username\" : \"bwayne\", \"password\" : \"bwayne123!\" },\r\n" +
                "        { \"username\" : \"dprince\", \"password\" : \"dprince123!\" }\r\n" +
                "      ]\r\n" +
                "    }\r\n" +
                "}";
        Object config = policy.parseConfiguration(json);
        ServiceRequest request = new ServiceRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(new IPolicyFailureFactoryComponent() {
            @Override
            public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                return failure;
            }
        });
        IPolicyChain<ServiceRequest> chain = Mockito.mock(IPolicyChain.class);

        // Failure
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);

        // Failure
        request.getHeaders().put("Authorization", createBasicAuthorization("ckent", "invalid_password"));
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);

        // Success
        request.getHeaders().put("Authorization", createBasicAuthorization("ckent", "ckent123!"));
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);
        Assert.assertEquals("ckent", request.getHeaders().get("X-Authenticated-Identity"));
    }

    /**
     * Creates the http Authorization string for the given credentials.
     * @param username
     * @param password
     */
    private String createBasicAuthorization(String username, String password) {
        String creds = username + ":" + password;
        StringBuilder builder = new StringBuilder();
        builder.append("Basic ");
        builder.append(Base64.encodeBase64String(creds.getBytes()));
        return builder.toString();
    }
}
