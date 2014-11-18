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
package io.apiman.gateway.vertx.io;

import org.vertx.java.core.Handler;

/**
 * An object whose main action can be triggered by calling {@link #execute(Object, Handler)}, with a ready
 * {@link Handler} indicating when it is safe to perform an implementation-specified subsequent action. For
 * instance, after writing a head object a stream object is returned via the readyHandler once it is ready to
 * accept data.
 * 
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <H> Head object
 * @param <S> Succeeding object
 */
public interface IReadyExecute<H, S> {
    /**
     * @param head object to execute immediately
     * @param readyHandler called when ready
     */
    void execute(H head, Handler<S> readyHandler);
}
