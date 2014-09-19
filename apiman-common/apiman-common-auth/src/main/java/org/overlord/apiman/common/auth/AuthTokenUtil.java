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
package org.overlord.apiman.common.auth;

import java.util.Date;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * A simple class to generate and consume authentication tokens.  This is a very
 * naive token-based authentication mechanism.  It is useful for unit testing and
 * demos, but should not be used in production as it is not secure.  In 
 * production, it is better to use OAuth or some other mature bearer token style
 * authentication approach.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuthTokenUtil {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Produce a token suitable for transmission.  This will generate the auth token,
     * then serialize it to a JSON string, then Base64 encode the JSON.
     * @param principal
     * @param roles
     * @param expiresInMillis
     */
    public static final String produceToken(String principal, Set<String> roles, int expiresInMillis) {
        AuthToken authToken = createAuthToken(principal, roles, expiresInMillis);
        String json = toJSON(authToken);
        return StringUtils.newStringUtf8(Base64.encodeBase64(StringUtils.getBytesUtf8(json)));
    }
    
    /**
     * Consumes an auth token, validating it during the process.  If successful, the
     * {@link AuthToken} is returned.  If the token is invalid (expired or bad 
     * signature) then an {@link IllegalArgumentException} is thrown.  Any other 
     * error will result in a runtime exception.
     * @param encodedJson
     */
    public static final AuthToken consumeToken(String encodedJson) throws IllegalArgumentException {
        String json = StringUtils.newStringUtf8(Base64.decodeBase64(encodedJson));
        AuthToken token = fromJSON(json);
        validateToken(token);
        return token;
    }
    
    /**
     * Validates an auth token.  This checks the expiration time of the token against 
     * the current system time.  It also checks the validity of the signature.
     * @param token
     */
    public static final void validateToken(AuthToken token) throws IllegalArgumentException {
        if (token.getExpiresOn().before(new Date())) {
            throw new IllegalArgumentException("Authentication token expired: " + token.getExpiresOn()); //$NON-NLS-1$
        }
        // TODO validate the signature here
    }

    /**
     * Creates an auth token.
     * @param principal
     * @param roles
     * @param expiresInMillis
     */
    public static final AuthToken createAuthToken(String principal, Set<String> roles, int expiresInMillis) {
        AuthToken token = new AuthToken();
        token.setIssuedOn(new Date());
        token.setExpiresOn(new Date(System.currentTimeMillis() + expiresInMillis));
        token.setPrincipal(principal);
        token.setRoles(roles);
        signAuthToken(token);
        return token;
    }

    /**
     * Adds a digital signature to the auth token.
     * @param token
     */
    public static final void signAuthToken(AuthToken token) {
        // TODO sign the token here
    }
    
    /**
     * Convert the auth token to a JSON string.
     * @param token
     */
    public static final String toJSON(AuthToken token) {
        try {
            return mapper.writer().writeValueAsString(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Read the auth token from the JSON string.
     * @param json
     */
    public static final AuthToken fromJSON(String json) {
        try {
            return mapper.reader(AuthToken.class).readValue(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
