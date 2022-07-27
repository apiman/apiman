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

package io.apiman.gateway.engine.jdbc;

import io.apiman.common.util.ddl.DdlParser;
import io.apiman.gateway.engine.IGatewayInitializer;

import java.io.InputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Used to (optionally) initialize the Gateway database using a DDL.  This 
 * should probably be disabled in production, in favor of a proper DDL promotion
 * strategy (I'm talking to you, DBAs!).
 * 
 * @author eric.wittmann@gmail.com
 */
public class JdbcInitializer extends AbstractJdbcComponent implements IGatewayInitializer {
    
    private final String dbType;
    
    /**
     * Constructor.
     * @param config
     */
    public JdbcInitializer(Map<String, String> config) {
        super(config);
        dbType = config.get("datasource.type"); //$NON-NLS-1$
        if (dbType == null) {
            throw new IllegalArgumentException("Missing configuration parameter for JDBC Initializer: 'datasource.type',  Sample values: h2, mysql5, postgresql9, oracle12"); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IGatewayInitializer#initialize()
     */
    @Override
    public void initialize() {
        try {
            doInit();
        } catch (Throwable t) {
            // Wait and try again...
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            doInit();
        }
    }

    /**
     * Do the initialization work.
     */
    @SuppressWarnings("nls")
    private void doInit() {
        QueryRunner run = new QueryRunner(ds);
        Boolean isInitialized;
        
        try {
            isInitialized = run.query("SELECT * FROM gw_apis", rs -> true);
        } catch (SQLException e) {
            isInitialized = false;
        }
        
        if (isInitialized) {
            System.out.println("============================================");
            System.out.println("Apiman Gateway database already initialized.");
            System.out.println("============================================");
            return;
        }
        
        ClassLoader cl = JdbcInitializer.class.getClassLoader();
        URL resource = cl.getResource("ddls/apiman-gateway_" + dbType + ".ddl");
        Objects.requireNonNull(resource, "No DDL found for database type: '" + dbType + "'");
        try (InputStream is = resource.openStream()) {
            System.out.println("=======================================");
            System.out.println("Initializing apiman Gateway database.");
            DdlParser ddlParser = new DdlParser();
            List<String> statements = ddlParser.parse(is);
            for (String sql : statements){
                System.out.println(sql);
                run.update(sql);
            }
            System.out.println("=======================================");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
