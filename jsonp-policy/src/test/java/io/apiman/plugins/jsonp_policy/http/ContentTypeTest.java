package io.apiman.plugins.jsonp_policy.http;

import static org.junit.Assert.*;
import io.apiman.plugins.jsonp_policy.http.ContentType;

import org.junit.Test;

public class ContentTypeTest {

    @Test
    public void testGetTypeSubtype() {
        assertEquals("text/xml", new ContentType("text/xml").getTypeSubtype());
        assertEquals("image/svg+xml", new ContentType(" image/svg+xml; charset=UTF-8").getTypeSubtype());
        assertEquals("application/vnd.oasis.opendocument.text", new ContentType(
                "  application/vnd.oasis.opendocument.text ; charset=UTF-8 ; version=1 ").getTypeSubtype());
    }

    @Test
    public void testSetTypeSubtype() {
        ContentType contentType = new ContentType("text/xml");
        contentType.setTypeSubtype("application/javascript");
        assertEquals("application/javascript", contentType.getTypeSubtype());
        assertEquals("application/javascript", contentType.toString());

        contentType = new ContentType(" image/svg+xml; charset=UTF-8");
        contentType.setTypeSubtype("application/javascript");
        assertEquals("application/javascript", contentType.getTypeSubtype());
        assertEquals("application/javascript; charset=UTF-8", contentType.toString());

        contentType = new ContentType("  application/vnd.oasis.opendocument.text ; charset=UTF-8 ; version=1 ");
        contentType.setTypeSubtype("application/javascript");
        assertEquals("application/javascript", contentType.getTypeSubtype());
        assertEquals("application/javascript; charset=UTF-8; version=1", contentType.toString());
    }

    @Test
    public void testGetCharset() {
        assertNull(new ContentType("text/xml").getCharset());
        assertEquals("utf-8", new ContentType(" image/svg+xml; charset=utf-8").getCharset());
        assertEquals("ISO-8859-1", new ContentType(
                "  application/vnd.oasis.opendocument.text ;  charset=ISO-8859-1 ; version=1  ").getCharset());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("text/xml", new ContentType("text/xml").toString());
        assertEquals("image/svg+xml; charset=utf-8", new ContentType(" image/svg+xml; charset=utf-8").toString());
        assertEquals("application/vnd.oasis.opendocument.text; charset=ISO-8859-1; version=1", new ContentType(
                "  application/vnd.oasis.opendocument.text ;  charset=ISO-8859-1 ; version=1  ").toString());
    }

}
