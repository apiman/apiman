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

package io.apiman.gateway.engine.components;

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.async.IAsyncFuture;

/**
 * <p>
 * A component to safely allow the execution of blocking code on non-blocking platforms. The precise nature of how this
 * is achieved will vary according to platform-specific implementation. On blocking platforms there is a safe pass-through.
 * </p>
 * <p>NB: This should only be used in cases where the use of blocking code is <em>unavoidable.</em></p>
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface IExecuteBlockingComponent extends IComponent {

    /**
     * <p>
     * Execute blocking code in a safe manner.
     * </p>
     * <p>
     * The caller provides their blocking code inside of an {@link IAsyncHandler}, and uses the
     * {@link IAsyncFuture} to indicate when their blocking code has terminated.
     * </p>
     * <p>
     * The result of the blocking execution is passed asynchronous to the {@link IAsyncResultHandler}
     * resultHandler.
     * </p>
     * <code>
     * <pre>
     *   context.getComponent(IExecuteBlocking.class).executeBlocking(future -> {
     *               Result result = ReallySlowBlockingThing.takesAges();
     *
     *               if (result.successful()) {
     *                   future.completed(result);
     *               } else {
     *                   future.fail(new RuntimeException("It failed :-("));
     *               }
     *
     *           },
     *           result -> {
     *              if (result.isSuccess()) {
     *                  System.out.println(result.getResult());
     *              } else {
     *                  System.out.println(result.getError().getMessage());
     *              }
     *           }
     *   );
     * </pre>
     * </code>
     *
     * @param blockingCode handler to execute blocking code safely.
     * @param resultHandler handler to receive result of blocking code execution.
     */
    <T> void executeBlocking(IAsyncHandler<IAsyncFuture<T>> blockingCode, IAsyncResultHandler<T> resultHandler);
}
