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

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.Args;
/**
 * Factory to produce {@link SSLConnectionSocketFactory}.
 *
 * @author Marc Savy
 */
public class SSLSessionStrategyFactory {
    private SSLSessionStrategyFactory() {}
    private static final HostnameVerifier ALLOW_ANY = new AllowAnyVerifier();
    private static final TrustStrategy SELF_SIGNED = new TrustSelfSignedStrategy();

    /**
     * Convenience function parses map of options to generate {@link SSLSessionStrategy}.
     * <p>
     * Defaults are provided for all fields:
     * <p>
     * <ul>
     *   <li>allowedProtocols - {@link #getDefaultProtocols()}</li>
     *   <li>allowedCiphers - {@link #getDefaultCipherSuites()}</li>
     *   <li>allowAnyHost - false</li>
     *   <li>allowSelfSigned - false</li>
     * </ul>
     *
     * @param optionsMap map of options
     * @return the SSL session strategy
     * @see #build(String[], String[], boolean, boolean) uses defaults
     * @throws NoSuchAlgorithmException if the selected algorithm is not available on the system
     * @throws KeyManagementException when particular cryptographic algorithm not available
     * @throws KeyStoreException problem with keystore
     */
    @SuppressWarnings("nls")
    public static SSLSessionStrategy buildStandard(Map<String, String> optionsMap)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        String allowedProtocolsStr = optionalVar(optionsMap, "allowedProtocols", null);
        String allowedCiphersStr = optionalVar(optionsMap, "allowedCiphers", null);
        String[] allowedProtocols = split(allowedProtocolsStr, ',', getDefaultProtocols());
        String[] allowedCiphers = split(allowedCiphersStr, ',', getDefaultCipherSuites());
        boolean allowAnyHost = parseBool(optionsMap, "allowAnyHost");
        boolean trustSelfSigned = parseBool(optionsMap, "allowSelfSigned");

