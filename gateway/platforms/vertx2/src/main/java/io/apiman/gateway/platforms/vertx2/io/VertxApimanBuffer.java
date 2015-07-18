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
package io.apiman.gateway.platforms.vertx2.io;

import io.apiman.gateway.engine.io.IApimanBuffer;
import io.vertx.core.buffer.Buffer;

import java.io.UnsupportedEncodingException;

/**
 * An {@link IApimanBuffer} implementation that wraps Vert.x's native {@link Buffer} format.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class VertxApimanBuffer implements IApimanBuffer {

    private Buffer nativeBuffer;

    public VertxApimanBuffer() {
        this.nativeBuffer = Buffer.buffer();
    }

    public VertxApimanBuffer(Buffer nativeBuffer) {
        this.nativeBuffer = nativeBuffer;
    }

    public VertxApimanBuffer(String stringData) {
        this.nativeBuffer = Buffer.buffer(stringData);
    }

    public VertxApimanBuffer(String stringData, String enc) {
        this.nativeBuffer = Buffer.buffer(stringData, enc);
    }

    public VertxApimanBuffer(byte[] byteData) {
        this.nativeBuffer = Buffer.buffer(byteData);
    }

    public VertxApimanBuffer(int sizeHint) {
        this.nativeBuffer = Buffer.buffer(sizeHint);
    }

    @Override
    public Object getNativeBuffer() {
        return nativeBuffer;
    }

    @Override
    public int length() {
        return nativeBuffer.length();
    }

    @Override
    public void insert(int index, IApimanBuffer buffer) {
       nativeBuffer.setBuffer(index, (Buffer) buffer.getNativeBuffer());
    }

    @Override
    public void insert(int index, IApimanBuffer buffer, int offset, int length) {
        nativeBuffer.setBuffer(index, (Buffer) buffer, offset, length);
    }

    @Override
    public void append(IApimanBuffer buffer) {
        nativeBuffer.appendBuffer((Buffer) buffer);
    }

    @Override
    public void append(IApimanBuffer buffer, int offset, int length) {
        nativeBuffer.appendBuffer((Buffer) buffer, offset, length);
    }

    @Override
    public byte get(int index) {
        return nativeBuffer.getByte(index);
    }

    @Override
    public void set(int index, byte b) {
        nativeBuffer.setByte(index, b);
    }

    @Override
    public void append(byte b) {
        nativeBuffer.appendByte(b);
    }

    @Override
    public byte[] getBytes() {
        return nativeBuffer.getBytes();
    }

    @Override
    public byte[] getBytes(int start, int end) {
        return nativeBuffer.getBytes(start, end);
    }

    @Override
    public void insert(int index, byte[] b) {
        nativeBuffer.setBytes(index, b);
    }

    @Override
    public void insert(int index, byte[] b, int offset, int length) {
        nativeBuffer.setBytes(index, b, offset, length);
    }

    @Override
    public void append(byte[] bytes) {
        nativeBuffer.appendBytes(bytes);
    }

    @Override
    public void append(byte[] bytes, int offset, int length) {
        nativeBuffer.appendBytes(bytes, offset, length);
    }

    @Override
    public String getString(int start, int end) {
        return nativeBuffer.getString(start, end);
    }

    @Override
    public String getString(int start, int end, String encoding) throws UnsupportedEncodingException {
        return nativeBuffer.getString(start, end, encoding);
    }

    @Override
    public void insert(int index, String string) {
        nativeBuffer.setString(index, string);
    }

    @Override
    public void insert(int index, String string, String encoding) throws UnsupportedEncodingException {
        nativeBuffer.setString(index, string, encoding);
    }

    @Override
    public void append(String string) {
        nativeBuffer.appendString(string);
    }

    @Override
    public void append(String string, String encoding) throws UnsupportedEncodingException {
        nativeBuffer.appendString(string, encoding);
    }

    @Override
    public String toString(String encoding) throws UnsupportedEncodingException {
        return nativeBuffer.toString(encoding);
    }

    @Override
    public String toString() {
        return nativeBuffer.toString();
    }
}
