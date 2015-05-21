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
package io.apiman.gateway.platforms.servlet.connectors.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * Encapsulates basic SSL strategy information
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class SSLSessionStrategy {

    private SSLContext sslContext;
    private String[] allowedProtocols;
    private String[] allowedCiphers;
    private HostnameVerifier hostnameVerifier;

    /**
     * Construct an {@link SSLSessionStrategy}
     *
     * @param sslContext the SSL context
     * @param allowedProtocols the allowed protocols
     * @param allowedCiphers the allowed ciphers
     * @param hostnameVerifier the hostname verifier
     */
    public SSLSessionStrategy(SSLContext sslContext, String[] allowedProtocols, String[] allowedCiphers,
            HostnameVerifier hostnameVerifier) {
        this.sslContext = sslContext;
        this.allowedProtocols = allowedProtocols;
        this.allowedCiphers = allowedCiphers;
        this.hostnameVerifier = hostnameVerifier;
    }

    /**
     * @return the sslContext
     */
    public SSLContext getSslContext() {
        return sslContext;
    }

    /**
     * @return the allowedProtocols
     */
    public String[] getAllowedProtocols() {
        return allowedProtocols;
    }

    /**
     * @return the allowedCiphers
     */
    public String[] getAllowedCiphers() {
        return allowedCiphers;
    }

    /**
     * @return the hostnameVerifier
     */
    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

}
