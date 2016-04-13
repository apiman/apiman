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

import java.util.Map;

import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * A base class for all JDBC component implementations.  Handles the configuration
 * of the required datasource.
 * 
 * @author eric.wittmann@gmail.com
 */
public abstract class AbstractJdbcComponent {
    
    protected DataSource ds;

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public AbstractJdbcComponent(Map<String, String> config) {
        String dsJndiLocation = config.get("datasource.jndi-location"); //$NON-NLS-1$
        if (dsJndiLocation == null) {
            throw new RuntimeException("Missing datasource JNDI location from JdbcRegistry configuration."); //$NON-NLS-1$
        }
        ds = lookupDS(dsJndiLocation);
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
    
}
