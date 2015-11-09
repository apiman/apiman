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

import static io.apiman.gateway.platforms.vertx3.components.jdbc.SpecHelpers.createTableAndPopulateData;
import static io.apiman.gateway.platforms.vertx3.components.jdbc.SpecHelpers.explodeOnFailure;
import static io.apiman.gateway.platforms.vertx3.components.jdbc.SpecHelpers.resetDB;

import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
import io.apiman.gateway.engine.components.jdbc.IJdbcComponent;
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean;
import io.apiman.gateway.platforms.vertx3.components.JdbcClientComponentImpl;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@RunWith(VertxUnitRunner.class)
@SuppressWarnings("nls")
public class NestedQueryTest {
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

    @AfterClass
    public static void resetDb(TestContext context)  {
       resetDB(context, options, Vertx.vertx());
    }

    @BeforeClass
    public static void setupTable(TestContext context) {
        createTableAndPopulateData(context, options, Vertx.vertx());
    }

    @Test
    public void shouldHandleMultipleExecuteAndQueryOperations(TestContext context) {
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(rule.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected!");
                IJdbcConnection connection = connectionResult;
                connection.execute("DELETE FROM APIMAN\n" +
                        "     WHERE CITY='Newtown'",
                        explodeOnFailure(context, async, onSuccess -> {

                            connection.query("SELECT * FROM APIMAN;",
                                    explodeOnFailure(context, async, queryResult -> {
                                            context.assertEquals(2, queryResult.getRowSize());
                                            context.assertEquals(4, queryResult.getColumnSize());

                                            queryResult.first();
                                            // Assert Seychelles
                                            context.assertEquals(1, queryResult.getInteger(0));
                                            context.assertEquals("Seychelles", queryResult.getString(1));
                                            context.assertEquals("Victoria", queryResult.getString(2));
                                            context.assertEquals(new DateTime("1976-06-29T00:00:00.000"), queryResult.getDateTime(3));

                                            queryResult.next();
                                            // Assert Miami, US
                                            context.assertEquals(3, queryResult.getInteger(0));
                                            context.assertEquals("United States", queryResult.getString(1));
                                            context.assertEquals("Miami", queryResult.getString(2));
                                            context.assertEquals(new DateTime("1896-07-28T00:00:00.000"), queryResult.getDateTime(3));

                                            async.complete();
                                    }));
                        }));
        }));
    }

}
