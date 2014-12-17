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
package io.apiman.gateway.vertx.verticles;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * Start the platform.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class InitializerVerticle extends ApimanVerticleBase {

    @Override
    public void start() {
        super.start();
        
        container.deployVerticle(HttpApiVerticle.class.getCanonicalName(), container.config(), new Handler<AsyncResult<String>>() {
            
            @Override
            public void handle(AsyncResult<String> event) {
                container.deployVerticle(HttpDispatcherVerticle.class.getCanonicalName(), container.config());
                container.deployVerticle(HttpGatewayVerticle.class.getCanonicalName(), container.config(), new Handler<AsyncResult<String>>() {

                    @Override
                    public void handle(AsyncResult<String> result) {
                        container.deployVerticle(PolicyVerticle.class.getCanonicalName(), container.config(), 1);
                    }
                });
            }
        });
    }

    @Override
    public String verticleType() {
        return "initialiser"; //$NON-NLS-1$
    }  
}
