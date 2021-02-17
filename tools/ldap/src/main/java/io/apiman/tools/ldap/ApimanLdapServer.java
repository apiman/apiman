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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.shared.DefaultDnFactory;
import org.apache.directory.server.i18n.I18n;
import org.junit.After;
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
    private CacheManager ehCacheManager;

    @After
    public void stop() {
        ehCacheManager.clearAll();
    }

    @Before
    public void start() throws Exception {
        ehCacheManager = CacheManager.newInstance();
        initLdapTestSetup(
            "apiman_LDAP_server",
            new File("target"),
            ehCacheManager,
            getLdapServer().getDirectoryService()
        );
        injectLdifFiles(
            getLdapServer().getDirectoryService(),
            "io/apiman/tools/ldap/users.ldif"
        );
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

    // TODO(msavy): Consider extracting common code into a shared LDAP test dep or similar. See LdapTestMixin.
    private JdbmPartition initLdapTestSetup(
        String partitionName,
        File targetDir,
        CacheManager ehCacheManager,
        DirectoryService service
    ) throws IOException, LdapException {

        JdbmPartition partition;
        if (!targetDir.isDirectory()) {
            throw new UncheckedIOException(
                new IOException("Couldn't find maven target directory: " + targetDir));
        }
        File partitionDir = new File(targetDir, partitionName);
        if (partitionDir.exists()) {
            FileUtils.deleteDirectory(partitionDir);
        }
        partitionDir.mkdirs();

        // Requires EHCache! Reuse any cache with existing name.
        String ehCacheName = "apiman-" + partitionName + "-ehcache-testing";
        if (!ehCacheManager.cacheExists(ehCacheName)) {
            ehCacheManager.addCache(ehCacheName);
            ehCacheManager.clearAll();
        }
        Cache cache = ehCacheManager.getCache(ehCacheName);

        final SchemaManager schemaManager = new DefaultSchemaManager();
        final DnFactory defaultDnFactory = new DefaultDnFactory(schemaManager, cache);

        partition = new JdbmPartition(schemaManager, defaultDnFactory);
        partition.setId("apiman");
        partition.setPartitionPath(partitionDir.toURI());
        partition.setSchemaManager(service.getSchemaManager());
        partition.setSuffixDn(new Dn("o=apiman"));

        // The test framework may leave the partition there (albeit empty). Find and reuse, else create.
        boolean partitionAlreadyExists = false;
        for (Partition servicePartition : service.getPartitions()) {
            if (servicePartition.getId().equals("apiman")) {
                partition = (JdbmPartition) servicePartition;
                partitionAlreadyExists = true;
            }
        }
        if (!partitionAlreadyExists) {
            service.addPartition(partition);
        }

        // Inject the foo root entry if it does not already exist
        // Between runs, this may be empty.
        try {
            service.getAdminSession().lookup(partition.getSuffixDn());
        } catch (Exception lnnfe) {
            fixLNNFE(service);
        }

        return partition;
    }

    private void fixLNNFE(DirectoryService service) throws LdapException {
        Dn dn = new Dn("o=apiman");
        Entry entry = service.newEntry(dn);
        entry.add("objectClass", "top", "domain", "extensibleObject");
        entry.add("dc", "apiman");
        entry.add("cn", "o=apiman");
        service.getAdminSession().add(entry);
    }

    private void injectLdifFiles(DirectoryService service, String... ldifFiles) throws Exception {
        try { fixLNNFE(service); } catch (Exception llne) { }

        if (ldifFiles != null && ldifFiles.length > 0) {
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
                                injectEntry(entry, service);
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

    private void injectEntry(LdifEntry entry, DirectoryService service) throws Exception {
        if (entry.isChangeAdd()) {
            service.getAdminSession().add(
                new DefaultEntry(service.getSchemaManager(), entry.getEntry()));
        } else if (entry.isChangeModify()) {
            service.getAdminSession().modify(entry.getDn(), entry.getModifications());
        } else {
            String message = I18n.err(I18n.ERR_117, entry.getChangeType());
            throw new NamingException(message);
        }
    }


}
