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
package io.apiman.gateway.platforms.servlet.components;

import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.platforms.servlet.io.ByteBuffer;

/**
 * Implementation of {@link IBufferFactoryComponent} for servlets.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class BufferFactoryComponentImpl implements IBufferFactoryComponent {

    /**
     * @see io.apiman.gateway.engine.components.IBufferFactoryComponent#createBuffer()
     */
    @Override
    public IApimanBuffer createBuffer() {
        return new ByteBuffer(30);
    }

    /**
     * @see io.apiman.gateway.engine.components.IBufferFactoryComponent#createBuffer(java.lang.String)
     */
    @Override
    public IApimanBuffer createBuffer(String stringData) {
        return new ByteBuffer(stringData);
    }

    /**
     * @see io.apiman.gateway.engine.components.IBufferFactoryComponent#createBuffer(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public IApimanBuffer createBuffer(String stringData, String enc) {
        return new ByteBuffer(stringData, enc);
    }

    /**
     * @see io.apiman.gateway.engine.components.IBufferFactoryComponent#createBuffer(byte[])
     */
    @Override
    public IApimanBuffer createBuffer(byte[] byteData) {
        return new ByteBuffer(byteData);
    }

    /**
     * @see io.apiman.gateway.engine.components.IBufferFactoryComponent#createBuffer(int)
     */
    @Override
    public IApimanBuffer createBuffer(int sizeHint) {
        return new ByteBuffer(sizeHint);
    }
}
