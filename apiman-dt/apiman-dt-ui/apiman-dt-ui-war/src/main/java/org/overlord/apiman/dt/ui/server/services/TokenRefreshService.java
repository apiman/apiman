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
package org.overlord.apiman.dt.ui.server.services;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.overlord.apiman.dt.ui.client.shared.beans.BearerTokenCredentialsBean;
import org.overlord.apiman.dt.ui.client.shared.services.ITokenRefreshService;
import org.overlord.apiman.dt.ui.server.ApimanUIConfig;
import org.overlord.apiman.dt.ui.server.auth.ITokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the token refresh service.  Creates new tokens
 * that the UI can use when invoking the apiman dt api.
 *
 * @author eric.wittmann@redhat.com
 */
@Service
public class TokenRefreshService implements ITokenRefreshService {

    private static Logger logger = LoggerFactory.getLogger(TokenRefreshService.class);

    /**
     * Constructor.
     */
    public TokenRefreshService() {
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.shared.services.ITokenRefreshService#refreshToken()
     */
    @Override
    public BearerTokenCredentialsBean refreshToken() {
        logger.debug("Refreshing authentication token."); //$NON-NLS-1$
        ServletRequest request = RpcContext.getServletRequest();
        String tokenGeneratorClassName = ApimanUIConfig.config.getString(ApimanUIConfig.APIMAN_DT_UI_API_AUTH_TOKEN_GENERATOR);
        if (tokenGeneratorClassName == null)
            throw new RuntimeException("No token generator class specified."); //$NON-NLS-1$
        try {
            Class<?> c = Class.forName(tokenGeneratorClassName);
            ITokenGenerator tokenGenerator = (ITokenGenerator) c.newInstance();
            String token = tokenGenerator.generateToken((HttpServletRequest) request);
            int refresh = tokenGenerator.getRefreshPeriod();
            BearerTokenCredentialsBean btcb = new BearerTokenCredentialsBean();
            btcb.setToken(new String(Base64.encodeBase64(token.getBytes("UTF-8")))); //$NON-NLS-1$
            btcb.setRefreshPeriod(refresh);
            return btcb;
        } catch (Exception e) {
            throw new RuntimeException("Error creating token generator."); //$NON-NLS-1$
        }
    }
}
