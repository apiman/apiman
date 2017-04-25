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
package io.apiman.gateway.platforms.vertx3.components.jdbc;

import io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean;
import io.apiman.gateway.platforms.vertx3.components.jdbc.suitetests.CreateTableTest;
import io.apiman.gateway.platforms.vertx3.components.jdbc.suitetests.ExecuteTest;
import io.apiman.gateway.platforms.vertx3.components.jdbc.suitetests.NestedQueryTest;
import io.apiman.gateway.platforms.vertx3.components.jdbc.suitetests.QueryTest;

import java.sql.SQLException;
import java.util.TimeZone;

import org.h2.tools.Server;
import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Mini integration tests for Vert.x 3 implementation of JDBC components.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@RunWith(Suite.class)
@SuiteClasses({ CreateTableTest.class, ExecuteTest.class, QueryTest.class, NestedQueryTest.class })
@SuppressWarnings("nls")
public class JdbcTestSuite {
    private static JdbcOptionsBean options = new JdbcOptionsBean();
    private static final String JDBC_URL = String.format("jdbc:h2:tcp://localhost/%s/JdbcClientComponentTestDb",
            System.getProperty("java.io.tmpdir"));

    static {
        // Important, this should occur BEFORE H2 is loaded otherwise it'll use local TZ and break everything.
        System.err.println("Permanently attempting to set the TZ to UTC.");
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        DateTimeZone.setDefault(DateTimeZone.UTC);
        options.setJdbcUrl(JDBC_URL);
        options.setAutoCommit(true);
        options.setPoolName("JdbcClientComponentTestPool");
    }

    public static Server h2Server;

    /**
     * Slow to start & stop so do this as infrequently as possible.
     * @throws SQLException the SQL exception
     */
    @BeforeClass
    public static void setupH2() throws SQLException {
        h2Server = Server.createTcpServer().start();
    }

    @AfterClass
    public static void teardownH2() {
        h2Server.stop();
    }
}
