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
package io.apiman.common.auth;

import java.util.Date;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    private static Logger logger = LoggerFactory.getLogger(AuthTokenUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static ISharedSecretSource sharedSecretSource;
    static {
        ServiceLoader<ISharedSecretSource> loader = ServiceLoader.load(ISharedSecretSource.class);
        for (ISharedSecretSource source : loader) {
            sharedSecretSource = source;
            // Just use the first one you find.
            break;
        }
        if (sharedSecretSource == null) {
            logger.warn("Missing a Shared-Secret source for auth-token style authentication.  Defaulting to insecure, hard-coded value."); //$NON-NLS-1$
            sharedSecretSource = new ISharedSecretSource() {
                @Override
                public String getSharedSecret() {
                    // Hard coded shared secret.  Should not be used in production (see above warning).
                    return "2BB6E867BC7564162AB1FD26BE61E49365934FBA9B3E56B1323ABE104C798D5C"; //$NON-NLS-1$
                }
            };
        }
    }
    
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
        String validSig = generateSignature(token);
        if (token.getSignature() == null || !token.getSignature().equals(validSig)) {
            throw new IllegalArgumentException("Missing or invalid signature on the auth token."); //$NON-NLS-1$
        }
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
        String signature = generateSignature(token);
        token.setSignature(signature);
    }

    /**
     * Generates a signature for the given token.
     * @param token
     */
    private static String generateSignature(AuthToken token) {
        StringBuilder builder = new StringBuilder();
        builder.append(token.getPrincipal());
        builder.append("||"); //$NON-NLS-1$
        builder.append(token.getExpiresOn().getTime());
        builder.append("||"); //$NON-NLS-1$
        builder.append(token.getIssuedOn().getTime());
        builder.append("||"); //$NON-NLS-1$
        TreeSet<String> roles = new TreeSet<String>(token.getRoles());
        boolean first = true;
        for (String role : roles) {
            if (first) {
                first = false;
            } else {
                builder.append(","); //$NON-NLS-1$
            }
            builder.append(role);
        }
        builder.append("||"); //$NON-NLS-1$
        builder.append(sharedSecretSource.getSharedSecret());
        return DigestUtils.sha256Hex(builder.toString());
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
