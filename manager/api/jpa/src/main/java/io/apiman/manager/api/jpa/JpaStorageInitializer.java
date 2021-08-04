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
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQL57InnoDBDialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.PostgreSQL92Dialect;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.dialect.SQLServerDialect;

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
        DB_TYPE_MAP.put(ApimanH2Dialect.class.getName(), "h2"); //$NON-NLS-1$
        DB_TYPE_MAP.put(H2Dialect.class.getName(), "h2"); //$NON-NLS-1$

        DB_TYPE_MAP.put(ApimanMySQL5Dialect.class.getName(), "mysql5"); //$NON-NLS-1$
        DB_TYPE_MAP.put(MySQL5Dialect.class.getName(), "mysql5"); //$NON-NLS-1$
        DB_TYPE_MAP.put(MySQLDialect.class.getName(), "mysql5"); //$NON-NLS-1$
        DB_TYPE_MAP.put(MySQL5InnoDBDialect.class.getName(), "mysql5"); //$NON-NLS-1$
        DB_TYPE_MAP.put(MySQL57InnoDBDialect.class.getName(), "mysql5"); //$NON-NLS-1$

        DB_TYPE_MAP.put(ApimanOracle12Dialect.class.getName(), "oracle12"); //$NON-NLS-1$
        DB_TYPE_MAP.put(Oracle12cDialect.class.getName(), "oracle12"); //$NON-NLS-1$
        DB_TYPE_MAP.put(Oracle10gDialect.class.getName(), "oracle12"); //$NON-NLS-1$

        DB_TYPE_MAP.put(ApimanPostgreSQLDialect.class.getName(), "postgresql9"); //$NON-NLS-1$
        DB_TYPE_MAP.put(PostgreSQL9Dialect.class.getName(), "postgresql9"); //$NON-NLS-1$
        DB_TYPE_MAP.put(PostgreSQL82Dialect.class.getName(), "postgresql9"); //$NON-NLS-1$
        DB_TYPE_MAP.put(PostgreSQL92Dialect.class.getName(), "postgresql9"); //$NON-NLS-1$
        DB_TYPE_MAP.put(PostgreSQL94Dialect.class.getName(), "postgresql9"); //$NON-NLS-1$

        DB_TYPE_MAP.put(SQLServer2012Dialect.class.getName(), "mssql11"); //$NON-NLS-1$
        DB_TYPE_MAP.put(SQLServerDialect.class.getName(), "mssql11"); //$NON-NLS-1$
    }
    
    private final DataSource ds;
    private final String dbType;

    /**
     * Constructor.
     */
    public JpaStorageInitializer(String dsJndiLocation, String hibernateDialect) {
        if (dsJndiLocation == null) {
            throw new RuntimeException("Missing datasource JNDI location from JPA storage configuration."); //$NON-NLS-1$
        }
        ds = lookupDS(dsJndiLocation);
        dbType = DB_TYPE_MAP.get(hibernateDialect);
        if (dbType == null) {
            throw new RuntimeException("Unknown hibernate dialect configured: " + hibernateDialect); //$NON-NLS-1$
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
            throw new RuntimeException("Datasource not found: " + dsJndiLocation); //$NON-NLS-1$
        }
        return ds;
    }

    /**
     * Called to initialize the database.
     */
    @SuppressWarnings("nls")
    public void initialize() {
        System.out.println("Going to try to initialise JPA storage");

        QueryRunner run = new QueryRunner(ds);
        Boolean isInitialized;
        
        try {
            //ds.getConnection().setAutoCommit(false);

            isInitialized = run.query("SELECT * FROM apis", new ResultSetHandler<Boolean>() {
                @Override
                public Boolean handle(ResultSet rs) throws SQLException {
                    return true;
                }
            });
        } catch (SQLException e) {
            LOGGER.error(e);
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
        try (InputStream is = resource.openStream()) {
            LOGGER.info("=======================================");
            LOGGER.info("Initializing Apiman Manager database. "  + resource.getPath());
            DdlParser ddlParser = new DdlParser();
            List<String> statements = ddlParser.parse(is);
            for (String sql : statements){
                LOGGER.info(sql);
                run.update(sql);
            }
            LOGGER.info("=======================================");
            //run.getDataSource().getConnection().commit();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
