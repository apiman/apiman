/*
 * Copyright 2018 JBoss Inc
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

package io.apiman.gateway.platforms.vertx3.connector;

import io.apiman.gateway.engine.impl.AbstractConnectorConfig;

import java.util.Set;
import java.util.TreeSet;

/**
 * Vert.x platform connector configuration.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class VertxConnectorConfig extends AbstractConnectorConfig {

    private static final Set<String> SUPPRESSED_REQUEST_HEADERS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private static final Set<String> SUPPRESSED_RESPONSE_HEADERS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    static {
        SUPPRESSED_REQUEST_HEADERS.add("X-API-Key");
        SUPPRESSED_REQUEST_HEADERS.add("Host");

        SUPPRESSED_RESPONSE_HEADERS.add("Connection");
    }

    public VertxConnectorConfig() {
        super(SUPPRESSED_REQUEST_HEADERS, SUPPRESSED_RESPONSE_HEADERS);
    }
}
