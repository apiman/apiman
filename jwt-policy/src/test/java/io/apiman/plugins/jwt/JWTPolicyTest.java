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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.keycloak.common.util.PemUtils;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@TestingPolicy(JWTPolicy.class)
@SuppressWarnings("nls")
public class JWTPolicyTest extends ApimanPolicyTest {
    private static final String PUBLIC_KEY_PEM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmoV2gM0BGxgLQUpMkNdLKkXq46tcCBjoatHWqukrYj6VZ1t6OciWYKZRsmBVDsc34gFM6/fBqBn7zRwIK+OGXu1OLGoXEjR9I+awdxpQItjDq9lyFMDFPfXu6nCPSpZ+txNWl6V2cno6PpcEPpUYT6n6lUjcwpbTuGwq80P29Net212ksAwLJGvpIIUJ5yWuYJtirhoUeJEwKJAGbo5xrRrY9w1pkw+1kdPhUpP26pd80Mga2hcwJtykeIx5gLajRbhsXaijOv2FBtBSKgEH8tXISt16SBjaUbp642tLvqsT/VUPvvcgmcWWqhvm72ALaBwu3G/OHswRMCxxMohMyQIDAQAB";
    private static final String PRIVATE_KEY_PEM = "MIIEpQIBAAKCAQEAmoV2gM0BGxgLQUpMkNdLKkXq46tcCBjoatHWqukrYj6VZ1t6OciWYKZRsmBVDsc34gFM6/fBqBn7zRwIK+OGXu1OLGoXEjR9I+awdxpQItjDq9lyFMDFPfXu6nCPSpZ+txNWl6V2cno6PpcEPpUYT6n6lUjcwpbTuGwq80P29Net212ksAwLJGvpIIUJ5yWuYJtirhoUeJEwKJAGbo5xrRrY9w1pkw+1kdPhUpP26pd80Mga2hcwJtykeIx5gLajRbhsXaijOv2FBtBSKgEH8tXISt16SBjaUbp642tLvqsT/VUPvvcgmcWWqhvm72ALaBwu3G/OHswRMCxxMohMyQIDAQABAoIBAQCHcwZ10T5u6Zy0FtUXAiI5ZCCKgeOilXLmcBqkptAIxqNgfqedj1+CSUjD+/2Tfr5Vtp4fGob/PAelvDTNhBx9ibdE55phsvEfT1DQlpg4c5rSQUHnPzOnJLXRe+mfkFxzTthRBhHWN55mzypBUaCF9JJb2grp6ByfRPJBXApWhHrEALUwTd/9OiETsC4d7GbJ6ofk45tSl0HzNIeld9iEZk0WrgH95ucN75yCYv839096nB9nCH80yXV9JZIGj8bC6aPwbBnUnUdQqZxsDBlKNkT7U5AIdhqQdYjdXteTopuv12bflXtZGyTJoes1qLL8lpWgzkbjQg91+qmpCywhAoGBANv5opBc10C3y6ZJh9zepbM+wr0tbzUvTFAhj1Y1DoaHwxb9qV1mtWQZ5qEf1O+7RJYljv3hwzUc/gsZ13nBfySpVrdiVMTVIuLC8UPuH/sv0Z/uXcbwr3jxezrhJX5dJkhz1I8gPUKHLWIhMp/jZr26ieSPz9KwupTn+MPmPyYfAoGBALPTtHqZbB4dpxPmImv2l7PgR92CwVSd/yjrfOold4Oi1bODjhNSR6/h/YghWfRHAHIoRBWTlSfu/JsffJG/2bZa2xlcpqMb/fHzg05zBtmu8ozi3CAE7Twg5bE4GtuqV1hFXK4mPxzboSmj8H4puU85GNuTA/sRDd9saZSu6CAXAoGAJ30//rR7++VCzN5EYpUhn/TzVqyyWxTbmUL9DVfG/MWgcx8kaV0H0SmJKoGhY0v1+xJRAiimN4G15V5FPVlMLtOreo5Pc2pjsduXHj/ARAKImjJbaVxJ0+dd3OsQJQgp2DXbAbqi5K+JqSUWhnd3OTYkjQB4KXWKeTLPiLNrwLcCgYEAm7l8dCLCRv4kvo2vR1E/I+zYLxHZO96qpRPwk4+ohJ0RdKg6865wF/abKDTBglGuKC2IcCriordJl0fYBxtdfJYHYFokj+FgsxLOpbPkvcPLlYerWisKCeTvI93THGDRzMYcMU87nlDvqnCmhYq6R8nJJfSVIOku20k10ST6LTcCgYEAtrTamlY8tQ9Li+yi+yeGB2nxQCVkjQE0yl2GPxGrZaXlpH2mrhshtz0UUXcDmfpUOINCc3OzgWNCymNUesmVNuaobvgERXiDv51cSDgfYNT6NZz4+JPox2sGgeZIgkvQlFPr6+OxaMl8iQLHKwIqFjJAGCajKA4CodlIRaQClqM=";
    private static final String AUTHORIZATION = "Authorization";

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
        String authVal = "Bearer " + signedToken();
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
            "  \"stripTokens\": true,\n" +
            "  \"signingKeyString\": \""+ PUBLIC_KEY_PEM +"\",\n" +
            "  \"allowedClockSkew\": 0,\n" +
            "  \"requiredClaims\": [{ \"claimName\": \"sub\", \"claimValue\": \"france frichot\" }]\n" +
            "}"
    )
    public void signedValidTokenStripAuth() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/amirante")
                .header(AUTHORIZATION, "Bearer " + signedToken());

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

    private String signedToken() throws Exception {
        JwtBuilder jwts = Jwts.builder().setSubject("france frichot")
                .signWith(SignatureAlgorithm.RS256, PemUtils.decodePrivateKey(PRIVATE_KEY_PEM));
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
}
