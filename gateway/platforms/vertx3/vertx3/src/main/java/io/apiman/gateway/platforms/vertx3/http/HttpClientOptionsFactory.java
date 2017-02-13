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
package io.apiman.gateway.platforms.vertx3.http;

import io.apiman.common.config.options.TLSOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

/**
 * Build {@link HttpClientOptions} using populated {@link TLSOptions}.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpClientOptionsFactory {
    private static final String[] EMPTY = new String[]{};
    private static Map<TLSOptions, HttpClientOptions> configCache = new HashMap<>();
    private static Logger log = LoggerFactory.getLogger(HttpClientOptionsFactory.class);

    public static HttpClientOptions parseTlsOptions(TLSOptions tlsOptions, URI apiEndpoint) {
        if (configCache.containsKey(tlsOptions))
            return configCache.get(tlsOptions);

        HttpClientOptions clientOptions = doParse(tlsOptions, apiEndpoint);
        configCache.put(tlsOptions, clientOptions);
        return clientOptions;
    }

    private static HttpClientOptions doParse(TLSOptions tlsOptions, URI apiEndpoint) {
        HttpClientOptions clientOptions = new HttpClientOptions();

        if (apiEndpoint.getScheme().equals("http")) { //$NON-NLS-1$
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
            String[] ciphers = arrayDifference(tlsOptions.getAllowedCiphers(), tlsOptions.getDisallowedCiphers(), getDefaultCipherSuites());
            for (String cipher : ciphers) {
                clientOptions.addEnabledCipherSuite(cipher);
            }
        }

        if (tlsOptions.getAllowedProtocols() != null) {
            log.info("Can't set allowed protocols on Vert.x gateway"); //$NON-NLS-1$
        }

        return clientOptions;
    }

    private static String[] arrayDifference(String[] allowed, String[] disallowed, String[] defaultItems) {
        List<String> allowL = new ArrayList<>(Arrays.asList(optionalVar(allowed, defaultItems)));
        List<String> disallowL = new ArrayList<>(Arrays.asList(optionalVar(disallowed, EMPTY)));
        allowL.removeAll(disallowL);
        return allowL.toArray(new String[allowL.size()]);
    }

    private static String[] optionalVar(String[] arr, String[] defaultArr) {
        if (arr == null || arr.length==0) {
            return defaultArr;
        }
        return arr;
    }

    private static String[] getDefaultCipherSuites() {
        try {
            return SSLContext.getDefault().getDefaultSSLParameters().getCipherSuites();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
