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
package io.apiman.gateway.vertx.conversation;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.vertx.config.VertxEngineConfig;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.logging.Logger;

/**
 * Handle a service request.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class ServiceRequestListener extends AbstractServiceListener<ServiceRequest> {

    public ServiceRequestListener(EventBus eb, Logger logger, String address) {
        super(eb, logger, address, ServiceRequest.class);
        logger.debug(ServiceRequestListener.class.getCanonicalName()+ " on " + address); //$NON-NLS-1$
    }

    @Override
    public void listen() {
        super.listen();
    }

    @Override
    public void reset() {
       finished = false;
    }

    public void ready() {
        logger.debug("Sending ready flag on the bus - ready to receive data " + //$NON-NLS-1$
          address + VertxEngineConfig.APIMAN_RT_READY_SUFFIX); 

        eb.send(address + VertxEngineConfig.APIMAN_RT_READY_SUFFIX, (Void) null);
    }
}
