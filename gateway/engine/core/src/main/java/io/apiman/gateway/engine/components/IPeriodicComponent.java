/*
 * Copyright 2015 JBoss Inc
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

import io.apiman.gateway.engine.async.IAsyncHandler;

/**
 * Simple timers, either periodic ({@link #setPeriodicTimer(long, IAsyncHandler)}) or one-off
 * {@link #setOneshotTimer(long, IAsyncHandler)}. They can be cancelled using the returned id (via method call
 * or handler). Cancellations must be idempotent.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public interface IPeriodicComponent {

    /**
     * Calls handler every {@code periodMillis} milliseconds after waiting {@code initialDelayMillis} for the
     * first iteration.
     * 
     * @param periodMillis periodic frequency to call handler in delta milliseconds
     * @param initialDelayMillis delta milliseconds to call handler for first iteration
     * @param periodicHandler handler with unique timer ID
     */
    int setPeriodicTimer(long periodMillis, long initialDelayMillis, IAsyncHandler<Integer> periodicHandler);

    /**
     * Calls handler only once after the specified {@code long deltaMillis} milliseconds has elapsed.
     * 
     * @param deltaMillis delta milliseconds
     * @param timerHandler handler with unique timer ID
     */
    int setOneshotTimer(long deltaMillis, IAsyncHandler<Integer> timerHandler);

    /**
     * Cancel a timer using its ID. This deschedules any executions pending for that ID, including those that
     * have yet to run. Operation is idempotent.
     * 
     * @param timerId Unique ID of timer to be cancelled
     */
    void cancelTimer(int timerId);
    
    /**
     * Cancel all timers.
     */
    void cancelAll();
}
