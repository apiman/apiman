/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.engine.vertx.polling.fetchers;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

/**
 * <p>
 * Interface for asynchronously fetching simple remote resources, for instance
 * via HTTP or local file-system.
 * </p>
 * <p>
 * Any exceptions are returned via {@link #exceptionHandler(Handler)}, which
 * the user should set if they are interested in handling errors.
 * </p>
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public interface ResourceFetcher {
    /**
     * Fetch a resource and return its content via the resultHandler
     *
     * @param resultHandler the result handler
     */
    void fetch(Handler<Buffer> resultHandler);

    /**
     * Set an exception handler, invoked in an error occurs.
     *
     * @param exceptionHandler the exception handler
     * @return fluent
     */
    ResourceFetcher exceptionHandler(Handler<Throwable> exceptionHandler);
}
