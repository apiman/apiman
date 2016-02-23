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
package io.apiman.gateway.api.rest.impl;

import io.apiman.gateway.engine.IEngine;

/**
 * Simple interface used by the REST implementation layer to get access
 * to the current in-scope API Management runtime engine.
 *
 * @author eric.wittmann@redhat.com
 */
@FunctionalInterface
public interface IEngineAccessor {
    
    /**
     * Gets the current engine.
     * @return the engine
     */
    public IEngine getEngine();

}
