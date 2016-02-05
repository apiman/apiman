package io.apiman.plugins.keycloak_oauth_policy;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.keycloak.common.util.Time;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.keycloak.representations.AddressClaimSet;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.impl.DefaultPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.impl.InMemorySharedStateComponent;
import io.apiman.gateway.engine.policies.AuthorizationPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.keycloak_oauth_policy.beans.ForwardAuthInfo;
import io.apiman.plugins.keycloak_oauth_policy.beans.ForwardRoles;
import io.apiman.plugins.keycloak_oauth_policy.beans.KeycloakOauthConfigBean;

/**
 * Test the {@link KeycloakOauthPolicy}.
 *
 * With thanks to the Keycloak project for their RSAVerifierTest whose setup procedures are adapted here for
 * our requirements.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings({ "nls", "deprecation" })
public class KeycloakOauthPolicyTest {

    private static X509Certificate[] idpCertificates;
    private static KeyPair idpPair;
    private AccessToken token;
    private KeycloakOauthPolicy keycloakOauthPolicy;
    private KeycloakOauthConfigBean config;
    private ApiRequest apiRequest;

    @Mock
    private IPolicyChain<ApiRequest> mChain;
    @Mock
    private IPolicyContext mContext;
    private ForwardRoles forwardRoles;

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

        realm.addAccess("apiman-api").addRole("apiman-gateway-user-role").addRole("a-nother-role");
        realm.setRealmAccess(new Access().addRole("lets-use-a-realm-role"));

        keycloakOauthPolicy = new KeycloakOauthPolicy();
        config = new KeycloakOauthConfigBean();
        config.setRequireOauth(true);
        config.setStripTokens(false);
        config.setBlacklistUnsafeTokens(false);
        config.setRequireTransportSecurity(false);

        forwardRoles = new ForwardRoles();
        config.setForwardRoles(forwardRoles);

        apiRequest = new ApiRequest();

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

        apiRequest.getQueryParams().put("access_token", token);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain, times(1)).doApply(apiRequest);
        verify(mChain, never()).doFailure(any(PolicyFailure.class));
    }

    @Test
    public void shouldSucceedWithValidHeaderAuthToken() throws CertificateEncodingException, IOException {
        String token = generateAndSerializeToken();

        apiRequest.getHeaders().put("Authorization", "Bearer " + token);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain, times(1)).doApply(apiRequest);
        verify(mChain, never()).doFailure(any(PolicyFailure.class));
    }

    @Test
    public void shouldPassthroughOnNullTokenIfOAuthNotRequired() {
        config.setRequireOauth(false);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);
        verify(mChain).doApply(any(ApiRequest.class));
    }

    @Test
    public void shouldFailIfNoToken() throws CertificateEncodingException, IOException {
        config.setRealm("apiman-realm");
        config.setRealmCertificateString(certificateAsPem(idpCertificates[0]));

        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain, times(1)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ApiRequest.class));
    }

    @Test
    public void shouldFailIfTokenNotYetValid() throws CertificateEncodingException, IOException {
        token.notBefore(Time.currentTime() + 9001);

        String encoded = new JWSBuilder().jsonContent(token).rsa256(idpPair.getPrivate());

        apiRequest.getQueryParams().put("access_token", encoded);

        config.setRealm("apiman-realm");
        config.setRealmCertificateString(certificateAsPem(idpCertificates[0]));

        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain, times(1)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ApiRequest.class));
    }

    @Test
    public void shouldFailOnInsecureConnection() throws CertificateEncodingException, IOException {
        // Require transport security
        config.setRequireTransportSecurity(true);
        // But set the connection as insecure
        apiRequest.setTransportSecure(false);

        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);

        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain, times(1)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ApiRequest.class));
    }

    @Test
    public void shouldBlacklistUnsafeToken() throws CertificateEncodingException, IOException {
        // Require transport security
        config.setRequireTransportSecurity(true);
        // Blacklist invalidly used tokens
        config.setBlacklistUnsafeTokens(true);
        // But set the connection as insecure
        apiRequest.setTransportSecure(false);

        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);

        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain, times(1)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ApiRequest.class));
    }

    @Test
    public void shouldTerminateOnBlacklistedToken() throws CertificateEncodingException, IOException {
        config.setRequireTransportSecurity(true);
        config.setBlacklistUnsafeTokens(true);
        apiRequest.setTransportSecure(false);

        // First, do a request that causes the token to be blacklisted.
        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        // Second, do the request again with the blacklisted token *with secure*.
        // It *must* still be blocked.
        apiRequest.setTransportSecure(true);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain, times(2)).doFailure(any(PolicyFailure.class));
        verify(mChain, never()).doApply(any(ApiRequest.class));
    }

    @SuppressWarnings("serial")
    @Test
    public void shouldForwardAppRoles() throws CertificateEncodingException, IOException {
        forwardRoles.setActive(true);
        forwardRoles.setApplicationName("apiman-api");

        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);

        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        Set<String> roles = new HashSet<String>() {
            {
                add("apiman-gateway-user-role");
                add("a-nother-role");
            }
         };

        verify(mContext).setAttribute(eq(AuthorizationPolicy.AUTHENTICATED_USER_ROLES), eq(roles));
        verify(mChain).doApply(any(ApiRequest.class));
    }

    @Test
    public void shouldForwardRealmRoles() throws CertificateEncodingException, IOException {
        forwardRoles.setActive(true);

        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);

        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        @SuppressWarnings("serial")
        Set<String> roles = new HashSet<String>() {
            {
                add("lets-use-a-realm-role");
            }
         };

        verify(mContext).setAttribute(eq(AuthorizationPolicy.AUTHENTICATED_USER_ROLES), eq(roles));
        verify(mChain).doApply(any(ApiRequest.class));
    }

    @Test
    public void shouldForwardAuthInfoName() throws CertificateEncodingException, IOException {
        ForwardAuthInfo authInfo = new ForwardAuthInfo();
        authInfo.setHeaders("X-TEST");
        authInfo.setField("preferred_username");
        config.getForwardAuthInfo().add(authInfo);

        token.setPreferredUsername("ABC");
        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain).doApply(apiRequest);

        Assert.assertEquals("ABC", apiRequest.getHeaders().get("X-TEST"));
    }

    @Test
    public void shouldForwardToken() throws CertificateEncodingException, IOException {
        ForwardAuthInfo authInfo = new ForwardAuthInfo();
        authInfo.setHeaders("X-TEST");
        authInfo.setField("access_token");
        config.getForwardAuthInfo().add(authInfo);

        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain).doApply(apiRequest);

        Assert.assertEquals(encoded, apiRequest.getHeaders().get("X-TEST"));
    }

    @Test
    public void shouldAccessSubClaim() throws CertificateEncodingException, IOException {
        ForwardAuthInfo authInfo = new ForwardAuthInfo();
        authInfo.setHeaders("X-TEST");
        authInfo.setField("address.street_address");
        config.getForwardAuthInfo().add(authInfo);

        AddressClaimSet addressClaim = new AddressClaimSet();
        addressClaim.setStreetAddress("123 newcastle street, miami");
        token.setAddress(addressClaim);

        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain).doApply(apiRequest);

        Assert.assertEquals("123 newcastle street, miami", apiRequest.getHeaders().get("X-TEST"));
    }

    @Test
    public void shouldBeNullForInvalidNestedClaimLookup()  throws CertificateEncodingException, IOException {
        ForwardAuthInfo authInfo = new ForwardAuthInfo();
        authInfo.setHeaders("X-TEST");
        authInfo.setField("address.street_address");
        config.getForwardAuthInfo().add(authInfo);
        // Do *not* set street address

        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain).doApply(apiRequest);

        Assert.assertEquals(null, apiRequest.getHeaders().get("X-TEST"));
    }

    @Test
    public void shouldBeNullForInvalidOtherClaimLookup()  throws CertificateEncodingException, IOException {
        ForwardAuthInfo authInfo = new ForwardAuthInfo();
        authInfo.setHeaders("X-TEST");
        authInfo.setField("xxx");
        config.getForwardAuthInfo().add(authInfo);
        // Do *not* set street address

        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain).doApply(apiRequest);

        Assert.assertEquals(null, apiRequest.getHeaders().get("X-TEST"));
    }

    @Test
    public void shouldBeNullForUnsetStandardClaimLookup()  throws CertificateEncodingException, IOException {
        ForwardAuthInfo authInfo = new ForwardAuthInfo();
        authInfo.setHeaders("X-TEST");
        authInfo.setField("email");
        config.getForwardAuthInfo().add(authInfo);
        // Do *not* set street address

        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain).doApply(apiRequest);

        Assert.assertEquals(null, apiRequest.getHeaders().get("X-TEST"));
    }

    @Test
    public void shouldForwardAuthInfoSubject() throws CertificateEncodingException, IOException {
        ForwardAuthInfo authInfo = new ForwardAuthInfo();
        authInfo.setHeaders("X-TEST");
        authInfo.setField("email");
        config.getForwardAuthInfo().add(authInfo);

        token.setEmail("apiman@apiman.io");
        String encoded = generateAndSerializeToken();
        apiRequest.getHeaders().put("Authorization", "Bearer " + encoded);
        keycloakOauthPolicy.apply(apiRequest, mContext, config, mChain);

        verify(mChain).doApply(apiRequest);

        Assert.assertEquals("apiman@apiman.io", apiRequest.getHeaders().get("X-TEST"));
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
