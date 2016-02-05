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

import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.io.IApimanBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A connection a simulated back end API.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyTesterApiConnection implements IApiConnection {

    private static IPolicyTestBackEndApi createBackEndApi(String endpoint) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends IPolicyTestBackEndApi> theClass = (Class<? extends IPolicyTestBackEndApi>) Class.forName(endpoint);
            return theClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final ApiRequest request;
    private final IAsyncResultHandler<IApiConnectionResponse> handler;
    private ByteArrayOutputStream output;
    private final IPolicyTestBackEndApi backEndApi;
    private boolean finished;

    /**
     * Constructor.
     * @param api
     * @param request
     * @param handler
     */
    public PolicyTesterApiConnection(Api api, ApiRequest request,
            IAsyncResultHandler<IApiConnectionResponse> handler) {
        this.request = request;
        this.handler = handler;
        this.output = null;
        this.backEndApi = createBackEndApi(api.getEndpoint());
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
        PolicyTestBackEndApiResponse response = backEndApi.invoke(request,
                output == null ? null : output.toByteArray());
        IApiConnectionResponse connectionResponse = new PolicyTesterApiConnectionResponse(response);
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
     * @see io.apiman.gateway.engine.IApiConnection#isConnected()
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
