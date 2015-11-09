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

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@RunWith(VertxUnitRunner.class)
@SuppressWarnings("nls")
public class CreateTableTest {
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

    @Test
    public void createTable(TestContext context) {
        Async async = context.async();

        IJdbcComponent component = new JdbcClientComponentImpl(rule.vertx(), null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected!");
                IJdbcConnection connection = connectionResult;
                connection.execute("create table APIMAN\n" +
                        "    (PLACE_ID integer NOT NULL,\n" +
                        "    COUNTRY varchar(40) NOT NULL,\n" +
                        "    CITY varchar(20) NOT NULL,\n" +
                        "    FOUNDING datetime NOT NULL,\n" +
                        "    PRIMARY KEY (PLACE_ID));", explodeOnFailure(context, async, onSuccess -> { async.complete(); })
                        );
        }));
    }
}
