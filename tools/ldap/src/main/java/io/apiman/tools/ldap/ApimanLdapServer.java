/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.tools.ldap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A simple ldap server.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
@RunWith(FrameworkRunner.class)
@CreateLdapServer(transports = { @CreateTransport(protocol = "LDAP", port = 7654) })
public class ApimanLdapServer extends AbstractLdapTestUnit {

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
            injectLdifFiles("io/apiman/tools/ldap/users.ldif");
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void startLdapServer() throws Exception {
        DirContext ctx = createContext();
        Assert.assertNotNull(ctx);

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> result = ctx.search("o=apiman", "(ObjectClass=*)", controls);

        int count = 0;
        while (result.hasMore()) {
            result.next();
            count++;
        }

        String url = "ldap://" + LDAP_SERVER + ":" + ldapServer.getPort();
        System.out.println("======================================================");
        System.out.println("LDAP server started successfully.");
        System.out.println("");
        System.out.println("  URL: " + url);
        System.out.println("  Node Count: " + count);
        System.out.println("  Direct Bind DN: cn=${username},ou=developers,ou=people,o=apiman");
        System.out.println("======================================================");
        System.out.println("");
        System.out.println("");
        System.out.println("Press Enter to stop the LDAP server.");
        new BufferedReader(new InputStreamReader(System.in)).readLine();
        System.out.println("Shutting down the LDAP server...");
    }

    public static void injectLdifFiles(String... ldifFiles) throws Exception {
        if ((ldifFiles != null) && (ldifFiles.length > 0)) {
            for (String ldifFile : ldifFiles) {
                InputStream is = null;
                try {
                    is = ApimanLdapServer.class.getClassLoader().getResourceAsStream(ldifFile);
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
