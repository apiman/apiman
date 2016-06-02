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

package io.apiman.gateway.engine.soap;

import io.apiman.gateway.engine.io.IApimanBuffer;

import java.io.UnsupportedEncodingException;

/**
 * Used to scan the first part of the soap envelope, looking for the section
 * that defines the (optional) soap headers.  If the soap headers are not
 * found within a reasonable number of bytes read, then an error is thrown.
 * Typically that would indicate that the content doesn't actually contain
 * a soap payload.
 * 
 * An example of a SOAP Message is:
 * 
 * <pre>
 * &lt;?xml version="1.0"?>
 * &lt;soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
 *   &lt;soap:Header>
 *     &lt;ns1:SomeHeader xmlns:ns1="uri:namespace1">Value&lt;/ns1:SomeHeader>
 *   &lt;/soap:Header>
 *   &lt;soap:Body>
 *     &lt;m:GetStockPrice xmlns:m="http://www.example.org/stock/">
 *       &lt;m:StockName>RHT&lt;/m:StockName>
 *     &lt;/m:GetStockPrice>
 *   &lt;/soap:Body>
 * &lt;/soap:Envelope>
 * </pre>
 * 
 * @author eric.wittmann@gmail.com
 */
public class SoapHeaderScanner {
    
    private static final int MAX_BUFFER = 1024 * 4; // 4k default max-buffer size

    private IApimanBuffer buffer;
    private int maxBufferLength = MAX_BUFFER;
    private ByteRange xmlPreamble = new ByteRange();
    private ByteRange envelopeDecl = new ByteRange();
    private ByteRange headers = new ByteRange();

    /**
     * Constructor.
     */
    public SoapHeaderScanner() {
    }
    
    /**
     * Append the given data to any existing buffer, then scan the buffer
     * looking for the soap headers.  If scanning is complete, this method
     * will return true.  If more data is required, then the method will return
     * false.  If an error condition is detected, then an exception will be
     * thrown.
     * @param buffer
     */
    public boolean scan(IApimanBuffer buffer) throws SoapEnvelopeNotFoundException {
        if (this.buffer == null) {
            this.buffer = buffer;
        } else {
            this.buffer.append(buffer);
        }
        boolean scanComplete = doScan();
        // If our buffer is already "max size" but we haven't found the start of the
        // soap envelope yet, then we're likely not going to find it.
        if (!scanComplete && this.buffer.length() >= getMaxBufferLength()) {
            throw new SoapEnvelopeNotFoundException();
        }
        return scanComplete;
    }

