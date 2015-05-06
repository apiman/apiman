package io.apiman.plugins.keycloak_oauth_policy;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.impl.DefaultPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.impl.InMemorySharedStateComponent;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.keycloak_oauth_policy.beans.ForwardRoles;
import io.apiman.plugins.keycloak_oauth_policy.beans.KeycloakOauthConfigBean;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.representations.AccessToken;
import org.keycloak.util.Time;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test the {@link KeycloakOauthPolicy}.
 *
 * With thanks to the Keycloak project for their RSAVerifierTest whose setup procedures are adapted here for
 * our requirements.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings({ "nls", "deprecation" })
public class KeycloakOauthPolicyTest {

    private static X509Certificate[] idpCertificates;
    private static KeyPair idpPair;
    private AccessToken token;
    private KeycloakOauthPolicy keycloakOauthPolicy;
    private KeycloakOauthConfigBean config;
    private ServiceRequest serviceRequest;

    @Mock
    private IPolicyChain<ServiceRequest> mChain;
    @Mock
    private IPolicyContext mContext;

    static {
        if (Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate generateTestCertificate(String subject, String issuer, KeyPair pair)
            throws InvalidKeyException, NoSuchProviderException, SignatureException {
        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(new X500Principal(issuer));
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 10000));
        certGen.setSubjectDN(new X500Principal(subject));
        certGen.setPublicKey(pair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        return certGen.generateX509Certificate(pair.getPrivate(), "BC");
    }

    @BeforeClass
    public static void setupCerts() throws NoSuchAlgorithmException, InvalidKeyException,
            NoSuchProviderException, SignatureException {
        idpPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        idpCertificates = new X509Certificate[] { generateTestCertificate("CN=IDP", "CN=IDP", idpPair) };
    }

    @Before
    public void initTest() {
        MockitoAnnotations.initMocks(this);

        token = new AccessToken();
        AccessToken realm = token.subject("CN=Client").issuer("apiman-realm"); // KC seems to use issuer for realm?

        realm.addAccess("apiman-service").addRole("apiman-gateway-user-role").addRole("a-nother-role");
        realm.addAccess("a-nother-service").addRole("bacon").addRole("shoes");

        keycloakOauthPolicy = new KeycloakOauthPolicy();
        config = new KeycloakOauthConfigBean();
        config.setRequireOauth(true);
        config.setStripTokens(false);
        config.setBlacklistUnsafeTokens(false);
        config.setRequireTransportSecurity(false);

        config.setForwardRoles(new ForwardRoles());

        serviceRequest = new ServiceRequest();

        // Set up components.
        // Failure factory
        given(mContext.getComponent(IPolicyFailureFactoryComponent.class)).
            willReturn(new DefaultPolicyFailureFactoryComponent());
        // Data store
        given(mContext.getComponent(ISharedStateComponent.class)).
            willReturn(new InMemorySharedStateComponent());
    }

    private String generateAndSerializeToken() throws CertificateEncodingException, IOException {
        token.notBefore(Time.currentTime() - 100);

        config.setRealm("apiman-realm");
        config.setRealmCertificateString(certificateAsPem(idpCertificates[0]));

        return new JWSBuilder().jsonContent(token).rsa256(idpPair.getPrivate());
    }

    @Test
    public void shouldSucceedWithValidQueryAuthToken() throws CertificateEncodingException, IOException {
        String token = generateAndSerializeToken();

        serviceRequest.getQueryParams().put("access_token", token);
        keycloakOauthPolicy.apply(serviceRequest, mContext, config, mChain);

        verify(mChain, times(1)).doApply(serviceRequest);
        verify(mChain, never()).doFailure(any(PolicyFailure.class));
    }

    @Test
    public void shouldSucceedWithValidHeaderAuthToken() throws CertificateEncodingException, IOException {
        String token = generateAndSerializeToken();

        serviceRequest.getHeaders().put("Authorization", "Bearer " + token);
        keycloakOauthPolicy.apply(serviceRequest, mContext, config, mChain);

        verify(mChain, times(1)).doApply(serviceRequest);
        verify(mChain, never()).doFailure(any(PolicyFailure.class));
    }

    @Test
    public void shouldPassthroughOnNullTokenIfOAuthNotRequired() {
        config.setRequireOauth(false);
        keycloakOauthPolicy.apply(serviceRequest, mContext, config, mChain);
        verify(mChain).doApply(any(ServiceRequest.class));
    }

    @Test
    public void shouldFailIfNoToken() throws CertificateEncodingException, IOException {
        config.setRealm("apiman-realm");
        config.setRealmCertificateString(certificateAsPem(idpCertificates[0]));

        keycloakOauthPolicy.apply(serviceRequest, mContext, config, mChain);

        verify(mChain, times(1)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ServiceRequest.class));
    }

