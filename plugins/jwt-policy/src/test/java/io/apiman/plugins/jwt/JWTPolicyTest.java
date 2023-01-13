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

package io.apiman.plugins.jwt;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.test.common.mock.EchoResponse;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.PolicyFailureError;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.keycloak.common.util.PemUtils;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@TestingPolicy(JWTPolicy.class)
@SuppressWarnings("nls")
public class JWTPolicyTest extends ApimanPolicyTest {
    private static final String PUBLIC_KEY_PEM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmoV2gM0BGxgLQUpMkNdLKkXq46tcCBjoatHWqukrYj6VZ1t6OciWYKZRsmBVDsc34gFM6/fBqBn7zRwIK+OGXu1OLGoXEjR9I+awdxpQItjDq9lyFMDFPfXu6nCPSpZ+txNWl6V2cno6PpcEPpUYT6n6lUjcwpbTuGwq80P29Net212ksAwLJGvpIIUJ5yWuYJtirhoUeJEwKJAGbo5xrRrY9w1pkw+1kdPhUpP26pd80Mga2hcwJtykeIx5gLajRbhsXaijOv2FBtBSKgEH8tXISt16SBjaUbp642tLvqsT/VUPvvcgmcWWqhvm72ALaBwu3G/OHswRMCxxMohMyQIDAQAB";
    private static final String PRIVATE_KEY_PEM = "MIIEpQIBAAKCAQEAmoV2gM0BGxgLQUpMkNdLKkXq46tcCBjoatHWqukrYj6VZ1t6OciWYKZRsmBVDsc34gFM6/fBqBn7zRwIK+OGXu1OLGoXEjR9I+awdxpQItjDq9lyFMDFPfXu6nCPSpZ+txNWl6V2cno6PpcEPpUYT6n6lUjcwpbTuGwq80P29Net212ksAwLJGvpIIUJ5yWuYJtirhoUeJEwKJAGbo5xrRrY9w1pkw+1kdPhUpP26pd80Mga2hcwJtykeIx5gLajRbhsXaijOv2FBtBSKgEH8tXISt16SBjaUbp642tLvqsT/VUPvvcgmcWWqhvm72ALaBwu3G/OHswRMCxxMohMyQIDAQABAoIBAQCHcwZ10T5u6Zy0FtUXAiI5ZCCKgeOilXLmcBqkptAIxqNgfqedj1+CSUjD+/2Tfr5Vtp4fGob/PAelvDTNhBx9ibdE55phsvEfT1DQlpg4c5rSQUHnPzOnJLXRe+mfkFxzTthRBhHWN55mzypBUaCF9JJb2grp6ByfRPJBXApWhHrEALUwTd/9OiETsC4d7GbJ6ofk45tSl0HzNIeld9iEZk0WrgH95ucN75yCYv839096nB9nCH80yXV9JZIGj8bC6aPwbBnUnUdQqZxsDBlKNkT7U5AIdhqQdYjdXteTopuv12bflXtZGyTJoes1qLL8lpWgzkbjQg91+qmpCywhAoGBANv5opBc10C3y6ZJh9zepbM+wr0tbzUvTFAhj1Y1DoaHwxb9qV1mtWQZ5qEf1O+7RJYljv3hwzUc/gsZ13nBfySpVrdiVMTVIuLC8UPuH/sv0Z/uXcbwr3jxezrhJX5dJkhz1I8gPUKHLWIhMp/jZr26ieSPz9KwupTn+MPmPyYfAoGBALPTtHqZbB4dpxPmImv2l7PgR92CwVSd/yjrfOold4Oi1bODjhNSR6/h/YghWfRHAHIoRBWTlSfu/JsffJG/2bZa2xlcpqMb/fHzg05zBtmu8ozi3CAE7Twg5bE4GtuqV1hFXK4mPxzboSmj8H4puU85GNuTA/sRDd9saZSu6CAXAoGAJ30//rR7++VCzN5EYpUhn/TzVqyyWxTbmUL9DVfG/MWgcx8kaV0H0SmJKoGhY0v1+xJRAiimN4G15V5FPVlMLtOreo5Pc2pjsduXHj/ARAKImjJbaVxJ0+dd3OsQJQgp2DXbAbqi5K+JqSUWhnd3OTYkjQB4KXWKeTLPiLNrwLcCgYEAm7l8dCLCRv4kvo2vR1E/I+zYLxHZO96qpRPwk4+ohJ0RdKg6865wF/abKDTBglGuKC2IcCriordJl0fYBxtdfJYHYFokj+FgsxLOpbPkvcPLlYerWisKCeTvI93THGDRzMYcMU87nlDvqnCmhYq6R8nJJfSVIOku20k10ST6LTcCgYEAtrTamlY8tQ9Li+yi+yeGB2nxQCVkjQE0yl2GPxGrZaXlpH2mrhshtz0UUXcDmfpUOINCc3OzgWNCymNUesmVNuaobvgERXiDv51cSDgfYNT6NZz4+JPox2sGgeZIgkvQlFPr6+OxaMl8iQLHKwIqFjJAGCajKA4CodlIRaQClqM=";
    private static final String PUBLIC_KEY_PEM_2 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu+TCyq0U+N4IHVBbwuRoOGYF/exUcEnQGsbSXiSXIHFATQkGEhTRFbGFiq6PBRKnhSQKoM9IzKO7Fo4rpwSf6HdYsN5y9UkL/DQpiQxbXLjbAoAcjpvToSkCS2sfbzDubU1ZVJJN8g9Ag47tG1VjdpDRqH8GgeR1t/qBn62NbU1RCU8WfIqfNNL2JeVGvuixSnoFleIBYejCN7+i0/glUvf+6D/WdeJYuZ2ZbBMk8p+Xh2WE4dNv0ILV4yC27hFho+sbImn2YI7cIowXjRY3zDN69n+JzjAz0sdgMcaNi34E6U9wZAidgQF52XiqVeimjpPzcm31sm7n3BahodKC4wIDAQAB";
    private static final String PRIVATE_KEY_PEM_2 = "MIIEowIBAAKCAQEAu+TCyq0U+N4IHVBbwuRoOGYF/exUcEnQGsbSXiSXIHFATQkGEhTRFbGFiq6PBRKnhSQKoM9IzKO7Fo4rpwSf6HdYsN5y9UkL/DQpiQxbXLjbAoAcjpvToSkCS2sfbzDubU1ZVJJN8g9Ag47tG1VjdpDRqH8GgeR1t/qBn62NbU1RCU8WfIqfNNL2JeVGvuixSnoFleIBYejCN7+i0/glUvf+6D/WdeJYuZ2ZbBMk8p+Xh2WE4dNv0ILV4yC27hFho+sbImn2YI7cIowXjRY3zDN69n+JzjAz0sdgMcaNi34E6U9wZAidgQF52XiqVeimjpPzcm31sm7n3BahodKC4wIDAQABAoIBACTjtgba0opF9Wvj8hAijf+8sCJ5et6M72nCbV0EbBN3iLtXMTTtl1td/i0LNpM1ZWRzfg6yg8WBw+KaySFCfC7E98nJ8uILlGnQx/LbVTiwJneoNXMeTv+OMKAkCQjon3cgP5CmJN2Idw8dSZobOqr1peQiBGIOO7qCWV7DUUgLFr3DYWgrYdTOhHD7tFU9TVNd67OahJmPvq7KLVmbjjJJT+XcV52DV/bE74EfCCrm/XrVvfPxeuY0xps9g3C5uYmTJ947s1yYvDnedmgbKf8xpxZOOrU2SGMkrGY8M2pXEfIzKCEpf5lJd4jmfanx07hYnbqVCNK0Wi1kWxgZ+pECgYEA6QUN+xIPeMcv1ezxdIlbC9EV05y81qv67Qu8d9vOgBPq/dtij1s83vbaLjo2CL298Fff9+d+mc+EmYkdh3cnalFGkFaYEgRJK2ZX9M6HzVOvcxHACeMFStnEoHUX4oYS+I5LnbPTqNpSN353wTaQaTlET8NwjAx31Eq7Qh/EWLsCgYEAzmxtNnu3DnhLYVTnOIPKhDcDj0Y8vUgoH2mh9DNLiDOUpoAyJN2++P4YROTlV2DO8YAJKn/nT/jDP431SNjMWxg294TwhRLBTo6Wsb6VshvO2oMNlzU4XhxslYostxtpzxxXR5Hz4Y4r7iEGyeCVWK/FV3+VMKv4XzyhN787z/kCgYAkbEnXKOeKNXhgs/y5/o5gtnn27dUGqTM7wk3fXlhU5MgijhxF1DkbkPJhr2+qxh3eZ532nhTH7gwIA8q91f4vPC0Permid1EIm6K7/Vx019Pg5LFj2jyFiqyVeDgXjGWCvJtNN0KqbhXT3szRQron+G+ZGC+LdJd2c4f5ugVy6QKBgH1kyjcVkg7dLlVS4R6omGYWkTgAn7cP1Se34GTaoCB9zOgT6eIRPgg9OnrnXRXmPe/gKjFB/z7KS7kYwA7fe8w+em2DuSPtT7Yr0gjpUEAgulhs0d2vu60XsTJp/F3C7lScz7wvQiobVj5Sm7AYmECGjedoHjWx0a/wwbJ+nZPpAoGBALwMo52OY05k+LpSsYWQCLFNfuitizQ8STYOTBPA3nGA2C1ntYK29ixFt897bZvWiZvgP261W1yaRNkUIG6qS43mUOVL9uiJvVHMM4+mHuz6N64Q8hbFi7+HQiWvKyN/FRp3H0mbPR4VMdMqwqI0N02HuERPJfcYn4L1wg5Mizb8";
    private static final String AUTHORIZATION = "Authorization";

