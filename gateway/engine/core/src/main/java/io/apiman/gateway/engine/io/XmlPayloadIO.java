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
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Reads/writes XML data, typically used for REST services that
 * speak XML instead of JSON.
 *
 * @author eric.wittmann@redhat.com
 */
public class XmlPayloadIO implements IPayloadIO<Document> {

    /**
     * @see io.apiman.gateway.engine.io.IPayloadIO#unmarshall(java.io.InputStream)
     */
    @Override
    public Document unmarshall(InputStream input) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(input);
        return doc;
    }

    /**
     * @see io.apiman.gateway.engine.io.IPayloadIO#unmarshall(byte[])
     */
    @Override
    public Document unmarshall(byte[] input) throws Exception {
        try (InputStream is = new ByteArrayInputStream(input)) {
            return unmarshall(is);
        }
    }

    /**
     * @see io.apiman.gateway.engine.io.IPayloadIO#marshall(java.lang.Object)
     */
    @Override
    public byte[] marshall(Document data) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(data);
        transformer.transform(source, result);
        String xml = result.getWriter().toString();
        String enc = data.getXmlEncoding();
        if (enc == null) {
            enc = "UTF8"; //$NON-NLS-1$
        }
        return xml.getBytes(enc);
    }

}
