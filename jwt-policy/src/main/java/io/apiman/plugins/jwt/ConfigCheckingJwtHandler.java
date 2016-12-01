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

import io.apiman.plugins.jwt.beans.JWTPolicyBean;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtHandlerAdapter;

import java.util.Collections;
import java.util.Map;

/*
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ConfigCheckingJwtHandler extends  JwtHandlerAdapter<Map<String, Object>> {

    private JWTPolicyBean config;

    ConfigCheckingJwtHandler(JWTPolicyBean config) {
        this.config = config;
    }

    @Override
    public Map<String, Object> onPlaintextJwt(@SuppressWarnings("rawtypes") Jwt<Header, String> jwt) {
        if (config.getRequireSigned()) {
            super.onPlaintextJwt(jwt);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> onClaimsJwt(@SuppressWarnings("rawtypes") Jwt<Header, Claims> jwt) {
        return config.getRequireSigned() ? super.onClaimsJwt(jwt) : jwt.getBody();
    }

    @Override
    public Map<String, Object> onPlaintextJws(Jws<String> jws) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> onClaimsJws(Jws<Claims> jws) {
        return jws.getBody();
    }
}
