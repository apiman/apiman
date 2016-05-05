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

package io.apiman.gateway.engine;

/**
 * Global list of all apiman gateway related properties.  Property names
 * reflect what would appear in apiman.properites.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class GatewayConfigProperties {

    public static final String MAX_PAYLOAD_BUFFER_SIZE = "apiman-gateway.max-payload-buffer-size";
    public static final String PUBLIC_ENDPOINT = "apiman-gateway.public-endpoint";

    public static final String REGISTRY_CLASS = "apiman-gateway.registry";
    public static final String PLUGIN_REGISTRY_CLASS = "apiman-gateway.plugin-registry";
    public static final String CONNECTOR_FACTORY_CLASS = "apiman-gateway.connector-factory";
    public static final String CONNECTOR_FACTORY_FOLLOW_REDIRECTS = "apiman-gateway.connector-factory.http.followRedirects";
    public static final String POLICY_FACTORY_CLASS = "apiman-gateway.policy-factory";
    public static final String POLICY_FACTORY_CLASS_RELOAD_SNAPSHOTS = "apiman-gateway.policy-factory.reload-snapshots";
    public static final String METRICS_CLASS = "apiman-gateway.metrics";
    public static final String LOGGER_FACTORY_CLASS = "apiman-gateway.logger-factory";
    public static final String DATA_ENCRYPTER_TYPE = "apiman.encrypter.type";
    public static final String COMPONENT_PREFIX = "apiman-gateway.components.";
    public static final String FAILURE_WRITER_CLASS = "apiman-gateway.writers.policy-failure";
    public static final String ERROR_WRITER_CLASS = "apiman-gateway.writers.error";
    public static final String INITIALIZERS = "apiman-gateway.initializers";

}
