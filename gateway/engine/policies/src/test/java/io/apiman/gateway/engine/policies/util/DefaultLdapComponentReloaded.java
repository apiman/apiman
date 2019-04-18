package io.apiman.gateway.engine.policies.util;

import com.unboundid.util.ssl.SSLUtil;
import io.apiman.gateway.engine.impl.DefaultLdapComponent;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * For tests only
 * Used to force reloading static SSLContext and SSLSocketFactory, without modifying DefaultLdapComponent
 *
 * @author jhauray
 */
public class DefaultLdapComponentReloaded extends DefaultLdapComponent {

    public DefaultLdapComponentReloaded() throws GeneralSecurityException {

        //Force reloading static SSLSocketFactory to accept truststore changes.
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmFactory.init((KeyStore) null);

        X509TrustManager trustManager = null;
        for (TrustManager tm : tmFactory.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                trustManager = (X509TrustManager) tm;
                break;
            }
        }

        DEFAULT_SOCKET_FACTORY = new SSLUtil(trustManager).createSSLSocketFactory();
    }
}
