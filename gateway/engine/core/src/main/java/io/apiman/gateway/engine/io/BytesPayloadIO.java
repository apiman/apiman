/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.gateway.engine.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * Reads/writes a byte buffer.  This is used when we don't know what type
 * the payload might be.
 *
 * @author eric.wittmann@redhat.com
 */
public class BytesPayloadIO implements IPayloadIO<byte[]> {
    
    /**
     * Constructor.
     */
    public BytesPayloadIO() {
    }
    
    /**
     * @see io.apiman.gateway.engine.io.IPayloadIO#unmarshall(java.io.InputStream)
     */
    @Override
    public byte[] unmarshall(InputStream input) throws Exception {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            IOUtils.copy(input, output);
            output.flush();
            return output.toByteArray();
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.io.IPayloadIO#unmarshall(byte[])
     */
    @Override
    public byte[] unmarshall(byte[] input) throws Exception {
        return input;
    }
    
    /**
     * @see io.apiman.gateway.engine.io.IPayloadIO#marshall(java.lang.Object)
     */
    @Override
    public byte[] marshall(byte[] data) throws Exception {
        return data;
    }

}
