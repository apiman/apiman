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
package io.apiman.gateway.platforms.vertx3.components;

import java.util.LinkedHashMap;
import java.util.Map;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.components.IPeriodicComponent;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Vert.x implementation of {@link IPeriodicComponent}.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class PeriodicComponentImpl implements IPeriodicComponent {
    private Vertx vertx;
    private Map<Long, Long> timerMap = new LinkedHashMap<>();
    private long id = 0;

    /**
     * @param vertx the vertx instance
     * @param engineConfig the engine config
     * @param config the config
     */
    public PeriodicComponentImpl(Vertx vertx, VertxEngineConfig engineConfig, Map<String, String> config) {
        this.vertx = vertx;
    }

    /**
     * @see io.apiman.gateway.engine.components.IPeriodicComponent#setPeriodicTimer(long, long,
     *      io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public long setPeriodicTimer(final long periodMillis, final long initialDelayMillis,
            final IAsyncHandler<Long> periodicHandler) {
        // Make our own ID to cope with initial delay feature not being present in vert.x's timers.
        final long timerId = id++;

        // Do the initial delay, then map the next timer back to our ID.
        long vertxId = vertx.setTimer(initialDelayMillis, (Handler<Long>) tid -> {

            long newVertxId = vertx.setPeriodic(periodMillis,  (Handler<Long>) tid2 -> {
                periodicHandler.handle(timerId);
            });

            // Periodic delay - re-map to the new Vert.x timer ID, as original timer ID is now invalid.
            // Vert.x. is single-threaded per-verticle so this should be safe.
            timerMap.put(timerId, newVertxId);
        });

        timerMap.put(timerId, vertxId);
        return timerId;
    }

    /**
     * @see io.apiman.gateway.engine.components.IPeriodicComponent#setOneshotTimer(long,
     *      io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public long setOneshotTimer(long deltaMillis, final IAsyncHandler<Long> timerHandler) {
        final long timerId = id++;

        long vertxTimerId = vertx.setTimer(deltaMillis, (Handler<Long>) tid -> {
            timerHandler.handle(timerId);
        });

        timerMap.put(timerId, vertxTimerId);
        return timerId;
    }

    /**
     * @see io.apiman.gateway.engine.components.IPeriodicComponent#cancelTimer(long)
     */
    @Override
    public void cancelTimer(long timerId) {
        vertx.cancelTimer(timerMap.remove(timerId));
    }

    /**
     * @see io.apiman.gateway.engine.components.IPeriodicComponent#cancelAll()
     */
    @Override
    public void cancelAll() {
        for (long vertxTimerId : timerMap.values()) {
            vertx.cancelTimer(vertxTimerId);
        }
        timerMap.clear();
        id = 0;
    }
}
