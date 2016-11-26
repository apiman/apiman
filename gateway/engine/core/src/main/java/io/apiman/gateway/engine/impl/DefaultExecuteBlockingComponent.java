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

package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.async.IAsyncFuture;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.IExecuteBlockingComponent;

/**
 * Default implementation just passes through (i.e. assumes blocking execution pattern).
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultExecuteBlockingComponent implements IExecuteBlockingComponent {

    @Override
    public <T> void executeBlocking(IAsyncHandler<IAsyncFuture<T>> blockingCode, IAsyncResultHandler<T> resultHandler) {
        IAsyncFuture<T> wrapped = passthrough(resultHandler);
        try {
            blockingCode.handle(wrapped);
        } catch (Exception e) {
            wrapped.fail(e);
        }
    }

    private <T> IAsyncFuture<T> passthrough(IAsyncResultHandler<T> resultHandler) {
        return IAsyncFuture.<T>create().setActionHandler(resultHandler::handle);
    }
}
