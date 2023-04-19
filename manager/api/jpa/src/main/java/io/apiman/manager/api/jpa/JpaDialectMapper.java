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
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQL91Dialect;
import org.hibernate.dialect.PostgreSQL92Dialect;
import org.hibernate.dialect.PostgreSQL93Dialect;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Initializes the database by installing the appropriate DDL for the database in
 * use.
 * @author eric.wittmann@gmail.com
 */
// TODO(msavy): let's replace this with the liquibase DB initialiser + migrator
public class JpaDialectMapper {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(JpaDialectMapper.class);
    public static final Map<String, NamePair> DB_TYPE_MAP = new HashMap<>();

    static {
        DB_TYPE_MAP.put("h2", NamePair.of("h2", ApimanH2Dialect.class.getName()));
        DB_TYPE_MAP.put(H2Dialect.class.getName(), NamePair.of("h2", H2Dialect.class.getName()));
        DB_TYPE_MAP.put(ApimanH2Dialect.class.getName(), NamePair.of("h2", ApimanH2Dialect.class.getName()));

        DB_TYPE_MAP.put("mysql8",  NamePair.of("mysql8", ApimanMySQL8Dialect.class.getName()));
        DB_TYPE_MAP.put("io.apiman.manager.api.jpa.ApimanMySQL5Dialect", NamePair.of("mysql8",  ApimanMySQL8Dialect.class.getName())); // compatibility but might need to drop this
        DB_TYPE_MAP.put(ApimanMySQL8Dialect.class.getName(),  NamePair.of("mysql8", ApimanMySQL8Dialect.class.getName())); // Hmm
        DB_TYPE_MAP.put(MySQLDialect.class.getName(), NamePair.of("mysql8", MySQLDialect.class.getName()));
        DB_TYPE_MAP.put(MySQL8Dialect.class.getName(), NamePair.of("mysql8", MySQL8Dialect.class.getName()));

        DB_TYPE_MAP.put("oracle19",  NamePair.of("oracle19", ApimanOracle19Dialect.class.getName()));
        DB_TYPE_MAP.put(ApimanOracle19Dialect.class.getName(),  NamePair.of("oracle19", ApimanOracle19Dialect.class.getName()));
        DB_TYPE_MAP.put(OracleDialect.class.getName(),  NamePair.of("oracle19", OracleDialect.class.getName()));
        DB_TYPE_MAP.put(Oracle8iDialect.class.getName(),  NamePair.of("oracle19", Oracle8iDialect.class.getName()));
        DB_TYPE_MAP.put(Oracle9iDialect.class.getName(),  NamePair.of("oracle19", Oracle9iDialect.class.getName()));

        DB_TYPE_MAP.put("postgresql11",  NamePair.of("postgresql11", ApimanPostgreSQLDialect.class.getName()));
        DB_TYPE_MAP.put(ApimanPostgreSQLDialect.class.getName(),  NamePair.of("postgresql11",ApimanPostgreSQLDialect.class.getName()));
        DB_TYPE_MAP.put(PostgreSQLDialect.class.getName(), NamePair.of("postgresql11", PostgreSQLDialect.class.getName()));
        DB_TYPE_MAP.put(PostgreSQL9Dialect.class.getName(), NamePair.of("postgresql11", PostgreSQL9Dialect.class.getName()));
        DB_TYPE_MAP.put(PostgreSQL91Dialect.class.getName(), NamePair.of("postgresql11", PostgreSQL91Dialect.class.getName()));
        DB_TYPE_MAP.put(PostgreSQL92Dialect.class.getName(), NamePair.of("postgresql11", PostgreSQL92Dialect.class.getName()));
        DB_TYPE_MAP.put(PostgreSQL93Dialect.class.getName(), NamePair.of("postgresql11", PostgreSQL93Dialect.class.getName()));
        DB_TYPE_MAP.put(PostgreSQL94Dialect.class.getName(), NamePair.of("postgresql11", PostgreSQL94Dialect.class.getName()));
        DB_TYPE_MAP.put(PostgreSQL95Dialect.class.getName(), NamePair.of("postgresql11", PostgreSQL95Dialect.class.getName()));

        DB_TYPE_MAP.put("mssql15", NamePair.of(SQLServerDialect.class.getName(), "mssql15"));
        DB_TYPE_MAP.put(SQLServerDialect.class.getName(), NamePair.of("mssql15", ApimanMSSQLDialect.class.getName()));
        DB_TYPE_MAP.put(ApimanMSSQLDialect.class.getName(), NamePair.of("mssql15", ApimanMSSQLDialect.class.getName()));
    }

    private final DataSource ds;
    private final NamePair namePair;

    /**
     * Constructor.
     */
    public JpaDialectMapper(String dsJndiLocation, String hibernateDialect) {
        if (dsJndiLocation == null) {
            throw new RuntimeException("Missing datasource JNDI location from JPA storage configuration.");
        }
        ds = lookupDS(dsJndiLocation);

        this.namePair = DB_TYPE_MAP.get(hibernateDialect);
        if (namePair == null) {
            throw new RuntimeException("Unknown hibernate dialect configured: " + hibernateDialect);
        }
    }

    public static String lookupFqdn(String apimanDialect) {
        var pair = DB_TYPE_MAP.get(apimanDialect);
        if (pair != null) {
            return pair.fqdn;
        } else {
            return apimanDialect;
        }
    }

    public String getSimpleName() {
        return namePair.simpleName;
    }

    public String getResolvedDialect() {
        return namePair.fqdn;
    }

    /**
     * Lookup the datasource in JNDI.
     * @param dsJndiLocation
     */
    public static DataSource lookupDS(String dsJndiLocation) {
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

    private static final class NamePair {
        public String simpleName;
        public String fqdn;

        static NamePair of(String simpleName, String fqdn) {
            var np = new NamePair();
            np.simpleName = simpleName;
            np.fqdn = fqdn;
            return np;
        }
    }

}
