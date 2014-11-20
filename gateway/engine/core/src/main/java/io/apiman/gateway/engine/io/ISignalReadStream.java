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

/**
 * As {@link IReadStream} but with explicit {@link #transmit()} which indicates to the sender that they may
 * begin transmitting.
 * 
 * Implementors must ensure that no data is sent before {@link #transmit()} is called; when or if the data is
 * ultimately sent is implementation dependent.
 * 
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <H> Head type.
 */
public interface ISignalReadStream<H> extends IReadStream<H>, IAbortable {

    /**
     * Signal that transmission may begin. No calls to {@link #bodyHandler(IAsyncHandler)} or
     * {@link #endHandler(IAsyncHandler)} will arrive until this has been invoked.
     */
    void transmit();
}
