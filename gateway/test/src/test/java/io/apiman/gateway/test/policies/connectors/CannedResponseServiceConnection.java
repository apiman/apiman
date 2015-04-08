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
package io.apiman.gateway.test.policies.connectors;

import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.engine.io.ByteBuffer;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.platforms.servlet.GatewayThreadContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Simulates a live connection but always returns the same response
 *
 * @author rubenrm1@gmail.com
 */
public class CannedResponseServiceConnection implements IServiceConnection, IServiceConnectionResponse {

    private static final byte[] CANNED_RESPONSE = "{ \"message\" : \"Hello, this is an intercepted response\" }".getBytes(); //$NON-NLS-1$
    
    private IAsyncResultHandler<IServiceConnectionResponse> responseHandler;
    private boolean connected;
    
    private IAsyncHandler<IApimanBuffer> bodyHandler;
    private IAsyncHandler<Void> endHandler;
    
    private ServiceResponse response;

    /**
     * Constructor.
     * @param handler the Service Connection Response handler
     * @throws ConnectorException when failed to connect to back-end
     */
    public CannedResponseServiceConnection(IAsyncResultHandler<IServiceConnectionResponse> handler) throws ConnectorException {
        this.responseHandler = handler;
        connected = true;
    }

    /**
     * @see io.apiman.gateway.engine.io.IReadStream#bodyHandler(io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler) {
        this.bodyHandler = bodyHandler;
    }

    /**
     * @see io.apiman.gateway.engine.io.IReadStream#endHandler(io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public void endHandler(IAsyncHandler<Void> endHandler) {
        this.endHandler = endHandler;
    }

    /**
     * @see io.apiman.gateway.engine.io.IReadStream#getHead()
     */
    @Override
    public ServiceResponse getHead() {
        return response;
    }

    /**
     * @see io.apiman.gateway.engine.io.IStream#isFinished()
     */
    @Override
    public boolean isFinished() {
        return !connected;
    }

    /**
     * @see io.apiman.gateway.engine.io.IAbortable#abort()
     */
    @Override
    public void abort() {
        connected = false;
    }

    /**
     * @see io.apiman.gateway.engine.io.IWriteStream#write(io.apiman.gateway.engine.io.IApimanBuffer)
     */
    @Override
    public void write(IApimanBuffer chunk) {
        // do nothing
    }

    /**
     * @see io.apiman.gateway.engine.io.IWriteStream#end()
     */
    @Override
    public void end() {
        // Process the response, convert to a ServiceResponse object, and return it
        response = GatewayThreadContext.getServiceResponse();
        response.getHeaders().put("Content-Type", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
        response.setCode(200);
        response.setMessage("OK"); //$NON-NLS-1$
        responseHandler.handle(AsyncResultImpl.<IServiceConnectionResponse>create(this));
    }

    /**
     * @see io.apiman.gateway.engine.io.ISignalReadStream#transmit()
     */
    @Override
    public void transmit() {
        try {
            InputStream is = new ByteArrayInputStream(CANNED_RESPONSE);
            ByteBuffer buffer = new ByteBuffer(CANNED_RESPONSE.length);
            buffer.readFrom(is);
            bodyHandler.handle(buffer);
            IOUtils.closeQuietly(is);
            connected = false;
            endHandler.handle(null);
        } catch (Throwable e) {
            if (connected) {
                abort();
            }
            throw new RuntimeException(e);
        }
    }
    
}