    @Rule
    // http://www.mock-server.com/mock_server/running_mock_server.html#junit_rule
    public MockServerRule mockServerRule = new MockServerRule(this, 1080);
    private MockServerClient mockServerClient = mockServerRule.getClient();

    @Before
    public void initialize(){
        RSAPublicKey rsa = getPublicRsaKey(PUBLIC_KEY_PEM);
        mockServerClient.when(request().withMethod("GET").withPath("/jwks.json"))
            .respond(response().withStatusCode(200)
            .withBody("{\"keys\": [\n" +
                    "    {\n" +
                    "      \"kid\": null,\n" +
                    "      \"e\": \""+ Base64.getUrlEncoder().encodeToString(rsa.getPublicExponent().toByteArray()) +"\",\n" +
                    "      \"n\": \""+ Base64.getUrlEncoder().encodeToString(rsa.getModulus().toByteArray()) +"\",\n" +
                    "      \"kty\":\"RSA\",\n" +
                    "      \"alg\": \"RS256\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}")
            );
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": true,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": false,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void signedValidToken() throws Throwable {
        String authVal = "Bearer " + signedToken(PRIVATE_KEY_PEM);
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                .header(AUTHORIZATION, authVal);
        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        // Ensure we didn't remove the header and it has remained unchanged
        Assert.assertEquals(authVal, echo.getHeaders().get(AUTHORIZATION));
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": true,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": false,\n" +
            "  \"signingKeyString\": \"http://127.0.0.1:1080/jwks.json\", \n" +
            "  \"kid\": \"null\", \n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void signedValidTokenWithJwksAndExpireCache() throws Throwable {
        // First request with first jwk(s)
        String authVal = "Bearer " + signedToken(PRIVATE_KEY_PEM);
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                .header(AUTHORIZATION, authVal);
        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        // Ensure we didn't remove the header and it has remained unchanged
        Assert.assertEquals(authVal, echo.getHeaders().get(AUTHORIZATION));

        // Second request will fail because jwk(s) has changed and we have to invalidate the cache
        authVal = "Bearer " + signedToken(PRIVATE_KEY_PEM_2);
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                .header(AUTHORIZATION, authVal);
        try {
            send(request);
            // we should never get a valid response
            Assert.fail("Valid response instead of exception");
        } catch (Exception e) {
            assert true;
        }

        // Update jwk(s) on mockserver
        mockServerClient.reset();
        RSAPublicKey rsa = getPublicRsaKey(PUBLIC_KEY_PEM_2);
        mockServerClient.when(request().withMethod("GET").withPath("/jwks.json"))
            .respond(response().withStatusCode(200)
            .withBody("{\"keys\": [\n" +
                    "    {\n" +
                    "      \"kid\": null,\n" +
                    "      \"e\": \""+ Base64.getUrlEncoder().encodeToString(rsa.getPublicExponent().toByteArray()) +"\",\n" +
                    "      \"n\": \""+ Base64.getUrlEncoder().encodeToString(rsa.getModulus().toByteArray()) +"\",\n" +
                    "      \"kty\":\"RSA\",\n" +
                    "      \"alg\": \"RS256\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}")
            );

        // Send request again and it will pass
        authVal = "Bearer " + signedToken(PRIVATE_KEY_PEM_2);
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                .header(AUTHORIZATION, authVal);
        response = send(request);
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        // Ensure we didn't remove the header and it has remained unchanged
        Assert.assertEquals(authVal, echo.getHeaders().get(AUTHORIZATION));
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": true,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void signedValidTokenStripAuth() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                .header(AUTHORIZATION, "Bearer " + signedToken(PRIVATE_KEY_PEM));

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Assert.assertNull(echo.getHeaders().get(AUTHORIZATION));
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void unsignedValidTokenHeader() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                .header(AUTHORIZATION, "Bearer " + unsignedToken());

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"aride\" }],\n" +
            "  \"forwardAuthInfo\": [{ \"header\": \"X-Foo\", \"field\": \"sub\" }]\n" +
            "}"
    )
    public void shouldForwardClaimsAsHeaders() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                .header(AUTHORIZATION, "Bearer " + Jwts.builder().setSubject("aride").compact());

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Assert.assertEquals("aride", echo.getHeaders().get("X-Foo"));
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }],\n" +
            "  \"forwardAuthInfo\": [{ \"header\": \"X-Foo\", \"field\": \"access_token\" }]\n" +
            "}"
    )
    public void shouldForwardAccessTokenAsHeader() throws Throwable {
        String token = unsignedToken();
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                .header(AUTHORIZATION, "Bearer " + token);

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Assert.assertEquals(token, echo.getHeaders().get("X-Foo"));
    }


    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void unsignedValidTokenQueryParam() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                  .query("access_token", unsignedToken());
        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void shouldFailWhenNoTokenProvided() throws Throwable {
        PolicyFailure failure = null;
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante");
        try {
            send(request);
        } catch (PolicyFailureError pfe) {
            failure = pfe.getFailure();
        }
        Assert.assertNotNull(failure);
        Assert.assertEquals(401, failure.getResponseCode());
        Assert.assertEquals(12005, failure.getFailureCode());
        Assert.assertEquals(PolicyFailureType.Authentication, failure.getType());
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void shouldFailWhenTokenInvalid() throws Throwable {
        PolicyFailure failure = null;
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                    .header("Authorization", "Bearer <Obviously invalid token>");
        try {
            send(request);
        } catch (PolicyFailureError pfe) {
            failure = pfe.getFailure();
        }
        Assert.assertNotNull(failure);
        Assert.assertEquals(401, failure.getResponseCode());
        Assert.assertEquals(12007, failure.getFailureCode());
        Assert.assertEquals(PolicyFailureType.Authentication, failure.getType());
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void shouldFailWhenTokenNotYetValid() throws Throwable {
        PolicyFailure failure = null;
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                    .header("Authorization", "Bearer " + unsignedNotYetValidToken());
        try {
            send(request);
        } catch (PolicyFailureError pfe) {
            failure = pfe.getFailure();
        }
        Assert.assertNotNull(failure);
        Assert.assertEquals(401, failure.getResponseCode());
        Assert.assertEquals(12010, failure.getFailureCode());
        Assert.assertEquals(PolicyFailureType.Authentication, failure.getType());
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void shouldFailWhenTokenExpired() throws Throwable {
        PolicyFailure failure = null;
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                    .header("Authorization", "Bearer " + unsignedExpiredToken());
        try {
            send(request);
        } catch (PolicyFailureError pfe) {
            failure = pfe.getFailure();
        }
        Assert.assertNotNull(failure);
        Assert.assertEquals(401, failure.getResponseCode());
        Assert.assertEquals(12006, failure.getFailureCode());
        Assert.assertEquals(PolicyFailureType.Authentication, failure.getType());
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"will_not_match\" }]\n" +
            "}"
    )
    public void shouldFailWithUnexpectedClaimValue() throws Throwable {
        PolicyFailure failure = null;
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                    .header("Authorization", "Bearer " + unsignedToken());
        try {
            send(request);
        } catch (PolicyFailureError pfe) {
            failure = pfe.getFailure();
        }
        Assert.assertNotNull(failure);
        Assert.assertEquals(401, failure.getResponseCode());
        Assert.assertEquals(12009, failure.getFailureCode());
        Assert.assertEquals(PolicyFailureType.Authentication, failure.getType());
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"will_not_match\" }]\n" +
            "}"
    )
    public void shouldFailWithMissingClaim() throws Throwable {
        PolicyFailure failure = null;
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                    .header("Authorization", "Bearer " + Jwts.builder().claim("x", "x").compact());
        try {
            send(request);
        } catch (PolicyFailureError pfe) {
            failure = pfe.getFailure();
        }
        Assert.assertNotNull(failure);
        Assert.assertEquals(401, failure.getResponseCode());
        Assert.assertEquals(12009, failure.getFailureCode());
        Assert.assertEquals(PolicyFailureType.Authentication, failure.getType());
    }

    @Test
    @Configuration("{\n" +
            "  \"requireJWT\": true,\n" +
            "  \"requireSigned\": false,\n" +
            "  \"requireTransportSecurity\": true,\n" +
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"will_not_match\" }]\n" +
            "}"
    )
    public void shouldFailWithNoTls() throws Throwable {
        PolicyFailure failure = null;
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                    .header("Authorization", "Bearer " + Jwts.builder().claim("x", "x").compact());
        try {
            send(request);
        } catch (PolicyFailureError pfe) {
            failure = pfe.getFailure();
        }
        Assert.assertNotNull(failure);
        Assert.assertEquals(401, failure.getResponseCode());
        Assert.assertEquals(12009, failure.getFailureCode());
        Assert.assertEquals(PolicyFailureType.Authentication, failure.getType());
    }

    private String signedToken(String privateKey) throws Exception {
        JwtBuilder jwts = Jwts.builder().setSubject("france frichot")
                .signWith(SignatureAlgorithm.RS256, PemUtils.decodePrivateKey(privateKey));
        return jwts.compact();
   }

    private String unsignedToken() throws Exception {
         JwtBuilder jwts = Jwts.builder().setSubject("france frichot");
         return jwts.compact();
    }

    private String unsignedNotYetValidToken() throws Exception {
        Instant instant = LocalDateTime.now().plusDays(5).toInstant(ZoneOffset.UTC);
        Date nbf = Date.from(instant);
        JwtBuilder jwts = Jwts.builder().setSubject("france frichot").setNotBefore(nbf);
        return jwts.compact();
    }

    private String unsignedExpiredToken() throws Exception {
        Instant instant = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC);
        Date exp = Date.from(instant);
        JwtBuilder jwts = Jwts.builder().setSubject("france frichot").setExpiration(exp);
        return jwts.compact();
    }

    private RSAPublicKey getPublicRsaKey(String publicKey){
        RSAPublicKey rsa = null;
        try {
            rsa = (RSAPublicKey) PemUtils.decodePublicKey(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rsa;
    }
}
