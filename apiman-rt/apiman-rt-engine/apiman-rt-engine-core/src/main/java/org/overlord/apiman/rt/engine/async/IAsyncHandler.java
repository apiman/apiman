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
 * Asynchronous handler called when an async operation completes.
 * 
 * @author Marc Savy <msavy@redhat.com>
 *
 * @param <T> The event to handle
 */
public interface IAsyncHandler<T> {
    
    /**
     * Called when an async result is available.
     * 
     * @param result the async result
     */
    public void handle(IAsyncResult<T> result);
}