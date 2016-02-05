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
package io.apiman.gateway.engine.io;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.ApiRequest;

/**
 * Generic representation of a three part stream: head, body and end, with
 * handlers and operators for each.
 *
 * For example: The head might represent a {@link ApiRequest}; the body is a
 * stream of {@link IApimanBuffer} chunks; {@link #end()} is used indicate that
 * transmission of the body has completed.
 *
 * The head handler is also used to indicate the result of the operation.
 *
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <H> Type of head
 */
public abstract class AbstractStream<H> implements IReadWriteStream<H> {

    protected IAsyncHandler<H> headHandler;
    protected IAsyncHandler<IApimanBuffer> bodyHandler;
    protected IAsyncHandler<Void> endHandler;
    protected boolean finished = false;

    /**
     * Sets the head handler.
     * @param headHandler head handler
     */
    public void headHandler(IAsyncHandler<H> headHandler) {
        this.headHandler = headHandler;
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
     * @see io.apiman.gateway.engine.io.IWriteStream#write(io.apiman.gateway.engine.io.IApimanBuffer)
     */
    @Override
    public void write(IApimanBuffer chunk) {
        if (bodyHandler != null) {
            bodyHandler.handle(chunk);
        }
    }

    /**
     * @see io.apiman.gateway.engine.io.IWriteStream#end()
     */
    @Override
    public void end() { 
        if (endHandler != null) {
            endHandler.handle((Void) null);
        }
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
     * Called to handle the head.
     * @param head
     */
    protected abstract void handleHead(H head);

    /**
     * Called to handle a body chunk.
     * @param chunk
     */
    protected void handleBody(IApimanBuffer chunk) {
        if (bodyHandler != null) {
            bodyHandler.handle(chunk);
        }
    }

    /**
     * Signals the end of the stream.
     */
    protected void handleEnd() {
        if (endHandler != null) {
            endHandler.handle((Void) null);
        }

        finished = true;
    }
}
