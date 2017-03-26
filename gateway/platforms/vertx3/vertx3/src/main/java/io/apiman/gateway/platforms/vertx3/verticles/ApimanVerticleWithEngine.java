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
package io.apiman.gateway.platforms.vertx3.verticles;

import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.platforms.vertx3.engine.VertxConfigDrivenEngineFactory;
import io.vertx.core.Future;

/**
 * A base for those verticles that require an instantiated engine.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public abstract class ApimanVerticleWithEngine extends ApimanVerticleBase {

    protected IEngine engine; //FIXME

    @Override
    public void start(Future<Void> startFuture) {
        super.start(startFuture);
        engine = new VertxConfigDrivenEngineFactory(vertx, getEngineConfig())
                .setResultHandler(result -> {
                    if (result.isSuccess()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.getError());
                    }
                }).createEngine();

        engine.getRegistry(); // this should help avoid slow first-time loads.
    }

    protected IEngine engine() {
        return engine;
    }
}
