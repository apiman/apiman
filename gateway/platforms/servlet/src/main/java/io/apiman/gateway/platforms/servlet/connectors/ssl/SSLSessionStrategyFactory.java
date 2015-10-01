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

import io.apiman.common.config.options.TLSOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.PrivateKeyDetails;
import org.apache.http.conn.ssl.PrivateKeyStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.util.Args;

/**
 * Factory to produce {@link SSLSessionStrategy}.
 *
 * @author Marc Savy
 */
public class SSLSessionStrategyFactory {
    private SSLSessionStrategyFactory() {}
    private static final HostnameVerifier ALLOW_ANY = new AllowAnyVerifier();
    private static final TrustStrategy SELF_SIGNED = new TrustSelfSignedStrategy();
    private static String[] EMPTY = new String[]{};

    /**
     * Convenience function parses map of options to generate {@link SSLSessionStrategy}.
     * <p>
     * Defaults are provided for all fields:
     * <p>
     * <ul>
     *   <li>trustStore - default: <a href="https://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores">JSSERefGuide</a></li>
     *   <li>trustStorePassword - none</li>
     *   <li>allowedProtocols - {@link SSLParameters#getProtocols()}</li>
     *   <li>disallowedProtocols - {@link SSLParameters#getCipherSuites()}</li>
     *   <li>allowedCiphers - {@link SSLParameters#getCipherSuites()}</li>
     *   <li>disallowedCiphers - {@link SSLParameters#getCipherSuites()}</li>
     *   <li>allowAnyHost - false</li>
     *   <li>allowSelfSigned - false</li>
     * </ul>
     *
     * @param optionsMap map of options
     * @return the SSL session strategy
     * @see #build(String, String, String, String, String[], String, String[], String[], boolean, boolean)
     * @throws NoSuchAlgorithmException if the selected algorithm is not available on the system
     * @throws KeyManagementException when particular cryptographic algorithm not available
     * @throws KeyStoreException problem with keystore
     * @throws CertificateException if there was a problem with the certificate
     * @throws IOException if the truststore could not be found or was invalid
     * @throws UnrecoverableKeyException a key in keystore cannot be recovered
     */
    public static SSLSessionStrategy buildStandard(TLSOptions optionsMap)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
            UnrecoverableKeyException, CertificateException, IOException {
        String[] allowedProtocols = arrayDifference(optionsMap.getAllowedProtocols(),
                optionsMap.getDisallowedProtocols(),
                getDefaultProtocols());
        String[] allowedCiphers = arrayDifference(optionsMap.getAllowedCiphers(),
                optionsMap.getDisallowedCiphers(),
                getDefaultCipherSuites());

        return build(optionsMap.getTrustStore(),
                optionsMap.getTrustStorePassword(),
                null, null, null, null, // All keyStore related stuff
                allowedProtocols,
                allowedCiphers,
                optionsMap.isAllowAnyHost(),
                optionsMap.isTrustSelfSigned());
    }

