package io.apiman.manager.api.schema.format;

import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Rewrite WSDL schema to point to managed Apiman gateway endpoint (adapted from old WsdlHelper).
 * <p>
 * Multiple endpoints are not supported for this schema type.
 *
 * @see <a href="https://www.w3.org/TR/wsdl20/">WSDL</a>
 * @author eric.wittmann@redhat.com
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class WsdlRewriter implements ApiDefinitionProvider {

    private static final String LOCATION = "location";
    private static final String SOAP_ADDRESS = "address";
    private static final String[] SOAP_NAMESPACES = {
            "http://schemas.xmlsoap.org/wsdl/soap/",
            "http://schemas.xmlsoap.org/wsdl/soap12/"
    };

    @Override
    public String rewrite(ProviderContext ctx, InputStream wsdlStream, ApiDefinitionType apiDefinitionType)
            throws IOException, StorageException, GatewayAuthenticationException, Exception {
        IStorage storage = ctx.getStorage();
        ApiVersionBean avb = ctx.getAvb();
        String orgId = avb.getApi().getOrganization().getId();
        String apiId = avb.getApi().getId();
        String apiVersion = avb.getVersion();

        Document document = readWsdlInputStream(wsdlStream);

        // Collection of all SOAP binding addresses
        List<Element> allSoapAddresses = new LinkedList<>();

        for (String soapNamespace : SOAP_NAMESPACES) {
            NodeList soapAddresses = document.getDocumentElement().getElementsByTagNameNS(soapNamespace, SOAP_ADDRESS);
            if (soapAddresses.getLength() > 0) {
                for (int j = 0; j < soapAddresses.getLength(); j++) {
                    allSoapAddresses.add((Element) soapAddresses.item(j));
                }
            }
        }

        // Find IDs of all GWs this ApiVersion is published onto.
        String firstGateway = avb.getGateways().stream()
                                      .map(ApiGatewayBean::getGatewayId)
                                      .findFirst()
                                      .orElse("");

        GatewayBean gateway = storage.getGateway(firstGateway);
        IGatewayLink link = ctx.getGatewayLinkFactory().create(gateway);

        // Go through the addresses we've found (if any) and update the 'location' attribute if needed.
        String endpoint = link.getApiEndpoint(orgId, apiId, apiVersion).getEndpoint();
        for (Element addressElem : allSoapAddresses) {
            String location = addressElem.getAttribute(LOCATION);
            if (!location.equals(endpoint)) {
                addressElem.setAttribute(LOCATION, endpoint);
            }
        }

        // Convert document back to string and update in storage.
        return xmlDocumentToString(document);
    }

    /**
     * Read the wsdl from input stream (xml)
     *
     * @param wsdlStream the wsdl(xml) as stream
     * @return the wsdl as document
     */
    private Document readWsdlInputStream(InputStream wsdlStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.parse(wsdlStream);
    }

    private String xmlDocumentToString(Document document) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.getBuffer().toString();
    }
}
