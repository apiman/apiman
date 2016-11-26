/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.gateway.engine.async;

/**
 * A simple future interface that allows the indication of an event's completion or failure.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 * @param <T> Type future contained type.
 */
public interface IAsyncFuture<T> extends IAsyncResult<T> {

    /**
     * Indicate completion
     */
    void completed();

    /**
     * Indicate completion and set result
     *
     * @param result the result to set
     */
    void completed(T result);

    /**
     * Indicate failure and set error
     *
     * @param t the throwable to set
     */
    void fail(Throwable t);

    /**
     * Set the action handler to invoke when the future indicates completion or failure.
     *
     * @param handler the handler invoked on completion or failure
     * @return this future
     */
    IAsyncFuture<T> setActionHandler(IAsyncResultHandler<T> handler);

    /**
     * Create an empty future.
     *
     * @return the new future
     */
    static <T> IAsyncFuture<T> create() {
        return new AsyncFutureImpl<>();
    }
}
