package io.apiman.plugins.jsonp_policy.http;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class HttpHeadersTest {

    private static final String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$
    private Map<String, String> headers;

    @Before
    public void setUp() {
        headers = new HashMap<String, String>();
    }

    @Test
    public void shouldReturnDefaultCharsetWhenContentTypeHeaderDoesNotExist() {
        // given
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        // when
        String charset = httpHeaders.getCharsetFromContentType("UTF-8");
        // then
        assertEquals("UTF-8", charset);
    }

    @Test
    public void shouldReturnDefaultCharsetWhenContentTypeHeaderDoesNotContainCharsetParameter() {
        // given
        headers.put(CONTENT_TYPE, "application/json");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        // when
        String charset = httpHeaders.getCharsetFromContentType("US-ASCII");
        // then
        assertEquals("US-ASCII", charset);
    }

    @Test
    public void shouldReturnCharsetWhenContentTypeHeaderContainsCharsetParameter() {
        // given
        headers.put(CONTENT_TYPE, "application/json; charset=ISO-8859-1");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        // when
        String charset = httpHeaders.getCharsetFromContentType("UTF-8");
        // then
        assertEquals("ISO-8859-1", charset);
    }

    @Test
    public void shouldAddContentTypeWhenItDoesNotExist() {
        // given
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        // when
        httpHeaders.setContentType("application/javascript");
        // then
        assertEquals("application/javascript", headers.get(CONTENT_TYPE));
    }

    @Test
    public void shouldReplaceContentTypeWhenItAlreadyExists() {
        // given
        headers.put(CONTENT_TYPE, "application/json");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        // when
        httpHeaders.setContentType("application/javascript");
        // then
        assertEquals("application/javascript", headers.get(CONTENT_TYPE));
    }

    @Test
    public void shouldReplaceContentTypeWhenItAlreadyExistsAndIncludesParameter() {
        // given
        headers.put(CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpHeaders httpHeaders = new HttpHeaders(headers);
        // when
        httpHeaders.setContentType("application/javascript");
        // then
        assertEquals("application/javascript; charset=UTF-8", headers.get(CONTENT_TYPE));
    }

}
