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

import io.apiman.gateway.engine.io.ByteBuffer;
import io.apiman.gateway.engine.io.IApimanBuffer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class SoapHeaderScannerTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.soap.SoapHeaderScanner#scan(io.apiman.gateway.engine.io.IApimanBuffer)}.
     */
    @Test
    public void testScanNoSoap() throws SoapEnvelopeNotFoundException {
        String testData = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin finibus mauris "
                + "vel fermentum finibus. Proin lacus nulla, placerat in velit tristique, semper congue nisi. "
                + "Quisque ultricies metus felis, eu aliquet ligula gravida euismod. Integer et nulla eget "
                + "metus pretium consectetur eu id arcu. Fusce odio turpis, gravida sit amet finibus eu, "
                + "consequat in est. Nunc eu volutpat leo. Praesent sollicitudin est vitae lacus egestas, "
                + "iaculis dictum libero suscipit. Morbi vitae egestas diam. Nulla eu molestie urna.";
        IApimanBuffer buffer = new ByteBuffer(testData);
        SoapHeaderScanner scanner = new SoapHeaderScanner();
        boolean done = scanner.scan(buffer);
        Assert.assertFalse("Expected the scan to *not* complete.", done);
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.soap.SoapHeaderScanner#scan(io.apiman.gateway.engine.io.IApimanBuffer)}.
     */
    @Test(expected=SoapEnvelopeNotFoundException.class)
    public void testScanNoEnvelopeFound() throws SoapEnvelopeNotFoundException {
        String testData = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin finibus mauris "
                + "vel fermentum finibus. Proin lacus nulla, placerat in velit tristique, semper congue nisi. "
                + "Quisque ultricies metus felis, eu aliquet ligula gravida euismod. Integer et nulla eget "
                + "metus pretium consectetur eu id arcu. Fusce odio turpis, gravida sit amet finibus eu, "
                + "consequat in est. Nunc eu volutpat leo. Praesent sollicitudin est vitae lacus egestas, "
                + "iaculis dictum libero suscipit. Morbi vitae egestas diam. Nulla eu molestie urna.";
        IApimanBuffer buffer = new ByteBuffer(testData);
        SoapHeaderScanner scanner = new SoapHeaderScanner();
        scanner.setMaxBufferLength(100);
        scanner.scan(buffer);
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.soap.SoapHeaderScanner#scan(io.apiman.gateway.engine.io.IApimanBuffer)}.
     */
    @Test
    public void testScanSimple() throws SoapEnvelopeNotFoundException {
        String testData = "<?xml version=\"1.0\"?>\r\n" + 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\r\n" + 
                "  <soap:Header>\r\n" + 
                "  </soap:Header>\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        IApimanBuffer buffer = new ByteBuffer(testData);
        SoapHeaderScanner scanner = new SoapHeaderScanner();
        boolean done = scanner.scan(buffer);
        Assert.assertTrue("Expected the scan to be complete but was not.", done);
        Assert.assertTrue("Expected the scan to find an XML preamble.", scanner.hasXmlPreamble());
        Assert.assertEquals("<?xml version=\"1.0\"?>", scanner.getXmlPreamble());
        String expectedEnvelopeDecl = 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">";
        Assert.assertEquals(expectedEnvelopeDecl, scanner.getEnvelopeDeclaration());
        String expectedHeaders = 
                "<soap:Header>\r\n" + 
                "  </soap:Header>";
        Assert.assertEquals(expectedHeaders, scanner.getHeaders());
        IApimanBuffer remainingBuffer = new ByteBuffer(scanner.getRemainingBytes());
        String expectedRemaining =
                "\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        Assert.assertEquals(expectedRemaining, remainingBuffer.getString(0, remainingBuffer.length()));
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.soap.SoapHeaderScanner#scan(io.apiman.gateway.engine.io.IApimanBuffer)}.
     */
    @Test
    public void testScanSimpleWithHeader() throws SoapEnvelopeNotFoundException {
        String testData = "<?xml version=\"1.0\"?>\r\n" + 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\r\n" + 
                "  <soap:Header>\r\n" + 
                "    <ns1:MyHeader xmlns:ns1=\"urn:namespace1\">Foo</ns1:MyHeader>\r\n" + 
                "  </soap:Header>\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        IApimanBuffer buffer = new ByteBuffer(testData);
        SoapHeaderScanner scanner = new SoapHeaderScanner();
        boolean done = scanner.scan(buffer);
        Assert.assertTrue("Expected the scan to be complete but was not.", done);
        Assert.assertTrue("Expected the scan to find an XML preamble.", scanner.hasXmlPreamble());
        Assert.assertEquals("<?xml version=\"1.0\"?>", scanner.getXmlPreamble());
        String expectedEnvelopeDecl = 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">";
        Assert.assertEquals(expectedEnvelopeDecl, scanner.getEnvelopeDeclaration());
        String expectedHeaders = 
                "<soap:Header>\r\n" + 
                "    <ns1:MyHeader xmlns:ns1=\"urn:namespace1\">Foo</ns1:MyHeader>\r\n" +
                "  </soap:Header>";
        Assert.assertEquals(expectedHeaders, scanner.getHeaders());
        IApimanBuffer remainingBuffer = new ByteBuffer(scanner.getRemainingBytes());
        String expectedRemaining =
                "\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        Assert.assertEquals(expectedRemaining, remainingBuffer.getString(0, remainingBuffer.length()));
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.soap.SoapHeaderScanner#scan(io.apiman.gateway.engine.io.IApimanBuffer)}.
     */
    @Test
    public void testScanSimpleWithHeaders() throws SoapEnvelopeNotFoundException {
        String testData = "<?xml version=\"1.0\"?>\r\n" + 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\r\n" + 
                "  <soap:Header>\r\n" + 
                "    <ns1:MyHeader1 xmlns:ns1=\"urn:namespace1\">Foo 1</ns1:MyHeader1>\r\n" + 
                "    <ns1:MyHeader2 xmlns:ns1=\"urn:namespace1\">Foo 2</ns1:MyHeader2>\r\n" + 
                "    <ns2:MyHeader3 xmlns:ns2=\"urn:namespace2\">Foo 3</ns2:MyHeader3>\r\n" + 
                "    <ns2:MyHeader4 xmlns:ns2=\"urn:namespace2\">Foo 4</ns2:MyHeader4>\r\n" + 
                "  </soap:Header>\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        IApimanBuffer buffer = new ByteBuffer(testData);
        SoapHeaderScanner scanner = new SoapHeaderScanner();
        boolean done = scanner.scan(buffer);
        Assert.assertTrue("Expected the scan to be complete but was not.", done);
        Assert.assertTrue("Expected the scan to find an XML preamble.", scanner.hasXmlPreamble());
        Assert.assertEquals("<?xml version=\"1.0\"?>", scanner.getXmlPreamble());
        String expectedEnvelopeDecl = 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">";
        Assert.assertEquals(expectedEnvelopeDecl, scanner.getEnvelopeDeclaration());
        String expectedHeaders = 
                "<soap:Header>\r\n" + 
                "    <ns1:MyHeader1 xmlns:ns1=\"urn:namespace1\">Foo 1</ns1:MyHeader1>\r\n" + 
                "    <ns1:MyHeader2 xmlns:ns1=\"urn:namespace1\">Foo 2</ns1:MyHeader2>\r\n" + 
                "    <ns2:MyHeader3 xmlns:ns2=\"urn:namespace2\">Foo 3</ns2:MyHeader3>\r\n" + 
                "    <ns2:MyHeader4 xmlns:ns2=\"urn:namespace2\">Foo 4</ns2:MyHeader4>\r\n" + 
                "  </soap:Header>";
        Assert.assertEquals(expectedHeaders, scanner.getHeaders());
        IApimanBuffer remainingBuffer = new ByteBuffer(scanner.getRemainingBytes());
        String expectedRemaining =
                "\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        Assert.assertEquals(expectedRemaining, remainingBuffer.getString(0, remainingBuffer.length()));
    }
    
    /**
     * Test method for {@link io.apiman.gateway.engine.soap.SoapHeaderScanner#scan(io.apiman.gateway.engine.io.IApimanBuffer)}.
     */
    @Test
    public void testScanMulti() throws SoapEnvelopeNotFoundException {
        String testData1 = "<?xml version=\"1.0\"?>\r\n" + 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\r\n" + 
                "  <soap:Hea";
        String testData2 = 
                "der>\r\n" + 
                "  </soap:Header>\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        IApimanBuffer buffer1 = new ByteBuffer(testData1);
        IApimanBuffer buffer2 = new ByteBuffer(testData2);

        SoapHeaderScanner scanner = new SoapHeaderScanner();
        boolean done = scanner.scan(buffer1);
        Assert.assertFalse("Expected the scan to NOT be complete.", done);
        
        done = scanner.scan(buffer2);
        Assert.assertTrue("Expected the scan to be complete but was not.", done);
        Assert.assertTrue("Expected the scan to find an XML preamble.", scanner.hasXmlPreamble());
        Assert.assertEquals("<?xml version=\"1.0\"?>", scanner.getXmlPreamble());
        String expectedEnvelopeDecl = 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">";
        Assert.assertEquals(expectedEnvelopeDecl, scanner.getEnvelopeDeclaration());
        String expectedHeaders = 
                "<soap:Header>\r\n" + 
                "  </soap:Header>";
        Assert.assertEquals(expectedHeaders, scanner.getHeaders());
        IApimanBuffer remainingBuffer = new ByteBuffer(scanner.getRemainingBytes());
        String expectedRemaining =
                "\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        Assert.assertEquals(expectedRemaining, remainingBuffer.getString(0, remainingBuffer.length()));
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.soap.SoapHeaderScanner#scan(io.apiman.gateway.engine.io.IApimanBuffer)}.
     */
    @Test
    public void testScanMultiWithHeaders() throws SoapEnvelopeNotFoundException {
        String testData1 = "<?xml version=\"1.0\"?>\r\n" + 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\r\n" + 
                "  <";
        String testData2 = 
                "soap:Header>\r\n" + 
                "    <ns1:MyHeader1 xmlns:ns1=\"urn:namespace1\">Foo 1</ns1:MyHeader1>\r\n" + 
                "    <ns1:MyHeader2 xmlns:ns1=\"urn:namespace1\">Foo 2<";
        String testData3 = 
                "/ns1:MyHeader2>\r\n" + 
                "    <ns2:MyHeader3 xmlns:ns2=\"urn:namespace2\">Foo ";
        String testData4 = 
                "3</ns2:MyHeader3>\r\n" + 
                "    <ns2:MyHeader4 xmlns:ns2=\"urn:namespace2\">Foo 4</ns2:MyHeader4>\r\n" + 
                "  </soap:Header";
        String testData5 = 
                ">\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        IApimanBuffer buffer1 = new ByteBuffer(testData1);
        IApimanBuffer buffer2 = new ByteBuffer(testData2);
        IApimanBuffer buffer3 = new ByteBuffer(testData3);
        IApimanBuffer buffer4 = new ByteBuffer(testData4);
        IApimanBuffer buffer5 = new ByteBuffer(testData5);
        
        SoapHeaderScanner scanner = new SoapHeaderScanner();
        boolean done = scanner.scan(buffer1);
        Assert.assertFalse("Expected the scan to NOT be complete.", done);
        
        done = scanner.scan(buffer2);
        Assert.assertFalse("Expected the scan to NOT be complete.", done);

        done = scanner.scan(buffer3);
        Assert.assertFalse("Expected the scan to NOT be complete.", done);

        done = scanner.scan(buffer4);
        Assert.assertFalse("Expected the scan to NOT be complete.", done);

        done = scanner.scan(buffer5);
        Assert.assertTrue("Expected the scan to be complete but was not.", done);
        Assert.assertTrue("Expected the scan to find an XML preamble.", scanner.hasXmlPreamble());
        Assert.assertEquals("<?xml version=\"1.0\"?>", scanner.getXmlPreamble());
        String expectedEnvelopeDecl = 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">";
        Assert.assertEquals(expectedEnvelopeDecl, scanner.getEnvelopeDeclaration());
        String expectedHeaders = 
                "<soap:Header>\r\n" + 
                "    <ns1:MyHeader1 xmlns:ns1=\"urn:namespace1\">Foo 1</ns1:MyHeader1>\r\n" + 
                "    <ns1:MyHeader2 xmlns:ns1=\"urn:namespace1\">Foo 2</ns1:MyHeader2>\r\n" + 
                "    <ns2:MyHeader3 xmlns:ns2=\"urn:namespace2\">Foo 3</ns2:MyHeader3>\r\n" + 
                "    <ns2:MyHeader4 xmlns:ns2=\"urn:namespace2\">Foo 4</ns2:MyHeader4>\r\n" + 
                "  </soap:Header>";
        Assert.assertEquals(expectedHeaders, scanner.getHeaders());
        IApimanBuffer remainingBuffer = new ByteBuffer(scanner.getRemainingBytes());
        String expectedRemaining =
                "\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        Assert.assertEquals(expectedRemaining, remainingBuffer.getString(0, remainingBuffer.length()));
    }


    /**
     * Test method for {@link io.apiman.gateway.engine.soap.SoapHeaderScanner#scan(io.apiman.gateway.engine.io.IApimanBuffer)}.
     */
    @Test
    public void testScanSimpleWithHeadersSmallBuffer() throws SoapEnvelopeNotFoundException {
        String testData = "<?xml version=\"1.0\"?>\r\n" + 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\r\n" + 
                "  <soap:Header>\r\n" + 
                "    <ns1:MyHeader1 xmlns:ns1=\"urn:namespace1\">Foo 1</ns1:MyHeader1>\r\n" + 
                "    <ns1:MyHeader2 xmlns:ns1=\"urn:namespace1\">Foo 2</ns1:MyHeader2>\r\n" + 
                "    <ns2:MyHeader3 xmlns:ns2=\"urn:namespace2\">Foo 3</ns2:MyHeader3>\r\n" + 
                "    <ns2:MyHeader4 xmlns:ns2=\"urn:namespace2\">Foo 4</ns2:MyHeader4>\r\n" + 
                "  </soap:Header>\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        IApimanBuffer buffer = new ByteBuffer(testData);
        SoapHeaderScanner scanner = new SoapHeaderScanner();
        scanner.setMaxBufferLength(100);
        boolean done = scanner.scan(buffer);
        Assert.assertTrue("Expected the scan to be complete but was not.", done);
        Assert.assertTrue("Expected the scan to find an XML preamble.", scanner.hasXmlPreamble());
        Assert.assertEquals("<?xml version=\"1.0\"?>", scanner.getXmlPreamble());
        String expectedEnvelopeDecl = 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">";
        Assert.assertEquals(expectedEnvelopeDecl, scanner.getEnvelopeDeclaration());
        String expectedHeaders = 
                "<soap:Header>\r\n" + 
                "    <ns1:MyHeader1 xmlns:ns1=\"urn:namespace1\">Foo 1</ns1:MyHeader1>\r\n" + 
                "    <ns1:MyHeader2 xmlns:ns1=\"urn:namespace1\">Foo 2</ns1:MyHeader2>\r\n" + 
                "    <ns2:MyHeader3 xmlns:ns2=\"urn:namespace2\">Foo 3</ns2:MyHeader3>\r\n" + 
                "    <ns2:MyHeader4 xmlns:ns2=\"urn:namespace2\">Foo 4</ns2:MyHeader4>\r\n" + 
                "  </soap:Header>";
        Assert.assertEquals(expectedHeaders, scanner.getHeaders());
        IApimanBuffer remainingBuffer = new ByteBuffer(scanner.getRemainingBytes());
        String expectedRemaining =
                "\r\n" + 
                "  <soap:Body>\r\n" + 
                "    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock/Surya\">\r\n" + 
                "      <m:StockName>IBM</m:StockName>\r\n" + 
                "    </m:GetStockPrice>\r\n" + 
                "  </soap:Body>\r\n" + 
                "</soap:Envelope>";
        Assert.assertEquals(expectedRemaining, remainingBuffer.getString(0, remainingBuffer.length()));
    }
    

}
