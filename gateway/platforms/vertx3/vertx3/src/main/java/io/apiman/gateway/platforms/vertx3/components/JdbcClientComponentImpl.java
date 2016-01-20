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
package io.apiman.gateway.platforms.vertx3.components;

import io.apiman.gateway.engine.components.IJdbcComponent;
import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
import io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.components.jdbc.VertxJdbcClientImpl;
import io.vertx.core.Vertx;

import java.util.Map;

import javax.sql.DataSource;

/**
* @author Marc Savy {@literal <msavy@redhat.com>}
*/
public class JdbcClientComponentImpl implements IJdbcComponent {

    private Vertx vertx;

    public JdbcClientComponentImpl(Vertx vertx, VertxEngineConfig engineConfig, Map<String, String> componentConfig) {
        this.vertx = vertx;
    }

    @Override
    public IJdbcClient createShared(String dsName, JdbcOptionsBean config) {
        return new VertxJdbcClientImpl(vertx, dsName, config);
    }

    @Override
    public IJdbcClient createStandalone(JdbcOptionsBean config) {
        return new VertxJdbcClientImpl(vertx, config);
    }

    @Override
    public IJdbcClient create(DataSource ds) {
        return new VertxJdbcClientImpl(vertx, ds);
    }

}
