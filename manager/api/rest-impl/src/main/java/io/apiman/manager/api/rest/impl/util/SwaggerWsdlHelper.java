package io.apiman.manager.api.rest.impl.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class SwaggerWsdlHelper {

    private static final String BASE_PATH = "basePath";
    private static final String HOST = "host";
    private static final String LOCATION = "location";
    private static final String SOAP_ADDRESS[] = {"soap:address","soap12:address"};
    private static final String SWAGGER = "swagger";

    /**
     * Constructor
     */
    private SwaggerWsdlHelper() {}

    /**
     * Reads an InputStream to a string
     * https://stackoverflow.com/a/35446009
     *
     * @param swaggerDefinitionStream
     * @return the stream as string
     * @throws IOException
     */
    public static String readSwaggerStreamToString(InputStream swaggerDefinitionStream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(swaggerDefinitionStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int result = bufferedInputStream.read();
        while (result != -1) {
            byteArrayOutputStream.write((byte) result);
            result = bufferedInputStream.read();
        }
        String streamAsString = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
        swaggerDefinitionStream.close();
        bufferedInputStream.close();
        byteArrayOutputStream.close();
        return streamAsString;
    }

    /**
     * Converts yaml string to json string
     *
     * @param yamlString yaml string
     * @return yaml as json
     * @throws IOException
     */
    public static String convertYamlToJson(String yamlString) throws IOException {
        ObjectMapper jsonWriter = new ObjectMapper();
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object yaml = yamlReader.readValue(yamlString, Object.class);
        return jsonWriter.writeValueAsString(yaml);
    }

    /**
     * Updates the swagger defintion file with the host and base path of the managed endpoint.
     * If there are changes the definition will updated silently in the storage.
     *
     * @param managedEndpoint   the managed endpoint
     * @param swaggerDefinition the swagger definition
     * @param apiVersion        the api version
     * @param storage           the storage implementation
     * @return prettified and updated swagger definition as string
     * @throws IOException
     * @throws StorageException
     */
    public static String updateSwaggerDefinitionWithEndpoint(URL managedEndpoint, String swaggerDefinition, ApiVersionBean apiVersion, IStorage storage) throws IOException, StorageException {
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode swaggerJsonNode = jsonMapper.readTree(swaggerDefinition);
        ObjectNode swaggerObjectNode = (ObjectNode) swaggerJsonNode;

        // get basePath and host from json
        String host = swaggerJsonNode.path(HOST).asText("");
        String path = swaggerJsonNode.path(BASE_PATH).asText("");

        // calculate basePath and host from managed endpoint
        String endpointHost = managedEndpoint.getHost();
        String endpointPort = (managedEndpoint.getPort() != -1) ? ":" + managedEndpoint.getPort() : "";
        String endpointPath = managedEndpoint.getPath();

        // replace values if swagger definition has version 2.0+
        Boolean updateStorage = false;
        if (swaggerJsonNode.findValue(SWAGGER) != null && (!host.equals(endpointHost + endpointPort) || !path.equals(endpointPath))) {
            swaggerObjectNode.put(BASE_PATH, endpointPath);
            swaggerObjectNode.put(HOST, endpointHost + endpointPort);
            updateStorage = true;
        }

        String prettifiedSwaggerDefinition = jsonNodeToString(swaggerJsonNode, apiVersion);
        // update definition in storage silently
        if (updateStorage)
            storage.updateApiDefinition(apiVersion, new ByteArrayInputStream(prettifiedSwaggerDefinition.getBytes(StandardCharsets.UTF_8)));

        return prettifiedSwaggerDefinition;
    }

    /**
     * Transforms a jsonNode to a prettified json or yaml string depending on the api definition type
     *
     * @param jsonNode   jsonNode to prettify
     * @param apiVersion apiVersion to determine the definition type
     * @return prettified swagger definition as yaml or json string
     */
    private static String jsonNodeToString(JsonNode jsonNode, ApiVersionBean apiVersion) {
        String prettifiedSwaggerDefinition = null;
        try {
            if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerJSON) {
                prettifiedSwaggerDefinition = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            } else if (apiVersion.getDefinitionType() == ApiDefinitionType.SwaggerYAML) {
                prettifiedSwaggerDefinition = new YAMLMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return prettifiedSwaggerDefinition;
    }

    /**
     * Updates the location in wsdl with the host and path of the managed endpoint.
     *
     * @param wsdlStream      the wsdl as stream
     * @param managedEndpoint the managed endpoint
     * @param apiVersion      the api version
     * @param storage         the storage implementation
     * @return
     * @throws StorageException
     */
    public static String updateLocationEndpointInWsdl(InputStream wsdlStream, URL managedEndpoint, ApiVersionBean apiVersion, IStorage storage) throws StorageException {
        Document document = readWsdlInputStream(wsdlStream);
        Boolean updateStorage = false;
        for(int i = 0; i < SOAP_ADDRESS.length;i++) {
            NodeList nodeList = document.getDocumentElement().getElementsByTagName(SOAP_ADDRESS[i]);
            if (nodeList != null) {
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Node node = nodeList.item(j);
                    String location = node.getAttributes().getNamedItem(LOCATION).getNodeValue();
                    String endpoint = managedEndpoint.toString();
                    if (!location.equals(endpoint)) {
                        node.getAttributes().getNamedItem(LOCATION).setNodeValue(managedEndpoint.toString());
                        updateStorage = true;
                    }
                }
            }
        }
        String wsdl = xmlDocumentToString(document);
        if (updateStorage) {
            storage.updateApiDefinition(apiVersion, new ByteArrayInputStream(wsdl.getBytes(StandardCharsets.UTF_8)));
        }
        return wsdl;
    }

    /**
     * Read the wsdl from input stream (xml)
     *
     * @param wsdlStream the wsdl(xml) as stream
     * @return the wsdl as document
     */
    private static Document readWsdlInputStream(InputStream wsdlStream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        Document document = null;
        try {
            documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.parse(wsdlStream);
            wsdlStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    /**
     * Transform a xml document to a string.
     *
     * @param document the xml document
     * @return the xml document as string
     */
    private static String xmlDocumentToString(Document document) {
        String wsdlString = null;
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            wsdlString = writer.getBuffer().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return wsdlString;
    }
}
