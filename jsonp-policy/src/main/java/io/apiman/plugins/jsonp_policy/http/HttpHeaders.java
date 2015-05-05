package io.apiman.plugins.jsonp_policy.http;

import java.util.Map;

/**
 * Utility class to get and set data into headers.
 * 
 * @author Alexandre Kieling <alex.kieling@gmail.com>
 */
public class HttpHeaders {

    private static final String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$
    private final Map<String, String> headers;

    /**
     * Constructor.
     * 
     * @param headers headers map
     */
    public HttpHeaders(Map<String, String> headers) {
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

}
