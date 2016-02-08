package io.apiman.plugins.jsonp_policy.http;

import io.apiman.gateway.engine.beans.util.HeaderMap;

/**
 * Utility class to get and set data into headers.
 *
 * @author Alexandre Kieling {@literal <alex.kieling@gmail.com>}
 */
public class HttpHeaders {

    private static final String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$
    private static final String CONTENT_LENGTH = "Content-Length"; //$NON-NLS-1$

    private final HeaderMap headers;

    /**
     * Constructor.
     *
     * @param headers headers map
     */
    public HttpHeaders(HeaderMap headers) {
        this.headers = headers;
    }

    /**
     * Extract the charset from the Content-Type header. When not present, the default charset is returned.
     *
     * @param defaultCharset the default charset
     * @return charset
     */
    public String getCharsetFromContentType(String defaultCharset) {
        String charset = null;
        String contentTypeStr = headers.get(CONTENT_TYPE);
        if (contentTypeStr != null) {
            charset = new ContentType(contentTypeStr).getCharset();
        }
        return charset != null ? charset : defaultCharset;
    }

    /**
     * Set the type/subtype value of the Content-Type header.
     *
     * @param typeSubtype the type/subtype value
     */
    public void setContentType(String typeSubtype) {
        String contentTypeStr = headers.get(CONTENT_TYPE);
        if (contentTypeStr != null) {
            ContentType contentType = new ContentType(contentTypeStr);
            contentType.setTypeSubtype(typeSubtype);
            headers.put(CONTENT_TYPE, contentType.toString());
        } else {
            headers.put(CONTENT_TYPE, typeSubtype);
        }
    }

    /**
     * @param additionalContentLength the additional content length
     */
    public void incrementContentLength(int additionalContentLength) {
        String cl = headers.get(CONTENT_LENGTH);
        if (cl != null && cl.length() > 0) {
            int clength = new Integer(cl).intValue();
            headers.put(CONTENT_LENGTH, String.valueOf(clength + additionalContentLength));
        }
    }

}
