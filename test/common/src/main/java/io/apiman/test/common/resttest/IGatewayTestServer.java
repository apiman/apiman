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
package io.apiman.test.common.resttest;

import org.codehaus.jackson.JsonNode;

/**
 * Any gateway under test would need to implement this interface, along
 * with standing up an actual gateway instance (with API).  The gateway
 * REST tests will then send http messages to the appropriate endpoints
 * in order to affect the test being run.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IGatewayTestServer {

    public void configure(JsonNode config);

    public String getApiEndpoint();

    public String getGatewayEndpoint();

    public String getEchoTestEndpoint();

    public void start();

    public void stop();

}
