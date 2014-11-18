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
package org.overlord.apiman.rt.gateway.servlet.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.overlord.apiman.rt.engine.io.IBuffer;

/**
 * A simple {@link IBuffer} from a byte array.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls") // TODO finish the implementation of this class!
public class ByteBuffer implements IBuffer {

    private byte [] buffer;
    private int bytesInBuffer = 0;
    
    /**
     * Constructor.
     */
    public ByteBuffer(int size) {
        setBuffer(new byte[size]);
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#getNativeBuffer()
     */
    @Override
    public Object getNativeBuffer() {
        return buffer;
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#length()
     */
    @Override
    public int length() {
        return bytesInBuffer;
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#set(int, org.overlord.apiman.rt.engine.io.IBuffer)
     */
    @Override
    public void set(int index, IBuffer buffer) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#set(int, org.overlord.apiman.rt.engine.io.IBuffer, int, int)
     */
    @Override
    public void set(int index, IBuffer buffer, int offset, int length) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#append(org.overlord.apiman.rt.engine.io.IBuffer)
     */
    @Override
    public void append(IBuffer buffer) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#append(org.overlord.apiman.rt.engine.io.IBuffer, int, int)
     */
    @Override
    public void append(IBuffer buffer, int offset, int length) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#getByte(int)
     */
    @Override
    public byte getByte(int index) {
        return buffer[index];
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#set(int, byte)
     */
    @Override
    public void set(int index, byte b) {
        buffer[index] = b;
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#append(byte)
     */
    @Override
    public void append(byte b) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#getBytes()
     */
    @Override
    public byte[] getBytes() {
        byte [] rval = new byte[bytesInBuffer];
        System.arraycopy(buffer, 0, rval, 0, bytesInBuffer);
        return rval;
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#getBytes(int, int)
     */
    @Override
    public byte[] getBytes(int start, int end) {
        int size = (end - start) - 1;
        byte [] rval = new byte[bytesInBuffer];
        System.arraycopy(buffer, start, rval, 0, size);
        return rval;
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#set(int, byte[])
     */
    @Override
    public void set(int index, byte[] b) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#set(int, byte[], int, int)
     */
    @Override
    public void set(int index, byte[] b, int offset, int length) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#append(byte[])
     */
    @Override
    public void append(byte[] bytes) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#append(byte[], int, int)
     */
    @Override
    public void append(byte[] bytes, int offset, int length) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#getString(int, int)
     */
    @Override
    public String getString(int start, int end) {
        return new String(getBytes(start, end));
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#getString(int, int, java.lang.String)
     */
    @Override
    public String getString(int start, int end, String encoding) {
        try {
            return new String(getBytes(start, end), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#set(int, java.lang.String)
     */
    @Override
    public void set(int index, String string) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#set(int, java.lang.String, java.lang.String)
     */
    @Override
    public void set(int index, String string, String encoding) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#append(java.lang.String)
     */
    @Override
    public void append(String string) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#append(java.lang.String, java.lang.String)
     */
    @Override
    public void append(String string, String encoding) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IBuffer#toString(java.lang.String)
     */
    @Override
    public String toString(String encoding) {
        try {
            return new String(buffer, 0, bytesInBuffer, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new String(buffer, 0, bytesInBuffer);
    }

    /**
     * @return the buffer
     */
    public byte [] getBuffer() {
        return buffer;
    }

    /**
     * @param buffer the buffer to set
     */
    public void setBuffer(byte [] buffer) {
        this.buffer = buffer;
    }

    /**
     * @return the bytesInBuffer
     */
    public int getBytesInBuffer() {
        return bytesInBuffer;
    }
    
    /**
     * Reads from the input stream.
     * @param stream
     * @throws IOException
     */
    public int readFrom(InputStream stream) throws IOException {
        bytesInBuffer = stream.read(buffer);
        return bytesInBuffer;
    }

}
