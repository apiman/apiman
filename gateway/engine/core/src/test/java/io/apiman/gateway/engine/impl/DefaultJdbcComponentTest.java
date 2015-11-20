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

package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.IJdbcResultSet;
import io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Full test for the default JDBC component.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class DefaultJdbcComponentTest {

    List<Throwable> errors = new ArrayList<>();

    @Before
    public void before() {
        System.out.println("Before!");
        errors.clear();
    }

    @Test
    public void testStandalone() throws Throwable {
        DefaultJdbcComponent component = new DefaultJdbcComponent();
        JdbcOptionsBean config = new JdbcOptionsBean();
        config.setAutoCommit(true);
        config.setJdbcUrl("jdbc:h2:mem:testStandalone;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        IJdbcClient client = component.createStandalone(config);
        doAllTests(client);
    }

    @Test
    public void testShared() throws Throwable {
        DefaultJdbcComponent component = new DefaultJdbcComponent();
        JdbcOptionsBean config = new JdbcOptionsBean();
        config.setAutoCommit(true);
        config.setJdbcUrl("jdbc:h2:mem:testShared;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        IJdbcClient client = component.createShared("sharedDS", config);
        doAllTests(client);
    }

    @Test
    public void testDataSource() throws Throwable {
        DefaultJdbcComponent component = new DefaultJdbcComponent();

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setUrl("jdbc:h2:mem:testDataSource;DB_CLOSE_DELAY=-1");

        try {
            IJdbcClient client = component.create(ds);
            doAllTests(client);
        } finally {
            try {
                ds.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param client
     */
    private void doAllTests(IJdbcClient client) throws Throwable {
        client.connect(new IAsyncResultHandler<IJdbcConnection>() {
            @Override
            public void handle(IAsyncResult<IJdbcConnection> result) {
                if (result.isError()) {
                    errors.add(result.getError());
                } else {
                    doCreate(result.getResult());
                }
            }
        });
        try {
            Throwable firstError = null;
            for (Throwable throwable : errors) {
                System.out.println("------------ ERROR DETECTED ----------------");
                throwable.printStackTrace();
                System.out.println("------------ -------------- ----------------");
                if (firstError == null) {
                    firstError = throwable;
                }
            }
            if (firstError != null) {
                throw firstError;
            }
        } catch (Throwable t) {
            throw t;
        }
    }

    /**
     * @param connection
     */
    protected void doCreate(final IJdbcConnection connection) {
        String createSql = "CREATE TABLE users ("
                            + "userId VARCHAR(255) NOT NULL, "
                            + "name VARCHAR(255) NOT NULL, "
                            + "age INTEGER, "
                            + "isAdmin BIT, "
                            + "PRIMARY KEY (userId))";
        IAsyncResultHandler<Void> afterCreateTable = new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isError()) {
                    errors.add(result.getError());
                } else {
                    System.out.println("Create worked, inserting.");
                    doInserts(connection);
                }
            }
        };
        connection.execute(afterCreateTable, createSql);
    }

    /**
     * @param connection
     */
    protected void doInserts(IJdbcConnection connection) {
        String insertSql = "INSERT INTO users (userId, name, age, isAdmin) VALUES (?, ?, ?, ?)";
        IAsyncResultHandler<Void> dfltHandler = new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isError()) {
                    errors.add(result.getError());
                } else {
                    System.out.println("Insert successful.");
                }
            }
        };
        connection.execute(dfltHandler, insertSql, "ewittman", "Eric Wittmann", 17, Boolean.TRUE);
        connection.execute(dfltHandler, insertSql, "msavy", "Marc Savy", 50, Boolean.FALSE);
        connection.execute(dfltHandler, insertSql, "ryordan", "Rachel Yordan", 92, Boolean.FALSE);

        doQueries(connection);
    }

    /**
     * @param connection
     */
    private void doQueries(IJdbcConnection connection) {
        IAsyncResultHandler<IJdbcResultSet> queryHandler = new IAsyncResultHandler<IJdbcResultSet>() {
            @Override
            public void handle(IAsyncResult<IJdbcResultSet> result) {
                if (result.isError()) {
                    errors.add(result.getError());
                } else {
                    try {
                        doQueryAssertions(result.getResult());
                    } catch (Throwable t) {
                        errors.add(t);
                    }
                }
            }
        };
        String querySql = "SELECT u.userId, u.name, u.age, u.isAdmin FROM users u WHERE userId = ?";
        connection.query(queryHandler, querySql , "ewittman");
    }

    /**
     * @param result
     */
    protected void doQueryAssertions(IJdbcResultSet result) {
        Assert.assertTrue("Expected the result set to have at least one row.", result.hasNext());
        result.next();
        Assert.assertEquals("ewittman", result.getString(1));
        Assert.assertEquals("Eric Wittmann", result.getString(2));
        Assert.assertEquals(new Integer(17), result.getInteger(3));
        Assert.assertEquals(Boolean.TRUE, result.getBoolean(4));
        Assert.assertFalse(result.hasNext());
    }

}
