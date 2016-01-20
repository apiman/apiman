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

import static io.apiman.gateway.platforms.vertx3.components.jdbc.SpecHelpers.explodeOnFailure;
import static io.apiman.gateway.platforms.vertx3.components.jdbc.SpecHelpers.resetDB;

import io.apiman.gateway.engine.components.IJdbcComponent;
import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
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
public class QueryTest {
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
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(Vertx.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected!");
                IJdbcConnection connection = connectionResult;
                String createSql = "create table APIMAN\n" +
                        "    (ID integer NOT NULL,\n" +
                        "    STRING varchar(40) NOT NULL,\n" +
                        "    SHORT smallint NOT NULL,\n" +
                        "    INTEGER integer NOT NULL,\n" +
                        "    LONG bigint NOT NULL,\n" +
                        "    DOUBLE double NOT NULL,\n " +
                        "    BOOLEAN boolean NOT NULL,\n" +
                        "    DATETIME datetime NOT NULL,\n" +
                        "    BYTEARRAY varbinary(100) NOT NULL,\n" +
                        "    PRIMARY KEY (ID));";
                String insertSql = "insert into APIMAN (ID, STRING, SHORT, INTEGER, LONG, DOUBLE, BOOLEAN, DATETIME, BYTEARRAY)\n" +
                        "     VALUES  (1, 'Voltigeur', 11, 9001009, 101010101, 3.14159, true, '1976-06-29 00:00:00', 31), ";
                connection.execute(
                        explodeOnFailure(context, async, onSuccess1 -> {
                            connection.execute(
                                    explodeOnFailure(context, async, onSuccess2 -> { async.complete(); }),
                                    insertSql);
                        }),
                        createSql);
        }));
    }

    @Test
    public void shouldQueryRecords(TestContext context) {
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(rule.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected!");
                IJdbcConnection connection = connectionResult;
                String selectSql = "SELECT * FROM APIMAN;";
                connection.query(
                        explodeOnFailure(context, async, queryResult -> {
                                context.assertEquals(9, queryResult.getNumColumns());

                                queryResult.next();
                                // Assert results
                                context.assertEquals(1, queryResult.getInteger(0));
                                context.assertEquals("Voltigeur", queryResult.getString(1));
                                context.assertEquals((short) 11, queryResult.getShort(2));
                                context.assertEquals(9001009, queryResult.getInteger(3));
                                context.assertEquals((long) 101010101, queryResult.getLong(4));
                                context.assertEquals(3.14159, queryResult.getDouble(5));
                                context.assertEquals(true, queryResult.getBoolean(6));
                                context.assertEquals(new DateTime("1976-06-29T00:00:00.000"), queryResult.getDateTime(7));
                                context.assertEquals(((byte) 31), queryResult.getBytes(8)[3]);

                                async.complete();
                            }),
                        selectSql);
        }));
    }

}
