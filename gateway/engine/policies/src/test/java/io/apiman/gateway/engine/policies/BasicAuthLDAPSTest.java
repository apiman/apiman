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

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.ILdapComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultLdapComponent;
import io.apiman.gateway.engine.policies.config.BasicAuthenticationConfig;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.annotations.SaslMechanism;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.security.TlsKeyGenerator;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.ldap.handlers.bind.plain.PlainMechanismHandler;
import org.apache.directory.server.ldap.handlers.extended.StartTlsHandler;
import org.apache.directory.shared.ldap.constants.SupportedSaslMechanisms;
import org.apache.directory.shared.ldap.entry.DefaultServerEntry;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.DN;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.naming.NamingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

/**
 * Fires up an ldaps server to do a better job testing the basic auth
 * policy when configured for ldaps.
 *
 * @author jhauray
 */
@SuppressWarnings({ "nls", "javadoc" })
@RunWith(FrameworkRunner.class)
@CreateLdapServer(
        transports = {
                @CreateTransport(protocol = "LDAPS", port = 10636)
        },
        saslHost = "localhost",
        saslMechanisms =
                {
                        @SaslMechanism(name = SupportedSaslMechanisms.PLAIN, implClass = PlainMechanismHandler.class),
                },
        extendedOpHandlers =
                { StartTlsHandler.class }
)
public class BasicAuthLDAPSTest extends AbstractLdapTestUnit {

    private static final String LDAP_SERVER = "localhost";
    private static final String LDAP_KEYSTORE =  "ldaps_server.jks";
    private static final String LDAP_KEYSTORE_PASSWD ="mustBeSecret";

    private static JdbmPartition partition;

    @Before
    public void setUp() throws Exception {

        if (partition != null) {
            return;
        }
        File targetDir = new File("target");
        if (!targetDir.isDirectory()) {
            throw new Exception("Couldn't find maven target directory: " + targetDir);
        }
        File partitionDir = new File(targetDir, "_ldaps-partition");
        if (partitionDir.exists()) {
            FileUtils.deleteDirectory(partitionDir);
        }
        partitionDir.mkdirs();

        final File partitionDirectory = partitionDir;
        partition = new JdbmPartition();
        partition.setId("apiman");
        partition.setPartitionDir(partitionDirectory);
        partition.setSchemaManager(service.getSchemaManager());
        partition.setSuffix("o=apiman");
        service.addPartition(partition);

        // Inject the foo root entry if it does not already exist
        try {
            service.getAdminSession().lookup(partition.getSuffixDn());
        } catch (Exception lnnfe) {
            DN dn = new DN("o=apiman");
            ServerEntry entry = service.newEntry(dn);
            entry.add("objectClass", "top", "domain", "extensibleObject");
            entry.add("dc", "apiman");
            entry.add("cn", "apiman");
            entry.add("o", "apiman");
            service.getAdminSession().add(entry);
        }

        try {
            injectLdifFiles("io/apiman/gateway/engine/policies/users.ldif");
        } catch (Exception e) {
            throw e;
        }

        //Init (delete if exist) key store for ldaps server
        File goodKeyStoreFile = new File(targetDir, LDAP_KEYSTORE);
        if ( goodKeyStoreFile.exists() ){goodKeyStoreFile.delete();}

        //Get LDAP Root entry
        ServerEntry entry = service.getAdminSession().lookup(partition.getSuffixDn());

        //Add private KeyPair and a self-signed certificate corresponding to the entry
        TlsKeyGenerator.addKeyPair( entry );
        KeyPair keyPair = TlsKeyGenerator.getKeyPair( entry );
        X509Certificate cert = TlsKeyGenerator.getCertificate( entry );

        //Add generated certificate to the keystore
        KeyStore goodKeyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
        goodKeyStore.load( null, null );
        goodKeyStore.setCertificateEntry( "apacheds", cert );
        goodKeyStore.setKeyEntry( "apacheds", keyPair.getPrivate(),
                LDAP_KEYSTORE_PASSWD.toCharArray(),
                new Certificate[]{ cert } );
        goodKeyStore.store( new FileOutputStream( goodKeyStoreFile ), LDAP_KEYSTORE_PASSWD.toCharArray() );

        //Load generated Keystore in ldaps server
        ldapServer.setKeystoreFile(goodKeyStoreFile.getAbsolutePath());
        ldapServer.setCertificatePassword(LDAP_KEYSTORE_PASSWD);
        ldapServer.reloadSslContext();

        //Load Keystore as Apiman current TrustStore
        System.setProperty("javax.net.ssl.trustStore", goodKeyStoreFile.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", LDAP_KEYSTORE_PASSWD);

    }

    /**
     * Test method for {@link BasicAuthenticationPolicy#apply(ServiceRequest, IPolicyContext, Object, IPolicyChain)}.
     */
    @Test
    public void testApply() throws Exception {
        // Test using a direct bind to the user account
        //////////////////////////////////////////////////
        String json = "{\r\n" +
                "    \"realm\" : \"TestRealm\",\r\n" +
                "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
                "    \"ldapIdentity\" : {\r\n" +
                "        \"url\" : \"ldaps://" + LDAP_SERVER + ":" + ldapServer.getTransports()[0].getPort() + "\",\r\n" +
                "        \"dnPattern\" : \"uid=${username},ou=system\"\r\n" +
                "    }\r\n" +
                "}";

        doTest(json, null, null, PolicyFailureCodes.BASIC_AUTH_REQUIRED);
        doTest(json, "admin", "invalid_password", PolicyFailureCodes.BASIC_AUTH_FAILED);
        doTest(json, "admin", "secret", null);

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

        doTest(json, null, null, PolicyFailureCodes.BASIC_AUTH_REQUIRED);
        doTest(json, "ewittman", "invalid_password", PolicyFailureCodes.BASIC_AUTH_FAILED);
        doTest(json, "unknown_user", "password", PolicyFailureCodes.BASIC_AUTH_FAILED);
        doTest(json, "ewittman", "ewittman", null);

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
        doTest(json, null, null, PolicyFailureCodes.BASIC_AUTH_REQUIRED);
        doTest(json, "ewittman", "invalid_password", PolicyFailureCodes.BASIC_AUTH_FAILED);
        doTest(json, "unknown_user", "password", PolicyFailureCodes.BASIC_AUTH_FAILED);
        doTest(json, "ewittman", "ewittman", null);

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
        doTest(json, "ewittman", "ewittman", null, expectedRoles);
        doTest(json, "ewittman", "ewittmanx", PolicyFailureCodes.BASIC_AUTH_FAILED, expectedRoles);
    }

    private void doTest(String json, String username, String password, Integer expectedFailureCode) throws Exception {
        doTest(json, username, password, expectedFailureCode, null);
    }

    // pass null if you expect success
    private void doTest(String json, String username, String password, Integer expectedFailureCode,
                        Set<String> expectedRoles) throws Exception {
        BasicAuthenticationPolicy policy = new BasicAuthenticationPolicy();
        BasicAuthenticationConfig config = policy.parseConfiguration(json);
        ApiRequest request = new ApiRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(new IPolicyFailureFactoryComponent() {
            @Override
            public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                failure.setType(type);
                failure.setFailureCode(failureCode);
                failure.setMessage(message);
                return failure;
            }
        });

        // The LDAP stuff we're testing!
        Mockito.when(context.getComponent(ILdapComponent.class)).thenReturn(new DefaultLdapComponent());

        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);

