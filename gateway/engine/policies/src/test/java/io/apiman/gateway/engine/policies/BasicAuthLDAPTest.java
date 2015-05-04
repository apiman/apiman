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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.shared.ldap.entry.DefaultServerEntry;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.DN;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Fires up an ldap server to do a better job testing the basic auth
 * policy when configured for ldap.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls", "javadoc" })
@RunWith(FrameworkRunner.class)
@CreateLdapServer(transports = { @CreateTransport(protocol = "LDAP", port = 7654) })
public class BasicAuthLDAPTest extends AbstractLdapTestUnit {

    private static final String LDAP_SERVER = "localhost";

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
        File partitionDir = new File(targetDir, "_ldap-partition");
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
            entry.add("cn", "o=apiman");
            service.getAdminSession().add(entry);
        }

        try {
            injectLdifFiles("io/apiman/gateway/engine/policies/users.ldif");
        } catch (Exception e) {
            throw e;
        }
    }

    // This will help determine whether all the right data is present in ldap
    @Test @Ignore
    public void testLdap() throws Exception {
        DirContext ctx = createContext();
        Assert.assertNotNull(ctx);

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> result = ctx.search("o=apiman", "(ObjectClass=*)", controls);

        System.out.println(" ==== Search Results ====");
        while (result.hasMore()) {
            SearchResult entry = result.next();
            System.out.println(" ===> " + entry.getName());
        }

    }

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.BasicAuthenticationPolicy#apply(ServiceRequest, IPolicyContext, Object, IPolicyChain)}.
     */
    @Test
    public void testApply() throws Exception {
        // Test using a direct bind to the user account
        //////////////////////////////////////////////////
        String json = "{\r\n" +
                "    \"realm\" : \"TestRealm\",\r\n" +
                "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
                "    \"ldapIdentity\" : {\r\n" +
                "        \"url\" : \"ldap://" + LDAP_SERVER + ":" + ldapServer.getPort() + "\",\r\n" +
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
                "    \"url\" : \"ldap://" + LDAP_SERVER + ":" + ldapServer.getPort() + "\",\r\n" +
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
    }

    // pass null if you expect success
    private void doTest(String json, String username, String password, Integer expectedFailureCode) throws Exception {
        BasicAuthenticationPolicy policy = new BasicAuthenticationPolicy();
        Object config = policy.parseConfiguration(json);
        ServiceRequest request = new ServiceRequest();
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
        IPolicyChain<ServiceRequest> chain = Mockito.mock(IPolicyChain.class);

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

    private DirContext createContext() throws NamingException {
        // Create a environment container
        Hashtable<Object, Object> env = new Hashtable<>();

        String url = "ldap://" + LDAP_SERVER + ":" + ldapServer.getPort();

        // Create a new context pointing to the partition
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put(Context.SECURITY_CREDENTIALS, "secret");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        // Let's open a connection on this partition
        InitialContext initialContext = new InitialContext(env);

        // We should be able to read it
        DirContext appRoot = (DirContext) initialContext.lookup("");
        Assert.assertNotNull(appRoot);

        return appRoot;
    }

    public static void injectLdifFiles(String... ldifFiles) throws Exception {
        if ((ldifFiles != null) && (ldifFiles.length > 0)) {
            for (String ldifFile : ldifFiles) {
                InputStream is = BasicAuthLDAPTest.class.getClassLoader().getResourceAsStream(ldifFile);
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
