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
package io.apiman.gateway.engine.io;

import io.apiman.gateway.engine.components.IBufferFactoryComponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * A simple {@link IApimanBuffer} from a byte array.  Don't use this class
 * directly - create and manage buffers by using the {@link IBufferFactoryComponent}
 * instead!  The factory will give you a platform-specific buffer implementation.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls") // TODO finish the implementation of this class!
public class ByteBuffer implements IApimanBuffer {

    private byte [] buffer;
    private int bytesInBuffer = 0;

    /**
     * Constructor.
     *
     * @param size initial size
     */
    public ByteBuffer(int size) {
        buffer = new byte[size];
    }

    /**
     * Constructor.
     *
     * @param stringData String data
     */
    public ByteBuffer(String stringData) {
        buffer = stringData.getBytes();
        bytesInBuffer = buffer.length;
    }

    /**
     * Constructor.
     *
     * @param stringData String data
     * @param enc String data encoding
     */
    public ByteBuffer(String stringData, String enc) {
        try {
            buffer = stringData.getBytes(enc);
            bytesInBuffer = buffer.length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param byteData the byte data to initialize buffer
     */
    public ByteBuffer(byte[] byteData) {
        buffer = Arrays.copyOf(byteData, byteData.length);
        bytesInBuffer = byteData.length;
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#getNativeBuffer()
     */
    @Override
    public Object getNativeBuffer() {
        return buffer;
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#length()
     */
    @Override
    public int length() {
        return bytesInBuffer;
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#insert(int, io.apiman.gateway.engine.io.IApimanBuffer)
     */
    @Override
    public void insert(int index, IApimanBuffer buffer) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#insert(int, io.apiman.gateway.engine.io.IApimanBuffer, int, int)
     */
    @Override
    public void insert(int index, IApimanBuffer buffer, int offset, int length) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#append(io.apiman.gateway.engine.io.IApimanBuffer)
     */
    @Override
    public void append(IApimanBuffer buffer) {
        append(buffer, 0, buffer.length());
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#append(io.apiman.gateway.engine.io.IApimanBuffer, int, int)
     */
    @Override
    public void append(IApimanBuffer buffer, int offset, int length) {
        int sizeToAppend = length;
        int newBufferSize = this.bytesInBuffer + sizeToAppend;
        if (this.buffer.length >= newBufferSize) {
            System.arraycopy(buffer.getBytes(), offset, this.buffer, bytesInBuffer, sizeToAppend);
        } else {
            byte [] newBuffer = new byte[newBufferSize];
            System.arraycopy(this.buffer, 0, newBuffer, 0, bytesInBuffer);
            System.arraycopy(buffer.getBytes(), offset, newBuffer, bytesInBuffer, sizeToAppend);
            this.buffer = newBuffer;
        }
        this.bytesInBuffer = newBufferSize;
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#get(int)
     */
    @Override
    public byte get(int index) {
        return buffer[index];
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#set(int, byte)
     */
    @Override
    public void set(int index, byte b) {
        buffer[index] = b;
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#append(byte)
     */
    @Override
    public void append(byte b) {
        byte [] bytes = new byte[1];
        bytes[0] = b;
        append(bytes);
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#getBytes()
     */
    @Override
    public byte[] getBytes() {
        byte [] rval = new byte[bytesInBuffer];
        System.arraycopy(buffer, 0, rval, 0, bytesInBuffer);
        return rval;
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#getBytes(int, int)
     */
    @Override
    public byte[] getBytes(int start, int end) {
        int size = (end - start) - 1;
        byte [] rval = new byte[bytesInBuffer];
        System.arraycopy(buffer, start, rval, 0, size);
        return rval;
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#insert(int, byte[])
     */
    @Override
    public void insert(int index, byte[] b) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#insert(int, byte[], int, int)
     */
    @Override
    public void insert(int index, byte[] b, int offset, int length) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#append(byte[])
     */
    @Override
    public void append(byte[] bytes) {
        int requiredBytes = bytesInBuffer + bytes.length;
        if (requiredBytes > buffer.length) {
            byte [] oldbuffer = buffer;
            buffer = new byte[requiredBytes];
            System.arraycopy(oldbuffer, 0, buffer, 0, bytesInBuffer);
        }
        System.arraycopy(bytes, 0, buffer, bytesInBuffer, bytes.length);
        bytesInBuffer = requiredBytes;
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#append(byte[], int, int)
     */
    @Override
    public void append(byte[] bytes, int offset, int length) {
        int requiredBytes = bytesInBuffer + length;
        if (requiredBytes > buffer.length) {
            byte [] oldbuffer = buffer;
            buffer = new byte[requiredBytes];
            System.arraycopy(oldbuffer, 0, buffer, 0, bytesInBuffer);
        }
        System.arraycopy(bytes, offset, buffer, bytesInBuffer, length);
        bytesInBuffer = requiredBytes;
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#getString(int, int)
     */
    @Override
    public String getString(int start, int end) {
        return new String(getBytes(start, end));
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#getString(int, int, java.lang.String)
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
     * @see io.apiman.gateway.engine.io.IApimanBuffer#insert(int, java.lang.String)
     */
    @Override
    public void insert(int index, String string) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#insert(int, java.lang.String, java.lang.String)
     */
    @Override
    public void insert(int index, String string, String encoding) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#append(java.lang.String)
     */
    @Override
    public void append(String string) {
        byte[] bytes = string.getBytes();
        append(bytes);
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#append(java.lang.String, java.lang.String)
     */
    @Override
    public void append(String string, String encoding) throws UnsupportedEncodingException {
        byte [] bytes = string.getBytes(encoding);
        append(bytes);
    }

    /**
     * @see io.apiman.gateway.engine.io.IApimanBuffer#toString(java.lang.String)
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
     * @return the bytesInBuffer
     */
    public int getBytesInBuffer() {
        return bytesInBuffer;
    }

    /**
     * Reads from the input stream.
     * @param stream the input stream to read from
     * @throws IOException I/O error when reading from buffer
     * @return bytes read from buffer
     */
    public int readFrom(InputStream stream) throws IOException {
        bytesInBuffer = stream.read(buffer);
        return bytesInBuffer;
    }

}
