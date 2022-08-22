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
import io.apiman.gateway.engine.IPolicyErrorWriter;
import io.apiman.gateway.engine.IPolicyFailureWriter;
import io.apiman.gateway.engine.impl.ConfigDrivenEngineFactory;
import io.apiman.gateway.platforms.vertx3.engine.VertxConfigDrivenEngineFactory;

import java.util.Map;

import io.vertx.core.Promise;

/**
 * A base for those verticles that require an instantiated engine.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public abstract class ApimanVerticleWithEngine extends ApimanVerticleBase {

    protected IEngine engine;
    protected IPolicyFailureWriter policyFailureWriter;
    protected IPolicyErrorWriter policyErrorWriter;

    @Override
    public void start(Promise<Void> startPromise) {
        super.start(startPromise);
        engine = new VertxConfigDrivenEngineFactory(vertx, getEngineConfig())
                .setResultHandler(result -> {
                    if (result.isSuccess()) {
                        startPromise.complete();
                    } else {
                        startPromise.fail(result.getError());
                    }
                }).createEngine();

        policyFailureWriter = initPolicyFailureWriter();
        policyErrorWriter = initPolicyErrorWriter();
    }

    private IPolicyFailureWriter initPolicyFailureWriter() {
        Class<? extends IPolicyFailureWriter> clazz = apimanConfig.getPolicyFailureWriterClass(engine.getPluginRegistry());
        Map<String, String> conf = apimanConfig.getPolicyFailureWriterConfig();
        return ConfigDrivenEngineFactory.instantiate(clazz, conf);
    }

    private IPolicyErrorWriter initPolicyErrorWriter() {
        Class<? extends IPolicyErrorWriter> clazz = apimanConfig.getPolicyErrorWriterClass(engine.getPluginRegistry());
        Map<String, String> conf = apimanConfig.getPolicyErrorWriterConfig();
        return ConfigDrivenEngineFactory.instantiate(clazz, conf);
    }

    protected IEngine engine() {
        return engine;
    }
}
