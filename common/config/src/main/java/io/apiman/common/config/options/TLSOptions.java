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
package io.apiman.common.config.options;

import java.util.Arrays;
import java.util.Map;

/**
 * Options parser for TLS/SSL.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class TLSOptions extends AbstractOptions {
    public static final String PREFIX = "tls."; //$NON-NLS-1$
    public static final String TLS_TRUSTSTORE = PREFIX + "trustStore"; //$NON-NLS-1$
    public static final String TLS_TRUSTSTOREPASSWORD = PREFIX + "trustStorePassword"; //$NON-NLS-1$
    public static final String TLS_KEYSTORE = PREFIX + "keyStore"; //$NON-NLS-1$
    public static final String TLS_KEYSTOREPASSWORD = PREFIX + "keystorePassword"; //$NON-NLS-1$
    public static final String TLS_KEYALIASES = PREFIX + "keyAliases"; //$NON-NLS-1$
    public static final String TLS_KEYPASSWORD = PREFIX + "keyPassword"; //$NON-NLS-1$
    public static final String TLS_ALLOWEDPROTOCOLS = PREFIX + "allowedProtocols"; //$NON-NLS-1$
    public static final String TLS_ALLOWEDCIPHERS = PREFIX + "allowedCiphers"; //$NON-NLS-1$
    public static final String TLS_ALLOWANYHOST = PREFIX + "allowAnyHost"; //$NON-NLS-1$
    public static final String TLS_ALLOWSELFSIGNED = PREFIX + "allowSelfSigned"; //$NON-NLS-1$
    public static final String TLS_DEVMODE = PREFIX + "devMode"; //$NON-NLS-1$

    private String trustStore;
    private String trustStorePassword;
    private String clientKeyStore;
    private String keyStorePassword;
    private String keyPassword;
    private String[] allowedProtocols;
    private String[] allowedCiphers;
    private boolean allowAnyHost;
    private boolean trustSelfSigned;
    private boolean devMode;
    private String[] keyAliases;

    /**
     * Constructor. Parses options immediately.
     * @param options the options
     */
    public TLSOptions(Map<String, String> options) {
        super(options);
    }

    /**
     * @see io.apiman.common.config.options.AbstractOptions#parse(java.util.Map)
     */
    @Override
    protected void parse(Map<String, String> options) {
        trustStore = getVar(options, TLS_TRUSTSTORE);
        trustStorePassword = getVar(options, TLS_TRUSTSTOREPASSWORD);
        clientKeyStore = getVar(options, TLS_KEYSTORE);
        keyStorePassword = getVar(options, TLS_KEYSTOREPASSWORD);
        keyAliases = split(getVar(options, TLS_KEYALIASES), ',');
        keyPassword = getVar(options, TLS_KEYPASSWORD);
        allowedProtocols = split(getVar(options, TLS_ALLOWEDPROTOCOLS), ',');
        allowedCiphers = split(getVar(options, TLS_ALLOWEDCIPHERS), ',');
        allowAnyHost = parseBool(options, TLS_ALLOWANYHOST);
        trustSelfSigned = parseBool(options, TLS_ALLOWSELFSIGNED);
        devMode = parseBool(options, TLS_DEVMODE);
    }

    /**
     * @return the trustStore
     */
    public String getTrustStore() {
        return trustStore;
    }

    /**
     * @param trustStore the trustStore to set
     */
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * @return the trustStorePassword
     */
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * @param trustStorePassword the trustStorePassword to set
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * @return the clientKeyStore
     */
    public String getKeyStore() {
        return clientKeyStore;
    }

    /**
     * @param clientKeyStore the clientKeyStore to set
     */
    public void setClientKeyStore(String clientKeyStore) {
        this.clientKeyStore = clientKeyStore;
    }

    /**
     * @return the keyStorePassword
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * @param keyStorePassword the keyStorePassword to set
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * @return the keyPassword
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * @param keyPassword the keyPassword to set
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * @return the allowedProtocols
     */
    public String[] getAllowedProtocols() {
        return allowedProtocols;
    }

    /**
     * @param allowedProtocols the allowedProtocols to set
     */
    public void setAllowedProtocols(String[] allowedProtocols) {
        this.allowedProtocols = allowedProtocols;
    }

    /**
     * @return the allowedCiphers
     */
    public String[] getAllowedCiphers() {
        return allowedCiphers;
    }

    /**
     * @param allowedCiphers the allowedCiphers to set
     */
    public void setAllowedCiphers(String[] allowedCiphers) {
        this.allowedCiphers = allowedCiphers;
    }

    /**
     * @return the allowAnyHost
     */
    public boolean isAllowAnyHost() {
        return allowAnyHost;
    }

    /**
     * @param allowAnyHost the allowAnyHost to set
     */
    public void setAllowAnyHost(boolean allowAnyHost) {
        this.allowAnyHost = allowAnyHost;
    }

    /**
     * @return the trustSelfSigned
     */
    public boolean isTrustSelfSigned() {
        return trustSelfSigned;
    }

    /**
     * @param allowSelfSigned the allowSelfSigned to set
     */
    public void setAllowSelfSigned(boolean allowSelfSigned) {
        this.trustSelfSigned = allowSelfSigned;
    }

    /**
     * @return the devMode
     */
    public boolean isDevMode() {
        return devMode;
    }

    /**
     * @param devMode the devMode to set
     */
    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    /**
     * @return the keyAliases
     */
    public String[] getKeyAliases() {
        return keyAliases;
    }

    /**
     * @param keyAliases the keyAliases to set
     */
    public void setKeyAliases(String[] keyAliases) {
        this.keyAliases = keyAliases;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (allowAnyHost ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(allowedCiphers);
        result = prime * result + Arrays.hashCode(allowedProtocols);
        result = prime * result + ((clientKeyStore == null) ? 0 : clientKeyStore.hashCode());
        result = prime * result + (devMode ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(keyAliases);
        result = prime * result + ((keyPassword == null) ? 0 : keyPassword.hashCode());
        result = prime * result + ((keyStorePassword == null) ? 0 : keyStorePassword.hashCode());
        result = prime * result + (trustSelfSigned ? 1231 : 1237);
        result = prime * result + ((trustStore == null) ? 0 : trustStore.hashCode());
        result = prime * result + ((trustStorePassword == null) ? 0 : trustStorePassword.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TLSOptions other = (TLSOptions) obj;
        if (allowAnyHost != other.allowAnyHost)
            return false;
        if (!Arrays.equals(allowedCiphers, other.allowedCiphers))
            return false;
        if (!Arrays.equals(allowedProtocols, other.allowedProtocols))
            return false;
        if (clientKeyStore == null) {
            if (other.clientKeyStore != null)
                return false;
        } else if (!clientKeyStore.equals(other.clientKeyStore))
            return false;
        if (devMode != other.devMode)
            return false;
        if (!Arrays.equals(keyAliases, other.keyAliases))
            return false;
        if (keyPassword == null) {
            if (other.keyPassword != null)
                return false;
        } else if (!keyPassword.equals(other.keyPassword))
            return false;
        if (keyStorePassword == null) {
            if (other.keyStorePassword != null)
                return false;
        } else if (!keyStorePassword.equals(other.keyStorePassword))
            return false;
        if (trustSelfSigned != other.trustSelfSigned)
            return false;
        if (trustStore == null) {
            if (other.trustStore != null)
                return false;
        } else if (!trustStore.equals(other.trustStore))
            return false;
        if (trustStorePassword == null) {
            if (other.trustStorePassword != null)
                return false;
        } else if (!trustStorePassword.equals(other.trustStorePassword))
            return false;
        return true;
    }
}
