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
package io.apiman.gateway.engine.policies.rewrite;

import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;

import java.io.UnsupportedEncodingException;

/**
 * Used to rewrite the URLs in a stream of data returned by a back end
 * API.
 *
 * @author eric.wittmann@redhat.com
 */
public class URLRewritingStream extends AbstractStream<ServiceResponse> {

    private ServiceResponse response;
    private String fromRegularExpression;
    private String toReplacement;
    private IBufferFactoryComponent bufferFactory;

    private IApimanBuffer buffer;
    private int position;
    private boolean atEnd;

    /**
     * Constructor.
     *
     * @param bufferFactory
     * @param response
     * @param config
     */
    public URLRewritingStream(IBufferFactoryComponent bufferFactory, ServiceResponse response,
            String fromRegularExpression, String toReplacement) {
        this.bufferFactory = bufferFactory;
        this.response = response;
        this.fromRegularExpression = fromRegularExpression;
        this.toReplacement = toReplacement;
    }

    /**
     * @see io.apiman.gateway.engine.io.IReadStream#getHead()
     */
    @Override
    public ServiceResponse getHead() {
        return response;
    }

    /**
     * @see io.apiman.gateway.engine.io.AbstractStream#handleHead(java.lang.Object)
     */
    @Override
    protected void handleHead(ServiceResponse head) {
        // Nothing to do with the head.
    }

    /**
     * @see io.apiman.gateway.engine.io.AbstractStream#write(io.apiman.gateway.engine.io.IApimanBuffer)
     */
    @Override
    public void write(IApimanBuffer chunk) {
        if (buffer == null) {
            buffer = bufferFactory.cloneBuffer(chunk);
        } else {
            buffer.append(chunk);
        }
        atEnd = false;
        processBuffer();
    }

    /**
     * Scan the buffer for possible URLs.  As they are found, extract them from the buffer,
     * optionally translate them (only if they match the regular expression), and then
     * send along the translated version.  If a potential URL match is found but its end
     * spans the end of the buffer, just wait for the next chunk of data!
     */
    private void processBuffer() {
        if (buffer == null || buffer.length() == 0) {
            return;
        }

        position = 0;
        int maxPos = buffer.length() - 1;
        boolean done = false;
        IApimanBuffer originalBuffer = buffer;
        int bytesConsumed = 0;
        int preUrlFromPos = 0;
        int urlsFound = 0;
        while (!done) {
            if (originalBuffer.get(position) == (byte) 'h' || originalBuffer.get(position) == (byte) 'H') {
                if (isURLStart()) {
                    urlsFound++;
                    // Write everything up to this point - consider it "consumed"
                    if (position > 0) {
                        IApimanBuffer preUrlData = bufferFactory.createBuffer(originalBuffer.getBytes(preUrlFromPos, position));
                        super.write(preUrlData);
                        bytesConsumed = position;
                    }

                    // Now consume the URL
                    int originalPos = position;
                    String url = consumeURL();

                    // URL successfully read from the buffer?  If not, it was probably because we hit
                    // the end of the chunk and need to wait for more data
                    if (url != null) {
                        url = rewriteURL(url);
                        IApimanBuffer urlBuffer;
                        if (url != null) {
                            urlBuffer = bufferFactory.createBuffer(url, "UTF-8"); //$NON-NLS-1$
                        } else {
                            urlBuffer = bufferFactory.createBuffer(originalBuffer.getBytes(originalPos, position));
                        }
                        super.write(urlBuffer);
                        bytesConsumed = position;
                        preUrlFromPos = position;
                    } else {
                        done = true;
                    }
                } else {
                    position++;
                }
            } else {
                position++;
            }

            if (position > maxPos) {
                done = true;
            }
        }

        // What do we do with what's left?  If there *is* anything left!
        if (urlsFound == 0) {
            super.write(originalBuffer);
            buffer = null;
        } else {
            buffer = bufferFactory.createBuffer(originalBuffer.getBytes(bytesConsumed, maxPos + 1));
            if (atEnd) {
                super.write(buffer);
            }
        }

    }

