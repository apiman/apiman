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

import java.io.UnsupportedEncodingException;

/**
 * A generic buffer used throughout APIMan, principally for streaming body
 * chunks.
 *
 * Each implementing platform should attempt to implement this in a manner which
 * is as efficient as practicable.
 *
 * Heavily inspired by <a href="http://www.vertx.io">Vert.x</a>'s Buffer
 * interface, which serves an almost identical purpose.
 *
 * Implementors are advised to use {@link #getNativeBuffer()} where possible to
 * reduce repackaging overhead.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public interface IApimanBuffer {

    /**
     * Get encapsulated native buffer.
     *
     * @return Native buffer.
     */
    Object getNativeBuffer();

    /**
     * @return Byte length of buffer.
     */
    int length();

    // ApimanBuffer

    /**
     * Inserts data at given index with buffer.
     *
     * @param index
     * @param buffer
     */
    void insert(int index, IApimanBuffer buffer);

    /**
     * Inserts data at the given index with buffer at given offset and constrained length.
     *
     * @param index ApimanBuffer index
     * @param buffer Provided buffer
     * @param offset Provided buffer offset index
     * @param length Provided buffer maximum length to set
     */
    void insert(int index, IApimanBuffer buffer, int offset, int length);

    /**
     * @param buffer Buffer to append
     */
    void append(IApimanBuffer buffer);

    /**
     * @param buffer Buffer to append
     * @param offset Provided buffer offset
     * @param length Provided buffer maximum length to append
     */
    void append(IApimanBuffer buffer, int offset, int length);

    // Byte

    /**
     * @param index Index of byte to retrieve
     * @return byte Value of byte at index
     * @throws IndexOutOfBoundsException
     */
    byte get(int index);

    /**
     * @param index Index of byte to set
     * @param b Byte value to set
     */
    void set(int index, byte b);

    /**
     * @param b Byte value to append
     */
    void append(byte b);

    // Byte[]

    /**
     * @return ApimanBuffer as byte array.
     */
    byte[] getBytes();

    /**
     * Get ApimanBuffer as byte array constrained by indices.
     *
     * @param start Start index inclusive
     * @param end End index exclusive
     * @return ApimanBuffer as byte array
     */
    byte[] getBytes(int start, int end);

    /**
     * Inserts data from byte array into the index location.
     *
     * @param index Start index
     * @param b Byte array to set
     */
    void insert(int index, byte[] b);

    /**
     * Inserts data from the byte array constrained by offset and length.
     *
     * @param index ApimanBuffer start index
     * @param b Byte Array to set
     * @param offset Byte array start index
     * @param length Byte array maximum length to append
     */
    void insert(int index, byte[] b, int offset, int length);

    /**
     * @param bytes Byte array to append.
     */
    void append(byte[] bytes);

    /**
     * Byte array to append with given offset and length.
     *
     * @param bytes Byte array to append
     * @param offset Byte array start index
     * @param length Maximum length to append
     */
    void append(byte[] bytes, int offset, int length);

    // String

    /**
     * @param start start index
     * @param end end index
     * @return String constrained by start and end indices
     */
    String getString(int start, int end);

    /**
     * @param start start index
     * @param end end index
     * @param encoding string encoding
     * @return String constrained by start and end indices
     * @throws UnsupportedEncodingException
     */
    String getString(int start, int end, String encoding) throws UnsupportedEncodingException;

    /**
     * Inserts data at index from value of string.
     *
     * @param index ApimanBuffer index
     * @param string String to set as bytes
     */
    void insert(int index, String string);

    /**
     * Inserts data at index from value of string.
     *
     * @param index ApimanBuffer index
     * @param string String to set as bytes
     * @param encoding Encoding of string
     * @throws UnsupportedEncodingException
     */
    void insert(int index, String string, String encoding) throws UnsupportedEncodingException;

    /**
     * @param string String to append
     */
    void append(String string);

    /**
     * @param string String to append
     * @param encoding Encoding of string
     * @throws UnsupportedEncodingException
     */
    void append(String string, String encoding) throws UnsupportedEncodingException;

    /**
     * @return String representation of string.
     */
    @Override
    String toString();

    /**
     * @param encoding Encoding of string
     * @return String ApimanBuffer as string of given encoding.
     * @throws UnsupportedEncodingException
     */
    String toString(String encoding) throws UnsupportedEncodingException;
}
