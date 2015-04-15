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

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.apiman.gateway.vertx.io.IResettable;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.impl.Json;
import org.vertx.java.core.logging.Logger;

/**
 * Send a service response
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class ServiceResponseExecutor implements IResettable {

    private EventBus eb;
    @SuppressWarnings("unused")
    private Logger logger;
    private String address;
    private boolean finished = false;

    public ServiceResponseExecutor(EventBus eb, Logger logger, String address) {
        this.eb = eb;
        this.logger = logger;
        this.address = address;
    }

    public void writeResponse(ServiceResponse serviceResponse) {
        eb.send(address + VertxEngineConfig.API_GATEWAY_HEAD_SUFFIX, Json.encode(serviceResponse));
    }

    public void write(Buffer bodyBuffer) {
        if(finished) {
            throw new IllegalStateException("Attempted write to connector after #end() was called."); //$NON-NLS-1$
        }

        eb.send(address + VertxEngineConfig.API_GATEWAY_BODY_SUFFIX, bodyBuffer);
    }

    public void end() {
        eb.send(address + VertxEngineConfig.API_GATEWAY_END_SUFFIX, (Void) null);
        finished = true;
    }

    public void error(Throwable error) {
        eb.send(address + VertxEngineConfig.API_GATEWAY_ERROR_SUFFIX, Json.encode(error));
    }

    public void failure(PolicyFailure failure) {
        eb.send(address + VertxEngineConfig.API_GATEWAY_FAILURE_SUFFIX, Json.encode(failure));
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void reset() {
        finished = false;
    }
}
