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
import java.util.Iterator;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit Test for {@link SoapPayloadIO}.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class SoapPayloadIOTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.io.SoapPayloadIO#unmarshall(java.io.InputStream)}.
     */
    @Test
    public void testUnmarshall_Simple() throws Exception {
        String xml = "<?xml version=\"1.0\"?>\r\n" + 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\r\n" + 
                "  <soap:Header>\r\n" + 
                "     <ns1:CustomHeader xmlns:ns1=\"urn:ns1\">CVALUE</ns1:CustomHeader>\r\n" + 
                "  </soap:Header>\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        byte [] xmlBytes = xml.getBytes();
        SoapPayloadIO io = new SoapPayloadIO();
        try (InputStream is = new ByteArrayInputStream(xmlBytes)) {
            SOAPEnvelope envelope = io.unmarshall(is);
            Assert.assertNotNull(envelope);
            Assert.assertEquals("Envelope", envelope.getLocalName());
            Assert.assertEquals("http://www.w3.org/2003/05/soap-envelope", envelope.getNamespaceURI());
            
            SOAPHeader header = envelope.getHeader();
            Assert.assertNotNull(header);
            
            Iterator allHeaderElements = header.examineAllHeaderElements();
            Assert.assertTrue(allHeaderElements.hasNext());
            SOAPHeaderElement cheader = (SOAPHeaderElement) allHeaderElements.next();
            Assert.assertNotNull(cheader);
            Assert.assertEquals("CVALUE", cheader.getTextContent());
        }
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.io.SoapPayloadIO#marshall(org.w3c.dom.Document)}.
     */
    @Test
    public void testMarshall_Simple() throws Exception {
        
    }
    
    

}
