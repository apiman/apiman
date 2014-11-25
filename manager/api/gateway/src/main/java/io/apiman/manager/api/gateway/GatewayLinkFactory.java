/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.manager.api.gateway;

import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.GatewayType;
import io.apiman.manager.api.gateway.rest.RestGatewayLink;

import javax.enterprise.context.ApplicationScoped;

/**
 * Factory for creating gateway links.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class GatewayLinkFactory implements IGatewayLinkFactory {

    /**
     * Constructor.
     */
    public GatewayLinkFactory() {
    }
    
    /**
     * @see io.apiman.manager.api.gateway.IGatewayLinkFactory#create(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public IGatewayLink create(GatewayBean gateway) {
        if (gateway.getType() == GatewayType.REST) {
            return new RestGatewayLink(gateway);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
}