    /**
     * @return the URL consumed from the buffer
     */
    private String consumeURL() {
        String rval = null;

        int pos = position + "http://".length(); //$NON-NLS-1$
        while (pos < buffer.length() && isValidURLChar(pos)) {
            pos++;
        }

        if (pos < buffer.length() || atEnd) {
            try {
                rval = buffer.getString(position, pos, "UTF-8"); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            position = pos;
        }
        return rval;
    }

    /**
     * @param pos the position to check
     * @return true if the character at position 'pos' is a valid URL character
     */
    private boolean isValidURLChar(int pos) {
        byte b = buffer.get(pos);
        char ch = (char) b;

        // Valid URL characters as defined here:  http://tools.ietf.org/html/rfc3986#section-2
        return Character.isDigit(ch) || Character.isLetter(ch) || ch == '-' || ch == '.' || ch == '_'
                || ch == '~' || ch == ':' || ch == '/' || ch == '?' || ch == '#' || ch == '[' || ch == ']'
                || ch == '@' || ch == '!' || ch == '$' || ch == '&' || ch == '\'' || ch == '(' || ch == ')'
                || ch == '*' || ch == '+' || ch == ',' || ch == ';' || ch == '=';
    }

    /**
     * @param url the URL to rewrite
     * @return a rewritten URL
     */
    private String rewriteURL(String url) {
        String fromUrl = decodeURL(url);
        String toUrl = fromUrl.replaceFirst(this.fromRegularExpression, this.toReplacement);
        return encodeURL(toUrl);
    }

    /**
     * @param url the URL to decode
     * @return the decoded URL
     */
    private String decodeURL(String url) {
        // Not yet supported - unsure yet whether we want to perform translation on the
        // raw URL or instead first decode it.  Re-encoding it is tricky, because the
        // default java URL encoder is pretty aggressive - e.g. it will turn
        // http://apiman.io into http%3A%2F%2Fapiman.io - when we really don't want that.
        return url;
    }

    /**
     * @param toUrl the URL to encode
     * @return the encoded URL
     */
    private String encodeURL(String toUrl) {
        // See comment in decodeURL()
        return toUrl;
    }

    /**
     * @return true if the current position in the buffer points to a URL
     */
    private boolean isURLStart() {
        // The 'h' in https?:// already matched - need to match the rest of it
        boolean isStart = true;

        int pos = position + 1;

        // The 't' in http://
        if (pos < buffer.length()) {
            isStart &= buffer.get(pos) == (byte) 't' || buffer.get(pos) == (byte) 'T';
        }
        pos++;

        // The 't' in http://
        if (pos < buffer.length()) {
            isStart &= buffer.get(pos) == (byte) 't' || buffer.get(pos) == (byte) 'T';
        }
        pos++;

        // The 'p' in http://
        if (pos < buffer.length()) {
            isStart &= buffer.get(pos) == (byte) 'p' || buffer.get(pos) == (byte) 'P';
        }
        pos++;

        // The (optional) 's' in https://
        if (pos < buffer.length()) {
            boolean isSecure = buffer.get(pos) == (byte) 's' || buffer.get(pos) == (byte) 'S';
            if (isSecure) {
                pos++;
            }
        }

        // The ':' in http://
        if (pos < buffer.length()) {
            isStart &= buffer.get(pos) == (byte) ':';
        }
        pos++;

        // The '/' in http://
        if (pos < buffer.length()) {
            isStart &= buffer.get(pos) == (byte) '/';
        }
        pos++;

        // The '/' in http://
        if (pos < buffer.length()) {
            isStart &= buffer.get(pos) == (byte) '/';
        }
        pos++;

        return isStart;
    }

    /**
     * @see io.apiman.gateway.engine.io.AbstractStream#end()
     */
    @Override
    public void end() {
        atEnd = true;
        processBuffer();
        super.end();
    }

}
