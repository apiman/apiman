package io.apiman.migration.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.squareup.okhttp.OkHttpClient;

/**
 * OkHttp client utilities.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class OkHttpUtils {
    public static OkHttpClient createClient(boolean trustAll) {
        OkHttpClient client = new OkHttpClient();
        if (trustAll) {
            client.setHostnameVerifier(OkHttpUtils.getUnsafeHostnameVerifier());
            client.setSslSocketFactory(OkHttpUtils.getUnsafeSslSocketFactory());
        }
        return client;
    }

    public static SSLSocketFactory getUnsafeSslSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager trustManager = new X509TrustManager() {

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
            };


            sslContext.init(null, new TrustManager[]{ trustManager } , new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public static HostnameVerifier getUnsafeHostnameVerifier() {
        return (hostname, session) -> true;
    }
}
