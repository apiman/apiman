/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.manager.api.jpa;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.ddl.DdlParser;

import java.io.InputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.PostgreSQL91Dialect;
import org.hibernate.dialect.PostgreSQL92Dialect;
import org.hibernate.dialect.PostgreSQL93Dialect;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.jdbi.v3.core.Jdbi;

/**
 * Initializes the database by installing the appropriate DDL for the database in
 * use.
 * @author eric.wittmann@gmail.com
 */
// TODO(msavy): let's replace this with the liquibase DB initialiser + migrator
public class JpaStorageInitializer {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(JpaStorageInitializer.class);
    private static final Map<String, String> DB_TYPE_MAP = new HashMap<>();
    static {
        DB_TYPE_MAP.put(ApimanH2Dialect.class.getName(), "h2");
        DB_TYPE_MAP.put(H2Dialect.class.getName(), "h2");

        DB_TYPE_MAP.put("io.apiman.manager.api.jpa.ApimanMySQL5Dialect",  "mysql8"); // compatibility but might need to drop this
        DB_TYPE_MAP.put(ApimanMySQL8Dialect.class.getName(),  "mysql8"); // Hmm
        DB_TYPE_MAP.put(MySQLDialect.class.getName(), "mysql8");
        DB_TYPE_MAP.put(MySQL8Dialect.class.getName(), "mysql8");

        DB_TYPE_MAP.put(ApimanOracle19Dialect.class.getName(),  "oracle19");
        DB_TYPE_MAP.put(OracleDialect.class.getName(), "oracle19");
        DB_TYPE_MAP.put(Oracle8iDialect.class.getName(), "oracle19");
        DB_TYPE_MAP.put(Oracle9iDialect.class.getName(), "oracle19");

        DB_TYPE_MAP.put(ApimanPostgreSQLDialect.class.getName(),  "postgresql9");
        DB_TYPE_MAP.put(PostgreSQLDialect.class.getName(), "postgresql9");
        DB_TYPE_MAP.put(PostgreSQL81Dialect.class.getName(), "postgresql9");
        DB_TYPE_MAP.put(PostgreSQL82Dialect.class.getName(), "postgresql9");
        DB_TYPE_MAP.put(PostgreSQL9Dialect.class.getName(), "postgresql9");
        DB_TYPE_MAP.put(PostgreSQL91Dialect.class.getName(), "postgresql9");
        DB_TYPE_MAP.put(PostgreSQL92Dialect.class.getName(), "postgresql9");
        DB_TYPE_MAP.put(PostgreSQL93Dialect.class.getName(), "postgresql9");
        DB_TYPE_MAP.put(PostgreSQL94Dialect.class.getName(), "postgresql9");
        DB_TYPE_MAP.put(PostgreSQL95Dialect.class.getName(), "postgresql9");

        DB_TYPE_MAP.put(SQLServerDialect.class.getName(), "mssql15");
        DB_TYPE_MAP.put(SQLServer2012Dialect.class.getName(), "mssql15");

    }
    
    private final DataSource ds;
    private final String dbType;

    /**
     * Constructor.
     */
    public JpaStorageInitializer(String dsJndiLocation, String hibernateDialect) {
        if (dsJndiLocation == null) {
            throw new RuntimeException("Missing datasource JNDI location from JPA storage configuration."); 
        }
        ds = lookupDS(dsJndiLocation);

        dbType = DB_TYPE_MAP.get(hibernateDialect);
        if (dbType == null) {
            throw new RuntimeException("Unknown hibernate dialect configured: " + hibernateDialect); 
        }
    }

    /**
     * Lookup the datasource in JNDI.
     * @param dsJndiLocation
     */
    private static DataSource lookupDS(String dsJndiLocation) {
        DataSource ds;
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(dsJndiLocation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (ds == null) {
            throw new RuntimeException("Datasource not found: " + dsJndiLocation); 
        }
        return ds;
    }

    /**
     * Called to initialize the database.
     */
    @SuppressWarnings("nls")
    public void initialize() {
        QueryRunner run = new QueryRunner(ds);
        Boolean isInitialized;
        
        try {
            isInitialized = run.query("SELECT * FROM apis", new ResultSetHandler<Boolean>() {
                @Override
                public Boolean handle(ResultSet rs) throws SQLException {
                    return true;
                }
            });
        } catch (SQLException e) {
            LOGGER.trace("Is initialised error: {0}", e);
            isInitialized = false;
        }
        
        if (isInitialized) {
            LOGGER.info("============================================");
            LOGGER.info("Apiman Manager database already initialized.");
            LOGGER.info("============================================");
            return;
        }

        ClassLoader cl = JpaStorageInitializer.class.getClassLoader();
        URL resource = cl.getResource("ddls/apiman_" + dbType + ".ddl");
        Objects.requireNonNull(resource, "DDLs are missing");
        try {
            try (InputStream is = resource.openStream()) {
                LOGGER.info("=======================================");
                LOGGER.info("Initializing Apiman Manager database. "  + resource.getPath());
                DdlParser ddlParser = new DdlParser();
                List<String> statements = ddlParser.parse(is);
                Jdbi jdbi = Jdbi.create(ds);
                for (String sql : statements) {
                    LOGGER.info(sql);
                    jdbi.useHandle(h -> {
                        h.getConnection().setAutoCommit(false);
                        h.createUpdate(sql).execute();
                        h.commit();
                    });
                }
                LOGGER.info("=======================================");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
