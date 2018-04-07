/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.engine.vertx.polling.fetchers.auth;

import java.util.Arrays;

/**
 * Auth types supported.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public enum AuthType {
    NONE(new NoneAuth()),
    BASIC(new BasicAuth()),
    OAUTH2(new OAuth2()),
    KEYCLOAKOAUTH2(new KeycloakOAuth2());

    private Authenticator auth;

    AuthType(Authenticator auth) {
        this.auth = auth;
    }

    public Authenticator getAuthenticator() {
        return auth;
    }

    public static String all() {
        return Arrays.toString(AuthType.values());
    }
}
