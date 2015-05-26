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
package io.apiman.test.policies;

import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;

/**
 * The {@link IConnectorFactory} used by the policy testing framework.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyTesterConnectorFactory implements IConnectorFactory {

    /**
     * @see io.apiman.gateway.engine.IConnectorFactory#createConnector(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.beans.Service)
     */
    @Override
    public IServiceConnector createConnector(ServiceRequest request, Service service, RequiredAuthType authType) {
        return new PolicyTesterConnector(service);
    }

}