        if (username != null) {
            request.getHeaders().put("Authorization", createBasicAuthorization(username, password));
        }

        if (expectedFailureCode == null) {
            policy.apply(request, context, config, chain);
            Mockito.verify(chain).doApply(request);
        } else {
            policy.apply(request, context, config, chain);
            Mockito.verify(chain).doFailure(failure);
            Assert.assertEquals(expectedFailureCode.intValue(), failure.getFailureCode());
        }

        if (expectedRoles != null && expectedFailureCode == null) {
            Mockito.verify(context).setAttribute(AuthorizationPolicy.AUTHENTICATED_USER_ROLES, expectedRoles);
        }
    }

    /**
     * Creates the http Authorization string for the given credentials.
     * @param username
     * @param password
     */
    private String createBasicAuthorization(String username, String password) {
        String creds = username + ":" + password;
        StringBuilder builder = new StringBuilder();
        builder.append("Basic ");
        builder.append(Base64.encodeBase64String(creds.getBytes()));
        return builder.toString();
    }


    public static void injectLdifFiles(String... ldifFiles) throws Exception {
        if (ldifFiles != null && ldifFiles.length > 0) {
            for (String ldifFile : ldifFiles) {
                InputStream is = null;
                try {
                    is = BasicAuthLDAPSTest.class.getClassLoader().getResourceAsStream(ldifFile);
                    if (is == null) {
                        throw new FileNotFoundException("LDIF file '" + ldifFile + "' not found.");
                    } else {
                        try {
                            LdifReader ldifReader = new LdifReader(is);
                            for (LdifEntry entry : ldifReader) {
                                injectEntry(entry);
                            }
                            ldifReader.close();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
    }

    private static void injectEntry(LdifEntry entry) throws Exception {
        if (entry.isChangeAdd()) {
            service.getAdminSession().add(
                    new DefaultServerEntry(service.getSchemaManager(), entry.getEntry()));
        } else if (entry.isChangeModify()) {
            service.getAdminSession().modify(entry.getDn(), entry.getModificationItems());
        } else {
            String message = I18n.err(I18n.ERR_117, entry.getChangeType());
            throw new NamingException(message);
        }
    }

}
