/*
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
package io.apiman.gateway.engine.policies;

import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.impl.DefaultLdapComponent;
import io.apiman.gateway.engine.policies.util.LdapTestMixin;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLSocketFactory;
import net.sf.ehcache.CacheManager;
import org.apache.commons.collections.ListUtils;
import org.apache.directory.api.ldap.model.constants.SupportedSaslMechanisms;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.annotations.SaslMechanism;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.security.TlsKeyGenerator;
import org.apache.directory.server.ldap.handlers.extended.StartTlsHandler;
import org.apache.directory.server.ldap.handlers.sasl.plain.PlainMechanismHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Fires up an ldaps server to do a better job testing the basic auth
 * policy when configured for ldaps.
 *
 * @author jhauray
 */
@Ignore
@SuppressWarnings({ "nls", "javadoc" })
@RunWith(FrameworkRunner.class)
@CreateLdapServer(
        transports = {
                @CreateTransport(protocol = "LDAPS", port = 10636, address = "localhost")
        },
        saslHost = "localhost",
        saslMechanisms = {
                @SaslMechanism(name = SupportedSaslMechanisms.PLAIN, implClass = PlainMechanismHandler.class),
        },
        extendedOpHandlers = {
                StartTlsHandler.class
        }
)
public class BasicAuthLDAPSTest extends AbstractLdapTestUnit implements LdapTestMixin {

    private static final String LDAP_SERVER = "localhost";
    private static final String LDAP_KEYSTORE =  "ldaps_server.jks";
    private static final String LDAP_KEYSTORE_PASSWD = "mustBeSecret";
    private static final String PARTITION_NAME = "_ldaps-partition";

    private static JdbmPartition partition;
    private CacheManager ehCacheManager;
    private DefaultLdapComponent ldapComponent;

    @After
    public void teardown() {
        ehCacheManager.clearAll();
    }

    @Before
    public void setUp() throws Exception {
        ehCacheManager = CacheManager.newInstance();
        File targetDir = new File("target");
        partition = initLdapTestSetup(
            PARTITION_NAME,
            targetDir,
            ehCacheManager,
            getLdapServer().getDirectoryService()
        );
        injectLdifFiles(
            getLdapServer().getDirectoryService(),
            "io/apiman/gateway/engine/policies/users.ldif"
        );

        Path keyStorePath = Paths.get(targetDir.getAbsolutePath(), LDAP_KEYSTORE).toAbsolutePath();
        // Init (delete if exist) key store for ldaps server
        Files.deleteIfExists(keyStorePath);

        // Get LDAP Root entry
        Entry entry = service.getAdminSession().lookup(partition.getSuffixDn());

        // Add private KeyPair and a self-signed certificate corresponding to the entry.
        // Importantly, we generate a large key to ensure that we aren't hit by security policy limitations that
        // have banned RSA keys with a short length (the default of the underlying library, currently, if you
        // call TlsKeyGenerator.addKeyPair#entry(Entry) without additional arguments)
        String hostName = InetAddress.getLocalHost().getHostName();
        TlsKeyGenerator.addKeyPair(entry, "CN=" + hostName + ", OU=Directory, O=Apiman, C=World",
             "CN=" + hostName + ", OU=Directory, O=ASF, C=World",
             "RSA", 4096);
        KeyPair keyPair = TlsKeyGenerator.getKeyPair(entry);
        X509Certificate cert = TlsKeyGenerator.getCertificate(entry);

        // Add generated certificate to the keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("apacheds", cert);
        keyStore.setKeyEntry("apacheds", keyPair.getPrivate(),
             LDAP_KEYSTORE_PASSWD.toCharArray(),
             new Certificate[]{cert});
        keyStore.store(new FileOutputStream(keyStorePath.toFile()), LDAP_KEYSTORE_PASSWD.toCharArray());

        if (Files.notExists(keyStorePath)) {
            throw new IOException(String.format("Keystore [%s] doesn't exist.", keyStorePath));
        }

        this.ldapComponent = new DefaultLdapComponent();
        overrideDefaultTrustStore(keyStorePath, ldapComponent);
    }

    /**
     * Override the default trust store by generating a new trust manager with our store in it (rather than
     * defaults, and/or those loaded at startup via environment variables).
     */
    private static void overrideDefaultTrustStore(Path keyStorePath,
        DefaultLdapComponent ldapComponent) throws Exception {
        SSLUtil.setEnabledSSLProtocols(
            new ArrayList<String>(){{
                add("TLSv1.3");
                add("TLSv1.2");
                add("TLSv1.1");
                add("TLSv1");
                add("SSLv3");
            }}
        );
        // Load the generated test keystore as Apiman's current TrustStore
        SSLUtil sslUtil = new SSLUtil(new TrustStoreTrustManager(
            keyStorePath.toFile(), LDAP_KEYSTORE_PASSWD.toCharArray(), "JKS", false
        ));
        SSLSocketFactory testSocketFactory = sslUtil.createSSLSocketFactory();
        ldapComponent.setSocketFactory(testSocketFactory);

        // Load generated Keystore in ldaps server
        ldapServer.setKeystoreFile(keyStorePath.toString());
        ldapServer.setCertificatePassword(LDAP_KEYSTORE_PASSWD);
        ldapServer.reloadSslContext();
    }

