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
package io.apiman.gateway.platforms.war.wildfly8.api;

import io.apiman.gateway.api.rest.impl.IPlatform;
import io.apiman.gateway.api.rest.impl.IPlatformAccessor;

/**
 * Platform accessor for wildfly8.
 *
 * @author eric.wittmann@redhat.com
 */
public class Wildfly8PlatformAccessor implements IPlatformAccessor {
    
    private static final Wildfly8Platform platform = new Wildfly8Platform();

    /**
     * @see io.apiman.gateway.api.rest.impl.IPlatformAccessor#getPlatform()
     */
    @Override
    public IPlatform getPlatform() {
        return platform;
    }

}
