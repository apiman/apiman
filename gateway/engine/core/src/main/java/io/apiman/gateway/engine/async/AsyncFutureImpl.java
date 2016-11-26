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
 * @author Marc Savy {@literal <msavy@redhat.com>}
 * @param <T> the contained type
 */
public class AsyncFutureImpl<T> implements IAsyncFuture<T> {
    private IAsyncResultHandler<T> handler;
    private T result = null;
    private Throwable error;
    private boolean succeeded;

    @Override
    public void completed() {
        succeeded = true;
        fireHandler();
    }

    @Override
    public void completed(T result) {
        this.result = result;
        succeeded = true;
        fireHandler();
    }

    @Override
    public void fail(Throwable error) {
        this.error = error;
        succeeded = false;
        fireHandler();
    }

    @Override
    public AsyncFutureImpl<T> setActionHandler(IAsyncResultHandler<T> handler) {
        this.handler = handler;
        return this;
    }

    private void fireHandler() {
        if (handler != null) {
            if (succeeded) {
                handler.handle(AsyncResultImpl.create(result));
            } else {
                handler.handle(AsyncResultImpl.create(error));
            }
        }
    }

    @Override
    public boolean isSuccess() {
        return succeeded;
    }

    @Override
    public boolean isError() {
        return !succeeded;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public Throwable getError() {
        return error;
    }
}
