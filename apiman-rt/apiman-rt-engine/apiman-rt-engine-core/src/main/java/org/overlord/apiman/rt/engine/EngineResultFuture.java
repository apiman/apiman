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
package org.overlord.apiman.rt.engine;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.async.IAsyncResult;

/**
 * A {@link Future} implementation for getting the engine result.  This basically
 * adapts the async engine implementation of "execute" into a standard {@link Future}
 * object.
 *
 * @author eric.wittmann@redhat.com
 */
public class EngineResultFuture implements Future<IAsyncResult<EngineResult>>, IAsyncHandler<EngineResult> {
    
    private CountDownLatch latch = new CountDownLatch(1);
    private IAsyncResult<EngineResult> asyncResult = null;
    
    /**
     * Constructor.
     */
    public EngineResultFuture() {
    }

    /**
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new RuntimeException("Not supported."); //$NON-NLS-1$
    }

    /**
     * @see java.util.concurrent.Future#isCancelled()
     */
    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * @see java.util.concurrent.Future#isDone()
     */
    @Override
    public boolean isDone() {
        return asyncResult != null;
    }

    /**
     * @see java.util.concurrent.Future#get()
     */
    @Override
    public IAsyncResult<EngineResult> get() throws InterruptedException, ExecutionException {
        latch.await();
        return asyncResult;
    }

    /**
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public IAsyncResult<EngineResult> get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        return asyncResult;
    }

    /**
     * @see org.overlord.apiman.rt.engine.async.IAsyncHandler#handle(org.overlord.apiman.rt.engine.async.IAsyncResult)
     */
    @Override
    public void handle(IAsyncResult<EngineResult> result) {
        this.asyncResult = result;
        this.latch.countDown();
    }

}