    /**
     * Test method for {@link BasicAuthenticationPolicy#apply(ApiRequest, IPolicyContext, Object, IPolicyChain)}
     */
    @Test
    public void testApply() throws Exception {
        // Test using a direct bind to the user account
        //////////////////////////////////////////////////
        String json = "{\r\n" +
            "    \"realm\" : \"TestRealm\",\r\n" +
            "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
            "    \"ldapIdentity\" : {\r\n" +
            "    \"url\" : \"ldaps://" + LDAP_SERVER + ":" + ldapServer.getTransports()[0].getPort() + "\",\r\n" +
            "    \"dnPattern\" : \"uid=${username},ou=system\"\r\n" +
            "    }\r\n" +
            "}";
        doTest(json, null, null, PolicyFailureCodes.BASIC_AUTH_REQUIRED, ldapComponent);
        doTest(json, "admin", "invalid_password", PolicyFailureCodes.BASIC_AUTH_FAILED,
            ldapComponent);
        doTest(json, "admin", "secret", null, ldapComponent);

        // Test using a service account with user search
        //////////////////////////////////////////////////
        json = "{\r\n" +
            "  \"realm\" : \"TestRealm\",\r\n" +
            "  \"ldapIdentity\" : {\r\n" +
            "    \"url\" : \"ldaps://" + LDAP_SERVER + ":" + ldapServer.getTransports()[0].getPort() + "\",\r\n" +
            "    \"dnPattern\" : \"uid=${username},ou=system\",\r\n" +
            "    \"bindAs\" : \"ServiceAccount\",\r\n" +
            "    \"credentials\" : {\r\n" +
            "      \"username\" : \"admin\",\r\n" +
            "      \"password\" : \"secret\"\r\n" +
            "    },\r\n" +
            "    \"userSearch\" : {\r\n" +
            "      \"baseDn\" : \"ou=people,o=apiman\",\r\n" +
            "      \"expression\" : \"(uid=${username})\"\r\n" +
            "    }\r\n" +
            "  }\r\n" +
            "}";

        doTest(json, null, null, PolicyFailureCodes.BASIC_AUTH_REQUIRED, ldapComponent);
        doTest(json, "ewittman", "invalid_password", PolicyFailureCodes.BASIC_AUTH_FAILED,
            ldapComponent);
        doTest(json, "unknown_user", "password", PolicyFailureCodes.BASIC_AUTH_FAILED, ldapComponent);
        doTest(json, "ewittman", "ewittman", null, ldapComponent);

        // Test using a service account with user search
        //////////////////////////////////////////////////
        json = "{\r\n" +
            "  \"realm\" : \"TestRealm\",\r\n" +
            "  \"ldapIdentity\" : {\r\n" +
            "    \"url\" : \"ldaps://" + LDAP_SERVER + ":" + ldapServer.getTransports()[0].getPort() + "\",\r\n" +
            "    \"dnPattern\" : \"uid=${username},ou=system\",\r\n" +
            "    \"bindAs\" : \"ServiceAccount\",\r\n" +
            "    \"credentials\" : {\r\n" +
            "      \"username\" : \"admin\",\r\n" +
            "      \"password\" : \"secret\"\r\n" +
            "    },\r\n" +
            "    \"userSearch\" : {\r\n" +
            "      \"baseDn\" : \"ou=people,o=apiman\",\r\n" +
            "      \"expression\" : \"(uid=${username})\"\r\n" +
            "    }\r\n" +
            "  }\r\n" +
            "}";
        doTest(json, null, null, PolicyFailureCodes.BASIC_AUTH_REQUIRED, ldapComponent);
        doTest(json, "ewittman", "invalid_password", PolicyFailureCodes.BASIC_AUTH_FAILED,
            ldapComponent);
        doTest(json, "unknown_user", "password", PolicyFailureCodes.BASIC_AUTH_FAILED, ldapComponent);
        doTest(json, "ewittman", "ewittman", null, ldapComponent);

        // Test with the extraction of user roles
        //////////////////////////////////////////////////
        json = "{\r\n" +
            "  \"realm\" : \"TestRealm\",\r\n" +
            "  \"ldapIdentity\" : {\r\n" +
            "    \"url\" : \"ldaps://" + LDAP_SERVER + ":" + ldapServer.getTransports()[0].getPort() + "\",\r\n" +
            "    \"dnPattern\" : \"uid=${username},ou=system\",\r\n" +
            "    \"bindAs\" : \"ServiceAccount\",\r\n" +
            "    \"credentials\" : {\r\n" +
            "      \"username\" : \"admin\",\r\n" +
            "      \"password\" : \"secret\"\r\n" +
            "    },\r\n" +
            "    \"userSearch\" : {\r\n" +
            "      \"baseDn\" : \"ou=people,o=apiman\",\r\n" +
            "      \"expression\" : \"(uid=${username})\"\r\n" +
            "    },\r\n" +
            "    \"extractRoles\" : true,\r\n" +
            "    \"membershipAttribute\" : \"title\",\r\n" +
            "    \"rolenameAttribute\" : \"cn\"\r\n" +
            "  }\r\n" +
            "}";
        Set<String> expectedRoles = new HashSet<>();
        expectedRoles.add("user");
        expectedRoles.add("admin");
        doTest(json, "ewittman", "ewittman", null, expectedRoles, ldapComponent);
        doTest(json, "ewittman", "ewittmanx", PolicyFailureCodes.BASIC_AUTH_FAILED, expectedRoles,
            ldapComponent);
    }
}
