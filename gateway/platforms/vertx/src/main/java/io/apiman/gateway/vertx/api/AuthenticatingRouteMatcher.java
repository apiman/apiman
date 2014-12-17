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
package io.apiman.gateway.vertx.api;

import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.logging.Logger;

/**
 * A RouteMatcher with BASIC authentication.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class AuthenticatingRouteMatcher extends RouteMatcher {

    private Map<String, String> fileBasicAuthData;
    private Logger logger;
    private VertxEngineConfig config;

    public AuthenticatingRouteMatcher(VertxEngineConfig config, Logger logger) {
        this.config = config;
        this.fileBasicAuthData = config.loadFileBasicAuth();
        this.logger = logger;
    }

    @Override
    public void handle(HttpServerRequest request) {
        if(!config.isAuthenticationEnabled() || authenticate(request)) {
            super.handle(request);
        } else {
            notAuthorised(request.response());
        }
    }

    private boolean authenticate(HttpServerRequest request) {
        String authString = request.headers().get(HttpHeaders.AUTHORIZATION);

        if(authString == null)
            return false;

        String[] basicAuth = StringUtils.splitByWholeSeparator(authString, "Basic "); //$NON-NLS-1$

        if(basicAuth.length == 1) {
            return basicAuth(request, basicAuth[0]);
        }

        return false;
    }

    private boolean basicAuth(HttpServerRequest request, String encodedAuth) {
        byte[] authBytes = Base64.decodeBase64(encodedAuth);
        String decodedString = new String(authBytes);
        String[] splitAuth = StringUtils.split(StringUtils.trim(decodedString), ":"); //$NON-NLS-1$

        if(splitAuth.length != 2)
            return false;

        if(fileBasicAuthData.containsKey(splitAuth[0])) {
            String storedHash = new String (Base64.decodeBase64(fileBasicAuthData.get(splitAuth[0])));

            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
                digest.update(splitAuth[1].getBytes());

                String receivedHash = new String(digest.digest());

                if(storedHash.equals(receivedHash)) {
                    return true;
                }
            } catch (NoSuchAlgorithmException e) {
                logger.error(e.getMessage(), e.getCause());
            }
      }

      request.response().headers().add("WWW-Authenticate", "Basic realm=\""+ config.getRealm() + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      return false;
    }

    private void notAuthorised(HttpServerResponse response) {
        response.setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
        response.setStatusMessage(HttpResponseStatus.UNAUTHORIZED.reasonPhrase());
        response.end();
    }
}
