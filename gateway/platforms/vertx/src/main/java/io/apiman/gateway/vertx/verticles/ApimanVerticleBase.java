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

import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.apiman.gateway.vertx.i18n.Messages;

import java.util.UUID;

import org.vertx.java.busmods.BusModBase;

/**
 * Standard base for all Apiman verticles.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public abstract class ApimanVerticleBase extends BusModBase {

    protected VertxEngineConfig amanConfig;
    protected String uuid;

    @Override
    public void start() {
        super.start();
        amanConfig = getEngineConfig();
        
        // If someone provides a UUID we use it, else generate one.
        if(config.containsField("uuid")) { //$NON-NLS-1$
            uuid = config.getString("uuid"); //$NON-NLS-1$
        } else {
            uuid = UUID.randomUUID().toString();
        }

        logger.info(Messages.getString("ApimanVerticleBase.0") + this.getClass().getName() + "\n" +   //$NON-NLS-1$//$NON-NLS-2$
                Messages.getString("ApimanVerticleBase.1") + verticleType() + "\n" +  //$NON-NLS-1$//$NON-NLS-2$
                Messages.getString("ApimanVerticleBase.2") + uuid + "\n");  //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * Maps to config.
     * @return Verticle's type
     */
    public abstract String verticleType();
    
    // Override this for verticle specific config & testing.
    protected VertxEngineConfig getEngineConfig() {
       return new VertxEngineConfig(config); 
    }
}
