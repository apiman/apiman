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

package io.apiman.gateway.platforms.vertx3.components.ldap;

import io.apiman.gateway.engine.components.ILdapComponent;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;
import io.apiman.gateway.platforms.vertx3.components.LdapClientComponentImpl;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.RunTestOnContext;
import java.io.File;
import net.sf.ehcache.CacheManager;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.FrameworkRunner;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings({ "nls", "javadoc" })
@RunWith(FrameworkRunner.class)
@CreateLdapServer(transports = { @CreateTransport(protocol = "LDAP", port = 7654) })
public class LdapTestParent extends AbstractLdapTestUnit implements LdapTestMixin {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    public LdapConfigBean config;
    public static final String LDAP_SERVER_HOST = "localhost";
    private static final String PARTITION_NAME = "_apiman_ldap_test";

    ILdapComponent ldapClientComponent = new LdapClientComponentImpl(Vertx.vertx(), null, null);
    private CacheManager ehCacheManager;
    private JdbmPartition partition;

    @Before
    public void before() throws Exception {
        config = new LdapConfigBean();
        config.setHost(LDAP_SERVER_HOST);
        config.setPort(ldapServer.getPort());

        ehCacheManager = CacheManager.newInstance();
        File targetDir = new File("target");

        partition = LdapTestMixin.initLdapTestSetup(
             PARTITION_NAME,
             targetDir,
             ehCacheManager,
             getLdapServer().getDirectoryService()
        );

        LdapTestMixin.injectLdifFiles(
             getLdapServer().getDirectoryService(),
             "io/apiman/gateway/platforms/vertx3/users.ldif"
        );
    }
}
