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
import io.apiman.gateway.engine.beans.ServiceRequest;

/**
 * Read a chunked stream by setting handlers.
 * 
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <H> Type of head object
 */
public interface IReadStream<H> extends IStream {
    /**
     * Handler called when a body chunk has arrived.
     * 
     * @param bodyHandler
     */
    public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler);

    /**
     * Handler signals when transmission has completed; no further calls to
     * {@link #bodyHandler(IAsyncHandler)} should occur after this has been
     * invoked.
     * 
     * @param endHandler
     */
    public void endHandler(IAsyncHandler<Void> endHandler);

    /**
     * Return the head object's handler (e.g. {@link ServiceRequest}).
     * 
     * @return the head object
     */
    public H getHead();
}
