package io.apiman.gateway.platforms.vertx3.components.ldap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import javax.naming.NamingException;
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
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.shared.DefaultDnFactory;
import org.apache.directory.server.i18n.I18n;

/**
 * Some common initialisation code for LDAP testing that can be mixed in without requiring inheritance
 *
 * @author Marc Savy @{literal<marc@rhymewithgravy.com>}
 */
public interface LdapTestMixin {

    /**
     * Initialise the LDAP server with basic test setup.
     */
    static JdbmPartition initLdapTestSetup(
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

    static void fixLNNFE(DirectoryService service) throws LdapException {
        Dn dn = new Dn("o=apiman");
        Entry entry = service.newEntry(dn);
        entry.add("objectClass", "top", "domain", "extensibleObject");
        entry.add("dc", "apiman");
        entry.add("cn", "o=apiman");
        service.getAdminSession().add(entry);
    }

    static void injectLdifFiles(DirectoryService service, String... ldifFiles) throws Exception {
        try { fixLNNFE(service); } catch (Exception llne) { }

        if (ldifFiles != null && ldifFiles.length > 0) {
            for (String ldifFile : ldifFiles) {
                InputStream is = null;
                try {
                    is = LdapTestMixin.class.getClassLoader().getResourceAsStream(ldifFile);
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

    static void injectEntry(LdifEntry entry, DirectoryService service) throws Exception {
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