    @Test
    public void shouldFailIfTokenNotYetValid() throws CertificateEncodingException, IOException {
        token.notBefore(Time.currentTime() + 9001);

        String encoded = new JWSBuilder().jsonContent(token).rsa256(idpPair.getPrivate());

        serviceRequest.getQueryParams().put("access_token", encoded);

        config.setRealm("apiman-realm");
        config.setRealmCertificateString(certificateAsPem(idpCertificates[0]));

        keycloakOauthPolicy.apply(serviceRequest, mContext, config, mChain);

        verify(mChain, times(1)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ServiceRequest.class));
    }

    @Test
    public void shouldFailOnInsecureConnection() throws CertificateEncodingException, IOException {
        // Require transport security
        config.setRequireTransportSecurity(true);
        // But set the connection as insecure
        serviceRequest.setTransportSecure(false);

        String encoded = generateAndSerializeToken();
        serviceRequest.getHeaders().put("Authorization", "Bearer " + encoded);

        keycloakOauthPolicy.apply(serviceRequest, mContext, config, mChain);

        verify(mChain, times(1)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ServiceRequest.class));
    }

    @Test
    public void shouldBlacklistUnsafeToken() throws CertificateEncodingException, IOException {
        // Require transport security
        config.setRequireTransportSecurity(true);
        // Blacklist invalidly used tokens
        config.setBlacklistUnsafeTokens(true);
        // But set the connection as insecure
        serviceRequest.setTransportSecure(false);

        String encoded = generateAndSerializeToken();
        serviceRequest.getHeaders().put("Authorization", "Bearer " + encoded);

        keycloakOauthPolicy.apply(serviceRequest, mContext, config, mChain);

        verify(mChain, times(1)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ServiceRequest.class));
    }

    @Test
    public void shouldTerminateOnBlacklistedToken() throws CertificateEncodingException, IOException {
        config.setRequireTransportSecurity(true);
        config.setBlacklistUnsafeTokens(true);
        serviceRequest.setTransportSecure(false);

        // First, do a request that causes the token to be blacklisted.
        String encoded = generateAndSerializeToken();
        serviceRequest.getHeaders().put("Authorization", "Bearer " + encoded);
        keycloakOauthPolicy.apply(serviceRequest, mContext, config, mChain);

        // Second, do the request again with the blacklisted token *with secure*.
        // It *must* still be blocked.
        serviceRequest.setTransportSecure(true);
        keycloakOauthPolicy.apply(serviceRequest, mContext, config, mChain);

        verify(mChain, times(2)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ServiceRequest.class));
    }

    private String certificateAsPem(X509Certificate x509) throws CertificateEncodingException, IOException {
        StringWriter sw = new StringWriter();
        PemWriter writer = new PemWriter(sw);
        PemObject pemObject = new PemObject("CERTIFICATE", x509.getEncoded());
        try {
            writer.writeObject(pemObject);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            writer.close();
        }
        return sw.toString();
    }
}
