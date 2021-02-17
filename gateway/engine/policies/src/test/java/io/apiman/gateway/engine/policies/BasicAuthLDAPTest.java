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

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.impl.DefaultLdapComponent;
import io.apiman.gateway.engine.policies.util.LdapTestMixin;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import net.sf.ehcache.CacheManager;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Fires up an ldap server to do a better job testing the basic auth
 * policy when configured for ldap.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls", "javadoc" })
@RunWith(FrameworkRunner.class)
@CreateLdapServer(transports = { @CreateTransport(protocol = "LDAP", port = 7654) })
public class BasicAuthLDAPTest extends AbstractLdapTestUnit implements LdapTestMixin {

    private static final String LDAP_SERVER = "localhost";
    private static final String PARTITION_NAME = "_ldap-partition";

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
        partition = initLdapTestSetup(
            PARTITION_NAME,
            new File("target"),
            ehCacheManager,
            getLdapServer().getDirectoryService()
        );
        injectLdifFiles(
            getLdapServer().getDirectoryService(),
            "io/apiman/gateway/engine/policies/users.ldif"
        );
        this.ldapComponent = new DefaultLdapComponent();
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
     * Test method for {@link io.apiman.gateway.engine.policies.BasicAuthenticationPolicy#apply(ApiRequest, IPolicyContext, Object, IPolicyChain)}
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

        doTest(json, null, null, PolicyFailureCodes.BASIC_AUTH_REQUIRED, ldapComponent);
        doTest(json, "admin", "invalid_password", PolicyFailureCodes.BASIC_AUTH_FAILED,
            ldapComponent);
        doTest(json, "admin", "secret", null, ldapComponent);

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
                "    \"url\" : \"ldap://localhost:7654\",\r\n" +
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
}
