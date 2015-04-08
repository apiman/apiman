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
package io.apiman.gateway.engine.async;

/**
 * Models the result of an asynchronous operation in the API Gateway.
 * 
 * @author eric.wittmann@redhat.com
 * @param <T> Result type
 */
public interface IAsyncResult<T> {
    
    /**
     * Whether the call was successful.
     * 
     * @return true on success, false on failure.
     */
    boolean isSuccess();

    /**
     * Whether the call failed.
     * 
     * @return true on failure, false on success.
     */
    boolean isError();

    /**
     * Contains the async call if it succeeded, otherwise null.
     * 
     * @return A result T on success, null when unsuccessful.
     */
    T getResult();

    /**
     * Any unhandled exception raised during the course of execution.
     * 
     * @return A Throwable if an unhandled exception occurred, otherwise null.
     */
    Throwable getError();
}
