/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.gateway.platforms.vertx2.http;

import io.apiman.common.config.options.TLSOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.JksOptions;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Build {@link HttpClientOptions} using populated {@link TLSOptions}.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpClientOptionsFactory {
    private static Map<TLSOptions, HttpClientOptions> configCache = new HashMap<>();

    public static HttpClientOptions parseOptions(TLSOptions tlsOptions, URL serviceEndpoint) {
        if (configCache.containsKey(tlsOptions))
            return configCache.get(tlsOptions);

        HttpClientOptions clientOptions = doParse(tlsOptions, serviceEndpoint);
        configCache.put(tlsOptions, clientOptions);
        return clientOptions;
    }

    private static HttpClientOptions doParse(TLSOptions tlsOptions, URL serviceEndpoint) {
        HttpClientOptions clientOptions = new HttpClientOptions();

        if (serviceEndpoint.getProtocol().equals("http")) { //$NON-NLS-1$
            return clientOptions.setSsl(false);
        } else {
            clientOptions.setSsl(true);
        }

        clientOptions.setTrustAll(tlsOptions.isTrustSelfSigned() || tlsOptions.isDevMode())
            .setVerifyHost(!tlsOptions.isAllowAnyHost() || tlsOptions.isDevMode());

        if (tlsOptions.getTrustStore() != null) {
            clientOptions.setTrustStoreOptions(
                new JksOptions().setPath(tlsOptions.getTrustStore()).setPassword(tlsOptions.getTrustStorePassword())
            );
        }

        if (tlsOptions.getKeyStore() != null) {
            clientOptions.setKeyStoreOptions(
                new JksOptions().setPath(tlsOptions.getKeyStore()).setPassword(tlsOptions.getKeyStorePassword())
            );
        }

        if (tlsOptions.getAllowedCiphers() != null) {
            for (String cipher : tlsOptions.getAllowedCiphers()) {
                clientOptions.addEnabledCipherSuite(cipher);
            }
        }

        if (tlsOptions.getAllowedProtocols() != null) {
            System.err.println("Can't set allowed protocols on Vert.x gateway"); //$NON-NLS-1$
        }

        return clientOptions;
    }
}
