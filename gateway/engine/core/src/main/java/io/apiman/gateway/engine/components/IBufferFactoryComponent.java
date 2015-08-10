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
package io.apiman.gateway.engine.components;

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.policy.IPolicy;

/**
 * Allows platform-specific buffer objects to be generated. There may be occasions where a {@link IPolicy} may
 * legitimately wish to construct a new buffer (e.g. caching), and therefore needs a mechanism to efficiently
 * get a new {@link IApimanBuffer} buffer with a native implementation.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public interface IBufferFactoryComponent extends IComponent {

    /**
     * Instantiate an {@link IApimanBuffer}.
     *
     * @return A new empty buffer
     */
    IApimanBuffer createBuffer();

    /**
     * Instantiate an {@link IApimanBuffer} of given size.
     *
     * @param size buffer size
     * @return A new empty buffer of size
     */
    IApimanBuffer createBuffer(int size);

    /**
     * Instantiate an {@link IApimanBuffer} with {@link String}.
     *
     * @param stringData string to instantiate buffer
     * @return A buffer instantiated with stringData
     */
    IApimanBuffer createBuffer(String stringData);

    /**
     * Instantiate an {@link IApimanBuffer} with {@link String}.
     *
     * @param stringData string to instantiate buffer
     * @param enc encoding of string
     * @return A buffer instantiated with stringData
     */
    IApimanBuffer createBuffer(String stringData, String enc);

    /**
     * Instantiate an {@link IApimanBuffer} with {@link Byte} array.
     *
     * @param byteData byte array data to instantiate buffer
     * @return Buffer instantiated with byteData
     */
    IApimanBuffer createBuffer(byte[] byteData);

    /**
     * Clone an existing buffer.
     * @param buffer
     */
    IApimanBuffer cloneBuffer(IApimanBuffer buffer);
}
