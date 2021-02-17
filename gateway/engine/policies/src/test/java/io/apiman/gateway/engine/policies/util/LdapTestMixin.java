package io.apiman.gateway.engine.policies.util;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.ILdapComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.AuthorizationPolicy;
import io.apiman.gateway.engine.policies.BasicAuthLDAPTest;
import io.apiman.gateway.engine.policies.BasicAuthenticationPolicy;
import io.apiman.gateway.engine.policies.config.BasicAuthenticationConfig;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Set;
import javax.naming.NamingException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.codec.binary.Base64;
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
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.shared.DefaultDnFactory;
import org.apache.directory.server.i18n.I18n;
import org.junit.Assert;
import org.mockito.Mockito;

/**
 * Some common initialisation code for LDAP testing that can be mixed in without requiring inheritance
 *
 * @author Marc Savy @{literal<marc@rhymewithgravy.com>}
 */
public interface LdapTestMixin {

    /**
     * Initialise the LDAP server with basic test setup.
     */
    default JdbmPartition initLdapTestSetup(
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

        // Requires EHCache!
        String ehCacheName = "apiman-" + partitionName + "-ehcache-testing";
        ehCacheManager.addCache(ehCacheName);
        Cache cache = ehCacheManager.getCache(ehCacheName);

        final SchemaManager schemaManager = new DefaultSchemaManager();
        final DnFactory defaultDnFactory = new DefaultDnFactory(schemaManager, cache);

        partition = new JdbmPartition(schemaManager, defaultDnFactory);
        partition.setId("apiman");
        partition.setPartitionPath(partitionDir.toURI());
        partition.setSchemaManager(service.getSchemaManager());
        partition.setSuffixDn(new Dn("o=apiman"));
        service.addPartition(partition);

        // Inject the foo root entry if it does not already exist
        try {
            service.getAdminSession().lookup(partition.getSuffixDn());
        } catch (Exception lnnfe) {
            Dn dn = new Dn("o=apiman");
            Entry entry = service.newEntry(dn);
            entry.add("objectClass", "top", "domain", "extensibleObject");
            entry.add("dc", "apiman");
            entry.add("cn", "o=apiman");
            service.getAdminSession().add(entry);
        }

        return partition;
    }

    default void injectLdifFiles(DirectoryService service, String... ldifFiles) throws Exception {
        if (ldifFiles != null && ldifFiles.length > 0) {
            for (String ldifFile : ldifFiles) {
                InputStream is = null;
                try {
                    is = BasicAuthLDAPTest.class.getClassLoader().getResourceAsStream(ldifFile);
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

    default void injectEntry(LdifEntry entry, DirectoryService service) throws Exception {
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

    /**
     * Creates the http Authorization string for the given credentials.
     *
     * @param username
     * @param password
     */
    default String createBasicAuthorization(String username, String password) {
        String creds = username + ":" + password;
        StringBuilder builder = new StringBuilder();
        builder.append("Basic ");
        builder.append(Base64.encodeBase64String(creds.getBytes()));
        return builder.toString();
    }

    default void doTest(String json, String username, String password, Integer expectedFailureCode,
        ILdapComponent ldapComponentUnderTest)
         throws Exception {
        doTest(json, username, password, expectedFailureCode, null, ldapComponentUnderTest);
    }

    // pass null if you expect success
    default void doTest(String json,
        String username,
        String password,
        Integer expectedFailureCode,
        Set<String> expectedRoles,
        ILdapComponent ldapComponentUnderTest
    ) {
        BasicAuthenticationPolicy policy = new BasicAuthenticationPolicy();
        BasicAuthenticationConfig config = policy.parseConfiguration(json);
        ApiRequest request = new ApiRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class))
             .thenReturn((PolicyFailureType type, int failureCode, String message) -> {
                 failure.setType(type);
                 failure.setFailureCode(failureCode);
                 failure.setMessage(message);
                 return failure;
             });

        // The LDAP stuff we're testing!
        Mockito.when(context.getComponent(ILdapComponent.class)).thenReturn(ldapComponentUnderTest);

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
}
