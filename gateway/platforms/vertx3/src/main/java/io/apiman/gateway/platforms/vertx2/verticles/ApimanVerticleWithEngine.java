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
package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.platforms.vertx2.engine.VertxConfigDrivenEngineFactory;

/**
 * A base for those verticles that require an instantiated engine.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public abstract class ApimanVerticleWithEngine extends ApimanVerticleBase {

    protected IEngine engine;
    //protected ApiListener apiListener;

    @Override
    public void start() {
        super.start();

        engine = new VertxConfigDrivenEngineFactory(vertx, getEngineConfig()).createEngine();
        engine.getRegistry(); // this should help avoid slow first-time loads.

        //System.out.println("After");
        //apiListener = new ApiListener(eb, uuid);
        //apiListener.listen(engine);
    }

    protected IEngine engine() {
        return engine;
    }
}