    /**
     * @return true if the scan found what it was looking for, false if more data is needed
     */
    private boolean doScan() throws SoapEnvelopeNotFoundException {
        xmlPreamble.startIdx = xmlPreamble.endIdx = -1;
        envelopeDecl.startIdx = envelopeDecl.endIdx = -1;
        headers.startIdx = headers.endIdx = -1;
        
        int currentIdx = 0;
        while (currentIdx < buffer.length()) {
            byte currentByte = buffer.get(currentIdx);
            if (currentByte == (byte) '<') {
                if (xmlPreamble.startIdx == -1 && isPreamble(currentIdx)) {
                    currentIdx = consumePreamble(currentIdx);
                } else if (envelopeDecl.startIdx == -1) {
                    currentIdx = consumeEnvelopeDecl(currentIdx);
                    if (currentIdx == -1) {
                        throw new SoapEnvelopeNotFoundException();
                    }
                } else {
                    currentIdx = consumeHeaders(currentIdx);
                    if (currentIdx == -1) {
                        throw new SoapEnvelopeNotFoundException();
                    }
                }
            } else {
                currentIdx++;
            }
            
            if (headers.endIdx != -1) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * @param index
     */
    private int consumePreamble(int index) {
        int end = findFrom('>', index);
        if (end == -1) {
            return index + 1;
        } else {
            xmlPreamble.startIdx = index;
            xmlPreamble.endIdx = end;
            return end + 1;
        }
    }

    /**
     * @param index
     */
    private int consumeEnvelopeDecl(int index) {
        int end = findFrom('>', index);
        if (end == -1) {
            // Not enough buffer - need more data.
            return buffer.length();
        }
        try {
            String str = buffer.getString(index, end + 1, "UTF-8"); //$NON-NLS-1$
            String [] split = str.split(" "); //$NON-NLS-1$
            if (split[0].endsWith("Envelope")) { //$NON-NLS-1$
                envelopeDecl.startIdx = index;
                envelopeDecl.endIdx = end;
                return end + 1;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        
        // We found a section of XML that should have been a soap:Envelope, but wasn't.
        return -1;
    }

    /**
     * @param index
     */
    private int consumeHeaders(int index) {
        int end = findFrom('>', index);
        if (end == -1) {
            // Not enough buffer - need more data.
            return buffer.length();
        }
        try {
            String str = buffer.getString(index, end + 1, "UTF-8"); //$NON-NLS-1$
            String [] split = str.split("[ >]"); //$NON-NLS-1$
            if (!split[0].endsWith("Header")) { //$NON-NLS-1$
                // We found a section of XML that should have been soap:Header, but wasn't.
                return end;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        
        // At this point we know that we've found the start of the headers.
        headers.startIdx = index;
        
        // Now read the buffer until we find the end tag </soap:Header>
        end = findHeaderEndTag(end);
        if (end == -1) {
            // Not enough buffer - need more data.
            return buffer.length();
        } else {
            headers.endIdx = end;
        }
        
        return index + 1;
    }

    /**
     * Scans through the buffer, starting at the given index, looking for the
     * soap:Header end tag (</soap:Header>)  The end tag may have any prefix, or
     * none at all.  We'll actually just scan for end tags until we find one that
     * should match the current element.
     * @param index
     */
    private int findHeaderEndTag(int index) {
        int currentIdx = index;
        int depth = 0;
        while (currentIdx < buffer.length()) {
            byte currentByte = buffer.get(currentIdx);
            if (currentByte == (byte) '<') {
                boolean isEndTag = (currentIdx + 1) < buffer.length() && buffer.get(currentIdx+1) == '/';
                if (isEndTag && depth > 0) {
                    // Found an end tag corresponding to some header element within soap:Header
                    depth--;
                    currentIdx = findFrom('>', currentIdx + 1);
                    if (currentIdx == -1) {
                        return -1;
                    }
                } else if (isEndTag && depth == 0) {
                    // Found it!  Probably.
                    int end = findFrom('>', currentIdx + 1);
                    return end;
                } else {
                    // Found a start tag corresponding to a child element of soap:Header
                    depth++;
                    currentIdx = findFrom('>', currentIdx + 1);
                    if (currentIdx == -1) {
                        return -1;
                    }
                }
            } else {
                currentIdx++;
            }
        }
        return -1;
    }

    /**
     * Returns true if the index points to an XML preamble of the following example form:
     * 
     * <pre>
     *   &lt;?xml version="1.0"?>
     * </pre>
     * @param index
     */
    private boolean isPreamble(int index) {
        if (index <= buffer.length() - 6) {
            if (
                    buffer.get(index)     == '<' && 
                    buffer.get(index + 1) == '?' && 
                    buffer.get(index + 2) == 'x' && 
                    buffer.get(index + 3) == 'm' && 
                    buffer.get(index + 4) == 'l' && 
                    buffer.get(index + 5) == ' '
                ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Search for the given character in the buffer, starting at the
     * given index.  If not found, return -1.  If found, return the 
     * index of the character.
     * @param c
     * @param index
     */
    private int findFrom(char c, int index) {
        int currentIdx = index;
        while (currentIdx < buffer.length()) {
            if (buffer.get(currentIdx) == c) {
                return currentIdx;
            }
            currentIdx++;
        }
        return -1;
    }

    /**
     * @return the maxBufferLength
     */
    public int getMaxBufferLength() {
        return maxBufferLength;
    }

    /**
     * @param maxBufferLength the maxBufferLength to set
     */
    public void setMaxBufferLength(int maxBufferLength) {
        this.maxBufferLength = maxBufferLength;
    }
    
    /**
     * @return true if an XML preamble was found
     */
    public boolean hasXmlPreamble() {
        return xmlPreamble.startIdx != -1 && xmlPreamble.endIdx != -1;
    }
    
    /**
     * @return the xml preamble found during scanning
     */
    public String getXmlPreamble() {
        try {
            return buffer.getString(xmlPreamble.startIdx, xmlPreamble.endIdx + 1, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the soap envelope declaration found during scanning
     */
    public String getEnvelopeDeclaration() {
        try {
            return buffer.getString(envelopeDecl.startIdx, envelopeDecl.endIdx + 1, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the soap envelope declaration found during scanning
     */
    public String getHeaders() {
        try {
            return buffer.getString(headers.startIdx, headers.endIdx + 1, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Return the rest of the buffer (everything after the soap headers);
     * @param buffer
     */
    public byte[] getRemainingBytes() {
        return this.buffer.getBytes(headers.endIdx + 1, buffer.length());
    }
    
    /**
     * Models a range of bytes within the buffer.
     * @author eric.wittmann@gmail.com
     */
    private static class ByteRange {
        int startIdx = -1;
        int endIdx = -1;
    }
    
}
