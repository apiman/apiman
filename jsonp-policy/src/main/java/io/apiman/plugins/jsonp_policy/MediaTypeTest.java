package io.apiman.plugins.jsonp_policy;

import static org.junit.Assert.*;

import org.junit.Test;

public class MediaTypeTest {

	@Test
	public void testGetTypeSubtype() {
		MediaType mediaType = new MediaType("text/xml");
		assertEquals("text/xml", mediaType.getTypeSubtype());

		mediaType = new MediaType("application/json; charset=ISO-8859-4");
		assertEquals("application/json", mediaType.getTypeSubtype());

		mediaType = new MediaType("application/vnd.linn.user+json; charset=us-ascii; version=1");
		assertEquals("application/vnd.linn.user+json", mediaType.getTypeSubtype());
	}

	@Test
	public void testGetParameters() {
		MediaType mediaType = new MediaType("text/xml");
		assertEquals(0, mediaType.getParameters().size());

		mediaType = new MediaType("application/json; charset=ISO-8859-4");
		assertEquals(1, mediaType.getParameters().size());
		assertEquals("ISO-8859-4", mediaType.getParameters().get("charset"));

		mediaType = new MediaType("application/vnd.linn.user+json; charset=us-ascii; version=1");
		assertEquals(2, mediaType.getParameters().size());
		assertEquals("us-ascii", mediaType.getParameters().get("charset"));
		assertEquals("1", mediaType.getParameters().get("version"));
	}

	@Test
	public void testToString() {
		MediaType mediaType = new MediaType("text/xml");
		assertEquals("text/xml", mediaType.toString());
	}

}
