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
package io.apiman.gateway.platforms.servlet.components;

import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.components.IPeriodicComponent;

/**
 * Servlet implementation of {@link IPeriodicComponent}.
 * 
 * A unique IDs are generated for each timer via a non-blocking {@link AtomicInteger}, with the (unlikely to
 * be achieved) limitation that these will eventually start overwriting once the bounds of {@link Long} are
 * surpassed.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class PeriodicComponentImpl implements IPeriodicComponent {
    private Timer timer = new Timer();
    private ConcurrentMap<Long, TimerTask> timerTaskMap = new ConcurrentHashMap<>();
    private AtomicInteger id = new AtomicInteger(0);

    public PeriodicComponentImpl() {
    }

    /**
     * @see io.apiman.gateway.engine.components.IPeriodicComponent#setPeriodicTimer(long, long,
     *      io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public long setPeriodicTimer(long periodMillis, long initialDelayMillis,
            final IAsyncHandler<Long> periodicHandler) {
        final long timerId = id.incrementAndGet();

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                periodicHandler.handle(timerId);
            }
        };
        // Keep reference to TimerTask and Timer ID we hand back (to cancel later).
        timerTaskMap.put(timerId, task);
        // Task, delay, frequency
        timer.schedule(task, initialDelayMillis, periodMillis);
        return timerId;
    }

    /**
     * @see io.apiman.gateway.engine.components.IPeriodicComponent#setOneshotTimer(long,
     *      io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public long setOneshotTimer(long deltaMillis, final IAsyncHandler<Long> timerHandler) {
        final long timerId = id.incrementAndGet();

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                timerHandler.handle(timerId);
            }
        };
        // Keep reference to TimerTask and Timer ID we hand back (to cancel later).
        timerTaskMap.put(timerId, task);
        // Task, delay, frequency
        timer.schedule(task, deltaMillis);
        return timerId;
    }

    /**
     * @see io.apiman.gateway.engine.components.IPeriodicComponent#cancelTimer(long)
     */
    @Override
    public void cancelTimer(long timerId) {
        timerTaskMap.remove(timerId).cancel();
    }

    /**
     * @see io.apiman.gateway.engine.components.IPeriodicComponent#cancelAll()
     */
    @Override
    public void cancelAll() {
        for (Entry<Long, TimerTask> e : timerTaskMap.entrySet()) {
            timerTaskMap.remove(e).cancel();
        }
    }
}
