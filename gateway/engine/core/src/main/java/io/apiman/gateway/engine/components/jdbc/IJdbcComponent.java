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
package io.apiman.gateway.engine.components.jdbc;

import javax.sql.DataSource;

/**
 * A simple async JDBC component interface
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface IJdbcComponent {

    /**
     * Create a shared data source.
     *
     * @param dsName the data source name
     * @param config the config
     * @return the async JDBC client
     */
    IJdbcClient createShared(String dsName, JdbcOptionsBean config);

    /**
     * Create a random DS (i.e. non-shared).
     * @param config the config
     *
     * @return the async JDBC client
     */
    IJdbcClient createStandalone(JdbcOptionsBean config);

    /**
     * Create client using provided DS. DS should be configured in advance.
     *
     * @param ds the configured datasource
     *
     * @return the async JDBC client
     */
    IJdbcClient create(DataSource ds);
}
