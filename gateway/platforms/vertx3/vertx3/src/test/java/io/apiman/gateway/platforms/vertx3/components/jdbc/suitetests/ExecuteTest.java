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
package io.apiman.gateway.platforms.vertx3.components.jdbc.suitetests;

import static io.apiman.gateway.platforms.vertx3.components.jdbc.SpecHelpers.createTable;
import static io.apiman.gateway.platforms.vertx3.components.jdbc.SpecHelpers.explodeOnFailure;
import static io.apiman.gateway.platforms.vertx3.components.jdbc.SpecHelpers.resetDB;

import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
import io.apiman.gateway.engine.components.jdbc.IJdbcComponent;
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean;
import io.apiman.gateway.platforms.vertx3.components.JdbcClientComponentImpl;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@RunWith(VertxUnitRunner.class)
@SuppressWarnings("nls")
public class ExecuteTest {
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    public static JdbcOptionsBean options = new JdbcOptionsBean();
    public static final String JDBC_URL = String.format("jdbc:h2:tcp://localhost/%s/JdbcClientComponentTestDb",
            System.getProperty("java.io.tmpdir"));

    static {
        options.setJdbcUrl(JDBC_URL);
        options.setAutoCommit(true);
        options.setPoolName("JdbcClientComponentTestPool");
    }

    @Before
    public void setupTable(TestContext context) {
        createTable(context, options, rule.vertx());
    }

    @After
    public void resetDb(TestContext context)  {
        resetDB(context, options, rule.vertx());
    }

    @Test
    public void shouldInsertRecords(TestContext context) {
        Async async = context.async();

        IJdbcComponent component = new JdbcClientComponentImpl(rule.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected here!");
                IJdbcConnection connection = connectionResult;
                connection.execute("insert into APIMAN (PLACE_ID, COUNTRY, CITY, FOUNDING)\n" +
                        "     VALUES  (1, 'Seychelles', 'Victoria', '1976-06-29 00:00:00'), " + // June 29, 1976
                        "             (2, 'United States', 'Newtown', '1788-01-09 00:00:00')," + // January 9, 1788
                        "             (3, 'United States', 'Miami', '1896-07-28 00:00:00');", // July 28, 1896
                        explodeOnFailure(context, async, onSuccess -> { async.complete(); }));
        }));
    }

    @Test
    public void shouldDeleteTable(TestContext context) {
        Async async = context.async();
        Async async2 = context.async();

        IJdbcComponent component = new JdbcClientComponentImpl(rule.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected here!");
                IJdbcConnection connection = connectionResult;
                connection.execute("DROP TABLE APIMAN;", explodeOnFailure(context, async, onSuccess -> {
                    async.complete();
                }));

                connection.query("SHOW TABLES", explodeOnFailure(context, async, result -> {
                    context.assertEquals(0, result.getRowSize());
                    async2.complete();
                }));
        }));
    }
}
