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

package io.apiman.gateway.engine.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit Test for {@link XmlPayloadIO}.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class XmlPayloadIOTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.io.XmlPayloadIO#unmarshall(java.io.InputStream)}.
     */
    @Test
    public void testUnmarshall_Simple() throws Exception {
        String xml = "<?xml version=\"1.0\"?>\r\n" + 
                "<book xmlns=\"urn:ns1\">\r\n" + 
                "      <author>Gambardella, Matthew</author>\r\n" + 
                "      <title>XML Developer's Guide</title>\r\n" + 
                "      <genre>Computer</genre>\r\n" + 
                "      <price>44.95</price>\r\n" + 
                "      <publish_date>2000-10-01</publish_date>\r\n" + 
                "      <description>An in-depth look at creating applications with XML.</description>\r\n" + 
                "</book>";
        byte [] xmlBytes = xml.getBytes();
        XmlPayloadIO io = new XmlPayloadIO();
        try (InputStream is = new ByteArrayInputStream(xmlBytes)) {
            Document document = io.unmarshall(is);
            Assert.assertNotNull(document);
            Assert.assertEquals("book", document.getDocumentElement().getLocalName());
            Assert.assertEquals("urn:ns1", document.getDocumentElement().getNamespaceURI());
        }
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.io.XmlPayloadIO#marshall(org.w3c.dom.Document)}.
     */
    @Test
    public void testMarshall_Simple() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document document = builder.newDocument();
        Element bookElem = document.createElementNS("urn:ns1", "book");
        document.appendChild(bookElem);
        
        Element authorElem = document.createElementNS("urn:ns1", "author");
        Element titleElem = document.createElementNS("urn:ns1", "title");
        Element genreElem = document.createElementNS("urn:ns1", "genre");
        
        authorElem.setTextContent("Gambardella, Matthew");
        titleElem.setTextContent("title");
        genreElem.setTextContent("Computer");
        
        bookElem.appendChild(authorElem);
        bookElem.appendChild(titleElem);
        bookElem.appendChild(genreElem);
        
        XmlPayloadIO io = new XmlPayloadIO();
        byte[] xmlBytes = io.marshall(document);
        String xml = new String(xmlBytes);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<book xmlns=\"urn:ns1\">"
                    + "<author>Gambardella, Matthew</author>"
                    + "<title>title</title>"
                    + "<genre>Computer</genre>"
                + "</book>";
        Assert.assertEquals(expected, xml);
    }
    
    

}
