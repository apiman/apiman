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

import org.overlord.apiman.rt.api.rest.impl.IEngineAccessor;
import org.overlord.apiman.rt.engine.IEngine;

/**
 * Accessor for the engine - allows the REST implementation to get
 * access to the engine created by the web app bootstrapper.
 *
 * @author eric.wittmann@redhat.com
 */
public class WarGatewayEngineAccessor implements IEngineAccessor {

    /**
     * @see org.overlord.apiman.rt.api.rest.impl.IEngineAccessor#getEngine()
     */
    @Override
    public IEngine getEngine() {
        return WarGateway.engine;
    }

}
