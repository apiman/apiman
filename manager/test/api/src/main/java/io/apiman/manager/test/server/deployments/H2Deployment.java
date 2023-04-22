/*
 * Copyright 2023 Black Parrot Labs Ltd
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
package io.apiman.manager.test.server.deployments;

import io.apiman.test.common.util.TestUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.jdbi.v3.core.Jdbi;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;

public class H2Deployment implements ITestDatabaseDeployment {

    /*
     * DataSource created - only if using JPA
     */
    private BasicDataSource ds = null;

    /**
     * Constructor.
     */
    public H2Deployment(Map<String, String> config) {
    }

    public H2Deployment() {}

    /**
     * Start/run the server.
     */
    public void start() {
        TestUtil.setProperty("apiman.hibernate.hbm2ddl.auto", "validate");

        try {
            InitialContext ctx = TestUtil.initialContext();
            TestUtil.ensureCtx(ctx, "java:/apiman");
            TestUtil.ensureCtx(ctx, "java:/apiman/datasources");
            String dbOutputPath = System.getProperty("apiman.test.h2-output-dir", null);
            if (dbOutputPath != null) {
                ds = createFileDatasource(new File(dbOutputPath));
            } else {
                ds = createInMemoryDatasource();
            }
            try {
                // For H2 versions older than 1.4.200 this ensures JSON type exists.
                Jdbi.create(ds).useHandle(h -> {
                    h.getConnection().setAutoCommit(false);
                    h.execute("CREATE domain IF NOT EXISTS json AS other");
                    h.commit();
                });
            } catch (Exception e) {}
            ctx.bind("java:/apiman/datasources/apiman-manager", ds);
        } catch (NamingException | SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Stop the server.
     * @throws Exception
     */
    public void stop() {
        try {
            if (ds != null) {
                // Drop everything from the in-memory DB in case a subsequent run re-uses this: could end up with unexpected DB state.
                try (var conn = ds.getConnection()) {
                    conn.prepareStatement("DROP ALL OBJECTS DELETE FILES").executeUpdate();
                    conn.commit();
                }
                ;
                ds.close();
                InitialContext ctx = TestUtil.initialContext();
                ctx.unbind("java:/apiman/datasources/apiman-manager");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an in-memory datasource.
     * @throws SQLException
     */
    private static BasicDataSource createInMemoryDatasource() throws SQLException {
        TestUtil.setProperty("apiman.hibernate.dialect", "io.apiman.manager.api.jpa.ApimanH2Dialect");
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setUrl("jdbc:h2:mem:test-apiman-inmem;DB_CLOSE_DELAY=-1");
        // Use this for trace level JDBC logging
        Connection connection = ds.getConnection();
        connection.close();
        System.out.println("DataSource created and bound to JNDI.");
        return ds;
    }

    /**
     * Creates an H2 file based datasource.
     * @throws SQLException
     */
    private static BasicDataSource createFileDatasource(File outputDirectory) throws SQLException {
        TestUtil.setProperty("apiman.hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setUrl("jdbc:h2:" + outputDirectory.toString() + "/apiman-manager-api;MVCC=true");
        Connection connection = ds.getConnection();
        connection.close();
        System.out.println("DataSource created and bound to JNDI.");
        return ds;
    }

}
