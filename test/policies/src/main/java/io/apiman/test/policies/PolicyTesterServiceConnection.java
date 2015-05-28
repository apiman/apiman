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

import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.io.IApimanBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A connection a simulated back end service.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyTesterServiceConnection implements IServiceConnection {

    private static IPolicyTestBackEndService createBackEndService(String endpoint) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends IPolicyTestBackEndService> theClass = (Class<? extends IPolicyTestBackEndService>) Class.forName(endpoint);
            return theClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final ServiceRequest request;
    private final IAsyncResultHandler<IServiceConnectionResponse> handler;
    private ByteArrayOutputStream output;
    private final IPolicyTestBackEndService backEndService;
    private boolean finished;

    /**
     * Constructor.
     * @param service
     * @param request
     * @param handler
     */
    public PolicyTesterServiceConnection(Service service, ServiceRequest request,
            IAsyncResultHandler<IServiceConnectionResponse> handler) {
        this.request = request;
        this.handler = handler;
        this.output = null;
        this.backEndService = createBackEndService(service.getEndpoint());
    }

    /**
     * @see io.apiman.gateway.engine.io.IWriteStream#write(io.apiman.gateway.engine.io.IApimanBuffer)
     */
    @Override
    public void write(IApimanBuffer chunk) {
        if (this.output == null) {
            this.output = new ByteArrayOutputStream();
        }
        try {
            this.output.write(chunk.getBytes());
        } catch (IOException e) {
            // should never happen :)
        }
    }

    /**
     * @see io.apiman.gateway.engine.io.IWriteStream#end()
     */
    @Override
    public void end() {
        PolicyTestBackEndServiceResponse response = backEndService.invoke(request,
                output == null ? null : output.toByteArray());
        IServiceConnectionResponse connectionResponse = new PolicyTesterServiceConnectionResponse(response);
        handler.handle(AsyncResultImpl.create(connectionResponse));
        finished = true;
    }

    /**
     * @see io.apiman.gateway.engine.io.IStream#isFinished()
     */
    @Override
    public boolean isFinished() {
        return finished;
    }

    /**
     * @see io.apiman.gateway.engine.IServiceConnection#isConnected()
     */
    @Override
    public boolean isConnected() {
        return !isFinished();
    }

    /**
     * @see io.apiman.gateway.engine.io.IAbortable#abort()
     */
    @Override
    public void abort() {
        finished = true;
    }

}
