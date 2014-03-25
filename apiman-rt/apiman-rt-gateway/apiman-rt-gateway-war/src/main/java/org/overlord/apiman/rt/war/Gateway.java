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
package org.overlord.apiman.rt.war;

import org.overlord.apiman.rt.engine.EngineFactory;
import org.overlord.apiman.rt.engine.IEngine;

/**
 * Top level gateway.  Used when the API Management Runtime Engine is being used
 * in a standard Web Application gateway scenario.
 *
 * @author eric.wittmann@redhat.com
 */
public class Gateway {
    
    public static IEngine engine;

    /**
     * Initialize the gateway.
     */
    public static void init() {
        engine = EngineFactory.createEngine();
    }

    /**
     * Shuts down the gateway.
     */
    public static void shutdown() {
        engine = null;
    }

}
