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
package org.overlord.apiman.rt.engine.async;

/**
 * @author Marc Savy <msavy@redhat.com>
 * @param <T>
 *
 */
public class AsyncResultImpl<T> implements IAsyncResult<T> {
    private T result;
    private Throwable error;
    private boolean success;
    
    /**
     * A successful async call.
     * @param result the result of the async call.
     */
    public AsyncResultImpl(T result) {
        this.result = result;
        success = true;
    }
    
    /**
     * An unsuccessful async call.
     * @param error the Throwable raised when error occurred.
     */
    public AsyncResultImpl(Throwable error) {
        this.error = error;
        success = false;
    }
    
    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public boolean isError() {
        return success == false; 
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