        return build(allowedProtocols, allowedCiphers, allowAnyHost, trustSelfSigned);
    }

    /**
     * Convenience function parses map of options to generate {@link SSLSessionStrategy}.
     * <p>
     * Defaults are provided for some fields, others are required:
     * <p>
     * <ul>
     *   <li>clientKeystore - required</li>
     *   <li>keystorePassword - none</li>
     *   <li>allowedProtocols - {@link #getDefaultProtocols()}</li>
     *   <li>allowedCiphers - {@link #getDefaultCipherSuites()}</li>
     *   <li>allowAnyHost - false</li>
     *   <li>allowSelfSigned - false</li>
     * </ul>
     *
     * @param optionsMap map of options
     * @return the SSL session strategy
     * @see #build(String[], String[], boolean, boolean) uses defaults
     * @throws NoSuchAlgorithmException if the selected algorithm is not available on the system
     * @throws KeyManagementException when particular cryptographic algorithm not available
     * @throws KeyStoreException problem with keystore
     * @throws CertificateException if there was a problem with the certificate
     * @throws IOException if the truststore could not be found or was invalid
     */
    @SuppressWarnings("nls")
    public static SSLSessionStrategy buildMutual(Map<String, String> optionsMap)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
            IOException {
        String clientKeystorePath = optionsMap.get("clientKeystore");
        String keystorePasswordStr = optionalVar(optionsMap, "keystorePassword", null);
        String allowedProtocolsStr = optionalVar(optionsMap, "allowedProtocols", null);
        String allowedCiphersStr = optionalVar(optionsMap, "allowedCiphers", null);

        Args.notNull(clientKeystorePath, "Client keystore (clientKeystore)");
        Args.notEmpty(clientKeystorePath, "Client keystore (clientKeystore)");

        File clientKeystore = new File(clientKeystorePath);
        String[] allowedProtocols = split(allowedProtocolsStr, ',', getDefaultProtocols());
        String[] allowedCiphers = split(allowedCiphersStr, ',', getDefaultCipherSuites());
        boolean allowAnyHost = parseBool(optionsMap, "allowAnyHost");
        boolean trustSelfSigned = parseBool(optionsMap, "allowSelfSigned");

        return build(clientKeystore, keystorePasswordStr, allowedProtocols, allowedCiphers, allowAnyHost,
                trustSelfSigned);
    }

    private static String[] getDefaultCipherSuites() throws NoSuchAlgorithmException {
        return SSLContext.getDefault().getDefaultSSLParameters().getCipherSuites();
    }

    private static String[] getDefaultProtocols() throws NoSuchAlgorithmException {
        return SSLContext.getDefault().getDefaultSSLParameters().getProtocols();
    }

    private static String optionalVar(Map<String, String> optionsMap, String varName, String defaultValue) {
        if(optionsMap.get(varName) == null || optionsMap.get(varName).isEmpty()) {
            return defaultValue;
        }
        return optionsMap.get(varName);
    }

    private static String[] split(String str, char splitter, String[] defaults) {
        String[] splitStr = StringUtils.split(str, splitter);

        if (splitStr == null)
            return defaults;

        String[] out = new String[splitStr.length];

        for (int i = 0; i < splitStr.length; i++) {
            out[i] = StringUtils.trim(splitStr[i]);
        }

        return out;
    }

    private static boolean parseBool(Map<String, String> optionsMap, String key) {
        return BooleanUtils.toBoolean(optionsMap.get(key));
    }

    /**
     * @param clientKeystore the client keystore (trust store)
     * @param keystorePassword password the keystore password
     * @param allowedProtocols the allowed transport protocols.
     *            <strong><em>Avoid specifying insecure protocols</em></strong>
     * @param allowedCiphers allowed crypto ciphersuites, <tt>null</tt> to use system defaults
     * @param trustSelfSigned true if self signed certificates can be trusted.
     *            <strong><em>Use with caution</em></strong>
     * @param allowAnyHostname true if any hostname can be connected to (i.e. does not need to match
     *            certificate hostname). <strong><em>Do not use in production</em></strong>
     * @return the connection socket factory
     * @throws NoSuchAlgorithmException if the selected algorithm is not available on the system
     * @throws KeyStoreException if there was a problem with the keystore
     * @throws CertificateException if there was a problem with the certificate
     * @throws IOException if the truststore could not be found or was invalid
     * @throws KeyManagementException if there is a problem with keys
     */
    public static SSLSessionStrategy build(File clientKeystore,
            String keystorePassword,
            String[] allowedProtocols,
            String[] allowedCiphers,
            boolean trustSelfSigned,
            boolean allowAnyHostname)

            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, KeyManagementException {

        Args.notNull(allowedProtocols, "Allowed protocols"); //$NON-NLS-1$
        Args.notNull(allowedCiphers, "Allowed ciphers"); //$NON-NLS-1$

        TrustStrategy trustStrategy = trustSelfSigned ?  SELF_SIGNED : null;
        HostnameVerifier hostnameVerifier = allowAnyHostname ? ALLOW_ANY :
            SSLConnectionSocketFactory.getDefaultHostnameVerifier();

        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(clientKeystore,
                keystorePassword.toCharArray(),
                trustStrategy)
                .build();

        return new SSLSessionStrategy(sslContext,
                allowedProtocols,
                allowedCiphers,
                hostnameVerifier);
    }

    /**
     * @param allowedProtocols the allowed transport protocols
     *            <strong><em>Avoid specifying insecure protocols</em></strong>
     * @param allowedCiphers allowed crypto ciphersuites, <tt>null</tt> to use system defaults
     * @param trustSelfSigned true if self signed certificates can be trusted.
     *            <strong><em>Use with caution</em></strong>
     * @param allowAnyHostname true if any hostname can be connected to (i.e. does not need to match
     *            certificate hostname). <strong><em>Do not use in production</em></strong>
     * @return the connection socket factory
     * @throws NoSuchAlgorithmException if the selected algorithm is not available on the system
     * @throws KeyStoreException if there was a problem with the keystore
     * @throws KeyManagementException if there is a problem with keys
     */
    public static SSLSessionStrategy build(String[] allowedProtocols,
            String[] allowedCiphers,
            boolean trustSelfSigned,
            boolean allowAnyHostname) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        Args.notNull(allowedProtocols, "Allowed protocols"); //$NON-NLS-1$
        Args.notNull(allowedCiphers, "Allowed ciphers"); //$NON-NLS-1$

        TrustStrategy trustStrategy = trustSelfSigned ?  SELF_SIGNED : null;
        HostnameVerifier hostnameVerifier = allowAnyHostname ? ALLOW_ANY :
            SSLConnectionSocketFactory.getDefaultHostnameVerifier();

        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStrategy).build();

        return new SSLSessionStrategy(sslContext,
                allowedProtocols,
                allowedCiphers,
                hostnameVerifier);
    }

    /**
     * Allows any hostname.
     *
     * @author Marc Savy <msavy@redhat.com>
     */
    private static final class AllowAnyVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