    /**
     * Convenience function parses map of options to generate {@link SSLSessionStrategy}.
     * <p>
     * Defaults are provided for some fields, others are options. ClientKeystore is required:
     * <p>
     * <ul>
     *   <li>trustStore - default: <a href="https://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores">JSSERefGuide</a></li>
     *   <li>trustStorePassword - none</li>
     *   <li>keyStore - required</li>
     *   <li>keyStorePassword - none</li>
     *   <li>keyAliases - none</li>
     *   <li>keyPassword - none</li>
     *   <li>allowedProtocols - {@link SSLParameters#getProtocols()}</li>
     *   <li>disallowedProtocols - {@link SSLParameters#getCipherSuites()}</li>
     *   <li>allowedCiphers - {@link SSLParameters#getCipherSuites()}</li>
     *   <li>disallowedCiphers - {@link SSLParameters#getCipherSuites()}</li>
     *   <li>allowAnyHost - false</li>
     *   <li>allowSelfSigned - false</li>
     * </ul>
     *
     * @param optionsMap map of options
     * @return the SSL session strategy
     * @see #build(String, String, String, String, String[], String, String[], String[], boolean, boolean)
     * @throws NoSuchAlgorithmException if the selected algorithm is not available on the system
     * @throws KeyManagementException when particular cryptographic algorithm not available
     * @throws KeyStoreException problem with keystore
     * @throws CertificateException if there was a problem with the certificate
     * @throws IOException if the truststore could not be found or was invalid
     * @throws UnrecoverableKeyException a key in keystore cannot be recovered
     */
    @SuppressWarnings("nls")
    public static SSLSessionStrategy buildMutual(TLSOptions optionsMap)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
            IOException, UnrecoverableKeyException {
        Args.notNull(optionsMap.getKeyStore(), "KeyStore");
        Args.notEmpty(optionsMap.getKeyStore(), "KeyStore must not be empty");

        String[] allowedProtocols = arrayDifference(optionsMap.getAllowedProtocols(),
                optionsMap.getDisallowedProtocols(),
                getDefaultProtocols());
        String[] allowedCiphers = arrayDifference(optionsMap.getAllowedCiphers(),
                optionsMap.getDisallowedCiphers(),
                getDefaultCipherSuites());

        return build(optionsMap.getTrustStore(),
                optionsMap.getTrustStorePassword(),
                optionsMap.getKeyStore(),
                optionsMap.getKeyStorePassword(),
                optionsMap.getKeyAliases(),
                optionsMap.getKeyPassword(),
                allowedProtocols,
                allowedCiphers,
                optionsMap.isAllowAnyHost(),
                optionsMap.isTrustSelfSigned());
    }

    /**
     * Build an {@link SSLSessionStrategy}.
     *
     * @param trustStore the trust store
     * @param trustStorePassword the truststore password (if any)
     * @param keyStore the keystore
     * @param keyStorePassword the keystore password (if any)
     * @param keyAliases the key aliases that are candidates for use (if any)
     * @param keyPassword the key password (if any)
     * @param allowedProtocols the allowed transport protocols.
     *            <strong><em>Avoid specifying insecure protocols</em></strong>
     * @param allowedCiphers allowed crypto ciphersuites, <tt>null</tt> to use system defaults
     * @param trustSelfSigned true if self signed certificates can be trusted.
     *             <strong><em>Use with caution</em></strong>
     * @param allowAnyHostname true if any hostname can be connected to (i.e. does not need to match
     *            certificate hostname). <strong><em>Do not use in production</em></strong>
     * @return the connection socket factory
     * @throws NoSuchAlgorithmException if the selected algorithm is not available on the system
     * @throws KeyStoreException if there was a problem with the keystore
     * @throws CertificateException if there was a problem with the certificate
     * @throws IOException if the truststore could not be found or was invalid
     * @throws KeyManagementException if there is a problem with keys
     * @throws UnrecoverableKeyException if the key cannot be recovered
     */
    public static SSLSessionStrategy build(String trustStore,
            String trustStorePassword,
            String keyStore,
            String keyStorePassword,
            String[] keyAliases,
            String keyPassword,
            String[] allowedProtocols,
            String[] allowedCiphers,
            boolean allowAnyHostname,
            boolean trustSelfSigned)

    throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException,
            KeyManagementException, UnrecoverableKeyException {

        Args.notNull(allowedProtocols, "Allowed protocols"); //$NON-NLS-1$
        Args.notNull(allowedCiphers, "Allowed ciphers"); //$NON-NLS-1$

        TrustStrategy trustStrategy = trustSelfSigned ?  SELF_SIGNED : null;
        HostnameVerifier hostnameVerifier = allowAnyHostname ? ALLOW_ANY :
            SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
        PrivateKeyStrategy privateKeyStrategy = keyAliases == null ? null : new SelectByAlias(keyAliases);
        boolean clientAuth = keyStore == null ? false : true;

        SSLContextBuilder builder = SSLContexts.custom();

        if (trustStore != null) {
            loadTrustMaterial(builder,
                    new File(trustStore),
                    trustStorePassword.toCharArray(),
                    trustStrategy);
        }

        if (keyStore != null) {
            char[] ksp = keyStorePassword == null ? null : keyStorePassword.toCharArray();
            char[] kp = keyPassword == null ? null : keyPassword.toCharArray();
            loadKeyMaterial(builder, new File(keyStore), ksp, kp, privateKeyStrategy);
        }

        SSLContext sslContext = builder.build();
        return new SSLSessionStrategy(hostnameVerifier, new CipherSelectingSSLSocketFactory(
                sslContext.getSocketFactory(), allowedCiphers, allowedProtocols, clientAuth));
    }

    /**
     * <strong><em>Do not use in production</em></strong>
     * <p>
     * Returns an SSLSessionStrategy that trusts any Certificate.
     * <p>
     * Naturally, this is vulnerable to a raft of MIITM and forgery attacks, so users should exercise extreme
     * caution and only use it for development purposes.
     *
     * @return the ssl strategy
     */
    public static SSLSessionStrategy buildUnsafe() {
        System.err.println("ATTENTION: SSLSessionStrategy will trust *any* certificate." //$NON-NLS-1$
                + " This is extremely unsafe for production. Caveat utilitor!"); //$NON-NLS-1$

        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1"); //$NON-NLS-1$

            // This accepts anything.
            sslContext.init(null, new X509TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());

            return new SSLSessionStrategy(ALLOW_ANY, sslContext.getSocketFactory());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] getDefaultCipherSuites() {
        try {
            return SSLContext.getDefault().getDefaultSSLParameters().getCipherSuites();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] getDefaultProtocols() {
        try {
            return SSLContext.getDefault().getDefaultSSLParameters().getProtocols();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * List A - List B
     *
     * @param allowed allowed items
     * @param disallowed disallowed items
     * @return allowed items minus disallowed items
     */
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

    private static final class SelectByAlias implements PrivateKeyStrategy {
        private Set<String> keyAliases = new HashSet<>();

        public SelectByAlias(String[] keyAliases) {
            for (String k : keyAliases) {
                this.keyAliases.add(k);
            }
        }

        @Override
        public String chooseAlias(Map<String, PrivateKeyDetails> aliases, Socket socket) {
            for (String k : aliases.keySet()) {
              if (keyAliases.contains(k)) {
                  return k;
              }
            }
            return null;
        }
    }

    // This code is adapted from
    // apache/httpcore/blob/4.4.x/httpcore/src/main/java/org/apache/http/ssl/SSLContextBuilder.java
    // for compatibility purposes.
    private static SSLContextBuilder loadTrustMaterial(SSLContextBuilder builder, final File file,
            final char[] tsp, final TrustStrategy trustStrategy)
                    throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        Args.notNull(file, "Truststore file"); //$NON-NLS-1$
        final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final FileInputStream instream = new FileInputStream(file);
        try {
            trustStore.load(instream, tsp);
        } finally {
            instream.close();
        }
        return builder.loadTrustMaterial(trustStore, trustStrategy);
    }

    // This code is adapted from
    // apache/httpcore/blob/4.4.x/httpcore/src/main/java/org/apache/http/ssl/SSLContextBuilder.java
    // for compatibility purposes.
    private static SSLContextBuilder loadKeyMaterial(SSLContextBuilder builder, File file, char[] ksp,
            char[] kp, PrivateKeyStrategy privateKeyStrategy) throws NoSuchAlgorithmException,
                    KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        Args.notNull(file, "Keystore file"); //$NON-NLS-1$
        final KeyStore identityStore = KeyStore.getInstance(KeyStore.getDefaultType());
        final FileInputStream instream = new FileInputStream(file);
        try {
            identityStore.load(instream, ksp);
        } finally {
            instream.close();
        }
        return builder.loadKeyMaterial(identityStore, kp, privateKeyStrategy);
    }

}
