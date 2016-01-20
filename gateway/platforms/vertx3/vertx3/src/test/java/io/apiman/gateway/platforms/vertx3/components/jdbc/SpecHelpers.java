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

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IJdbcComponent;
import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean;
import io.apiman.gateway.platforms.vertx3.components.JdbcClientComponentImpl;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/*
* @author Marc Savy {@literal <msavy@redhat.com>}
*/
@SuppressWarnings("nls")
public interface SpecHelpers {

    /**
     * Handler that fails tests if result is unsuccessful, with the exception included in the error.
     *
     * @param context the test context
     * @param async the async object
     * @param successHandler the success handler, called only if the result was successful
     * @return the result handler
     */
    static <T> IAsyncResultHandler<T> explodeOnFailure(TestContext context, Async async, Handler<T> successHandler) {
        return new IAsyncResultHandler<T>() {

            @Override
            public void handle(IAsyncResult<T> result) {
                if (result.isSuccess()) {
                    successHandler.handle(result.getResult());
                } else {
                    System.err.println("Operation failed"); //$NON-NLS-1$
                    context.fail(result.getError());
                }
            }
        };
    }

    static void resetDB(TestContext context, JdbcOptionsBean options, Vertx vertx) {
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(vertx, null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);

        client.connect(explodeOnFailure(context, async, connectionResult -> {
            System.out.println("Successfully connected!");
            IJdbcConnection connection = connectionResult;
            connection.execute(explodeOnFailure(context, async,
                    onSuccess -> {
                        System.out.println("Successfully reset DB!");
                        async.complete();
                    }),
                    "DROP ALL OBJECTS DELETE FILES");
        }));
    }

    static void createTable(TestContext context, JdbcOptionsBean options, Vertx vertx)  {
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(vertx, null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
                System.out.println("Successfully connected!");
                IJdbcConnection connection = connectionResult;
                String createTableSql = "create table APIMAN\n" +
                        "    (PLACE_ID integer NOT NULL,\n" +
                        "    COUNTRY varchar(40) NOT NULL,\n" +
                        "    CITY varchar(20) NOT NULL,\n" +
                        "    FOUNDING datetime NOT NULL,\n" +
                        "    PRIMARY KEY (PLACE_ID));";
                connection.execute(
                        explodeOnFailure(context, async, onSuccess -> { async.complete(); }),
                        createTableSql);
        }));
    }

    static void createTableAndPopulateData(TestContext context, JdbcOptionsBean options, Vertx vertx) {
        Async async = context.async();
        IJdbcComponent component = new JdbcClientComponentImpl(vertx, null, null); // Other two params aren't used.
        IJdbcClient client = component.createStandalone(options);
        client.connect(explodeOnFailure(context, async, connectionResult -> {
            System.out.println("Successfully connected!");
            IJdbcConnection connection = connectionResult;
            String createTableSql = "create table APIMAN\n" +
                    "    (PLACE_ID integer NOT NULL,\n" +
                    "    COUNTRY varchar(40) NOT NULL,\n" +
                    "    CITY varchar(20) NOT NULL,\n" +
                    "    FOUNDING datetime NOT NULL,\n" +
                    "    PRIMARY KEY (PLACE_ID));";
            String insertSql = "insert into APIMAN (PLACE_ID, COUNTRY, CITY, FOUNDING)\n" +
                    "     VALUES  (1, 'Seychelles', 'Victoria', '1976-06-29 00:00:00'), " + // June 29, 1976
                    "             (2, 'United States', 'Newtown', '1788-01-09 00:00:00')," + // January 9, 1788
                    "             (3, 'United States', 'Miami', '1896-07-28 00:00:00');"; // July 28, 1896
            connection.execute(
                    explodeOnFailure(context, async, onSuccess1 -> {
                        connection.execute(
                                explodeOnFailure(context, async, onSuccess2 -> { async.complete(); }),
                                insertSql);
                    }),
                    createTableSql
                    );
        }));
    }
}
