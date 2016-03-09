/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.test.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.apiman.test.common.plan.TestGroupType;
import io.apiman.test.common.plan.TestPlan;
import io.apiman.test.common.plan.TestType;
import io.apiman.test.common.resttest.RestTest;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.mvel2.MVEL;
import org.mvel2.integration.PropertyHandler;
import org.mvel2.integration.PropertyHandlerFactory;
import org.mvel2.integration.VariableResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * Runs a test plan.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({"nls", "javadoc"})
public class TestPlanRunner {

    private static Logger logger = LoggerFactory.getLogger(TestPlanRunner.class);
    private OkHttpClient client = new OkHttpClient();
    {
        client.setFollowRedirects(false);
        client.setFollowSslRedirects(false);
    }

    /**
     * Constructor.
     */
    public TestPlanRunner() {
    }

    /**
     * Called to run a test plan.
     *
     * @param resourcePath
     * @param cl
     * @param baseApiUrl
     */
    public void runTestPlan(String resourcePath, ClassLoader cl, String baseApiUrl) {
        TestPlan testPlan = TestUtil.loadTestPlan(resourcePath, cl);
        log("");
        log("-------------------------------------------------------------------------------");
        log("Executing Test Plan: " + resourcePath);
        log("   Base API URL: " + baseApiUrl);
        log("-------------------------------------------------------------------------------");
        log("");
        for (TestGroupType group : testPlan.getTestGroup()) {
            log("-----------------------------------------------------------");
            log("Starting Test Group [{0}]", group.getName());
            log("-----------------------------------------------------------");

            for (TestType test : group.getTest()) {
                String rtPath = test.getValue();
                Integer delay = test.getDelay();
                log("Executing REST Test [{0}] - {1}", test.getName(), rtPath);
                if (delay != null) {
                    try { Thread.sleep(delay); } catch (InterruptedException e) { }
                }
                if (rtPath == null || rtPath.trim().isEmpty()) {
                    continue;
                }
                RestTest restTest = TestUtil.loadRestTest(rtPath, cl);
                runTest(restTest, baseApiUrl);
                log("REST Test Completed");
                log("+++++++++++++++++++");
            }

            log("Test Group [{0}] Completed Successfully", group.getName());
        }

        log("");
        log("-------------------------------------------------------------------------------");
        log("Test Plan successfully executed: " + resourcePath);
        log("-------------------------------------------------------------------------------");
        log("");
    }

    /**
     * Runs a single REST test.
     *
     * @param restTest
     * @param baseApiUrl
     * @throws Error
     */
    public void runTest(RestTest restTest, String baseApiUrl) throws Error {
        try {
            String requestPath = TestUtil.doPropertyReplacement(restTest.getRequestPath());
            URI uri = getUri(baseApiUrl, requestPath);
            String rawType = restTest.getRequestHeaders().get("Content-Type") != null ?
                    restTest.getRequestHeaders().get("Content-Type") : "text/plain; charset=UTF-8";
            MediaType mediaType = MediaType.parse(rawType);

            log("Sending HTTP request to: " + uri);

            RequestBody body = null;
            if (restTest.getRequestPayload() != null && !restTest.getRequestPayload().isEmpty()) {
                body = RequestBody.create(mediaType, restTest.getRequestPayload());
            }

            Request.Builder requestBuilder = new Request.Builder()
                    .url(uri.toString())
                    .method(restTest.getRequestMethod(), body);

            Map<String, String> requestHeaders = restTest.getRequestHeaders();
            for (Entry<String, String> entry : requestHeaders.entrySet()) {
                String value = TestUtil.doPropertyReplacement(entry.getValue());
                // Handle system properties that may be configured in the rest-test itself
                if (entry.getKey().startsWith("X-RestTest-System-Property")) {
                    String [] split = value.split("=");
                    System.setProperty(split[0], split[1]);
                    continue;
                }
                requestBuilder.addHeader(entry.getKey(), value);
            }

            // Set up basic auth
            String authorization = createBasicAuthorization(restTest.getUsername(), restTest.getPassword());
            if (authorization != null) {
                requestBuilder.addHeader("Authorization", authorization);
            }

            Response response = client.newCall(requestBuilder.build()).execute();
            assertResponse(restTest, response);
        } catch (Error e) {
            logPlain("[ERROR] " + e.getMessage());
            throw e;
        } catch (ProtocolException e) {
            logPlain("[HTTP PROTOCOL EXCEPTION]" + e.getMessage());
        } catch (Exception e) {
            logPlain("[EXCEPTION] " + e.getMessage());
            throw new Error(e);
        }
    }

    /**
     * Create the basic auth header value.
     * @param username
     * @param password
     */
    private String createBasicAuthorization(String username, String password) {
        if (username == null || username.trim().length() == 0) {
            return null;
        }
        username = TestUtil.doPropertyReplacement(username);
        password = TestUtil.doPropertyReplacement(password);
        String val = username + ":" + password;
        return "Basic " + Base64.encodeBase64String(val.getBytes()).trim();
    }

    /**
     * Assert that the response matched the expected.
     * @param restTest
     * @param response
     */
    private void assertResponse(RestTest restTest, Response response) {
        int actualStatusCode = response.code();
        try {
            Assert.assertEquals("Unexpected REST response status code.  Status message: "
                    + response.message(), restTest.getExpectedStatusCode(),
                    actualStatusCode);
        } catch (Error e) {
            if (actualStatusCode >= 400) {
                InputStream content = null;
                try {
                    String payload = response.body().string();
                    System.out.println("------ START ERROR PAYLOAD ------");
                    if (payload.startsWith("{")) {
                        payload = payload.replace("\\r\\n", "\r\n").replace("\\t", "\t");
                    }
                    System.out.println(payload);
                    System.out.println("------ END   ERROR PAYLOAD ------");
                } catch (Exception e1) {
                } finally {
                    IOUtils.closeQuietly(content);
                }
            }
            throw e;
        }
        for (Entry<String, String> entry : restTest.getExpectedResponseHeaders().entrySet()) {
            String expectedHeaderName = entry.getKey();
            if (expectedHeaderName.startsWith("X-RestTest-"))
                continue;
            String expectedHeaderValue = entry.getValue();
            String header = response.header(expectedHeaderName);

            Assert.assertNotNull("Expected header to exist but was not found: " + expectedHeaderName, header);
            Assert.assertEquals(expectedHeaderValue, header);
        }
        String ctValue = response.header("Content-Type");
        if (ctValue == null) {
            assertNoPayload(restTest, response);
        } else {
            if (ctValue.startsWith("application/json")) {
                assertJsonPayload(restTest, response);
            } else if (ctValue.startsWith("text/plain") || ctValue.startsWith("text/html")) {
                assertTextPayload(restTest, response);
            } else if (ctValue.startsWith("application/xml") || ctValue.startsWith("application/wsdl+xml")) {
                assertXmlPayload(restTest, response);
            } else {
                Assert.fail("Unsupported response payload type: " + ctValue);
            }
        }
    }

    /**
     * Asserts that the response has no payload and that we are not expecting one.
     * @param restTest
     * @param response
     */
    private void assertNoPayload(RestTest restTest, Response response) {
        String expectedPayload = restTest.getExpectedResponsePayload();
        if (expectedPayload != null && expectedPayload.trim().length() > 0) {
            Assert.fail("Expected a payload but didn't get one.");
        }
    }

    /**
     * Assume the payload is JSON and do some assertions based on the configuration
     * in the REST Test.
     * @param restTest
     * @param response
     */
    private void assertJsonPayload(RestTest restTest, Response response) {
        InputStream inputStream = null;
        try {
            inputStream = response.body().byteStream();
            ObjectMapper jacksonParser = new ObjectMapper();
            JsonNode actualJson = jacksonParser.readTree(inputStream);
            bindVariables(actualJson, restTest);
            String expectedPayload = TestUtil.doPropertyReplacement(restTest.getExpectedResponsePayload());
            Assert.assertNotNull("REST Test missing expected JSON payload.", expectedPayload);
            JsonNode expectedJson = jacksonParser.readTree(expectedPayload);
            try {
                assertJson(restTest, expectedJson, actualJson);
            } catch (Error e) {
                System.out.println("--- START FAILED JSON PAYLOAD ---");
                System.out.println(actualJson.toString());
                System.out.println("--- END FAILED JSON PAYLOAD ---");
                throw e;
            }
        } catch (Exception e) {
            throw new Error(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * The payload is expected to be XML.  Parse it and then use XmlUnit to compare
     * the payload with the expected payload (obviously also XML).
     * @param restTest
     * @param response
     */
    private void assertXmlPayload(RestTest restTest, Response response) {
        InputStream inputStream = null;
        try {
            inputStream = response.body().byteStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer);
            String xmlPayload = writer.toString();
            String expectedPayload = TestUtil.doPropertyReplacement(restTest.getExpectedResponsePayload());
            Assert.assertNotNull("REST Test missing expected XML payload.", expectedPayload);
            try {
                XMLUnit.setIgnoreComments(true);
                XMLUnit.setIgnoreAttributeOrder(true);
                XMLUnit.setIgnoreWhitespace(true);
                XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
                XMLUnit.setCompareUnmatched(false);
                Diff diff = new Diff(expectedPayload, xmlPayload);
                // A custom element qualifier allows us to customize how the diff engine
                // compares the XML nodes.  In this case, we're specially handling any
                // elements named "entry" so that we can compare the standard XML format
                // of the Echo API we use for most of our tests.  The format of an
                // entry looks like:
                //    <entry>
                //      <key>Name</key>
                //      <value>Value</value>
                //    </entry>
                diff.overrideElementQualifier(new ElementNameQualifier() {
                    @Override
                    public boolean qualifyForComparison(Element control, Element test) {
                        if (control == null || test == null) {
                            return super.qualifyForComparison(control, test);
                        }
                        if (control.getNodeName().equals("entry") && test.getNodeName().equals("entry")) {
                            String controlKeyName = control.getElementsByTagName("key").item(0).getTextContent();
                            String testKeyName = test.getElementsByTagName("key").item(0).getTextContent();
                            return controlKeyName.equals(testKeyName);
                        }
                        return super.qualifyForComparison(control, test);
                    }
                });
                diff.overrideDifferenceListener(new DifferenceListener() {
                    @Override
                    public void skippedComparison(Node control, Node test) {
                    }
                    @Override
                    public int differenceFound(Difference difference) {
                        String value = difference.getControlNodeDetail().getValue();
                        String tvalue = null;
                        if (difference.getControlNodeDetail().getNode() != null) {
                            tvalue = difference.getControlNodeDetail().getNode().getTextContent();
                        }
                        if ("*".equals(value) || "*".equals(tvalue)) {
                            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                        } else {
                            return RETURN_ACCEPT_DIFFERENCE;
                        }
                    }
                });
                XMLAssert.assertXMLEqual(null, diff, true);
            } catch (Error e) {
                System.out.println("--- START FAILED XML PAYLOAD ---");
                System.out.println(xmlPayload);
                System.out.println("--- END FAILED XML PAYLOAD ---");
                throw e;
            }
        } catch (Exception e) {
            throw new Error(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Binds any variables found in the response JSON to system properties
     * so they can be used in later rest tests.
     * @param actualJson
     * @param restTest
     */
    private void bindVariables(JsonNode actualJson, RestTest restTest) {
        for (String headerName : restTest.getExpectedResponseHeaders().keySet()) {
            if (headerName.startsWith("X-RestTest-BindTo-")) {
                String bindExpression = restTest.getExpectedResponseHeaders().get(headerName);
                String bindVarName = headerName.substring("X-RestTest-BindTo-".length());
                String bindValue = evaluate(bindExpression, actualJson);
                log("-- Binding value in response --");
                log("\tExpression: " + bindExpression);
                log("\t    To Var: " + bindVarName);
                log("\t New Value: " + bindValue);
                if (bindValue == null) {
                    System.clearProperty(bindVarName);
                } else {
                    System.setProperty(bindVarName, bindValue);
                }
            }
        }
    }

    /**
     * Evaluates the given expression against the given JSON object.
     *
     * @param bindExpression
     * @param json
     */
    private String evaluate(String bindExpression, final JsonNode json) {
        PropertyHandlerFactory.registerPropertyHandler(ObjectNode.class, new PropertyHandler() {
            @Override
            public Object setProperty(String name, Object contextObj, VariableResolverFactory variableFactory,
                    Object value) {
                throw new RuntimeException("Not supported!");
            }

            @Override
            public Object getProperty(String name, Object contextObj, VariableResolverFactory variableFactory) {
                ObjectNode node = (ObjectNode) contextObj;
                TestVariableResolver resolver = new TestVariableResolver(node, name);
                return resolver.getValue();
            }
        });
        return String.valueOf(MVEL.eval(bindExpression, new TestVariableResolverFactory(json)));
    }

    /**
     * Asserts that the JSON payload matches what we expected, as defined
     * in the configuration of the rest test.
     * @param restTest
     * @param expectedJson
     * @param actualJson
     */
    public void assertJson(RestTest restTest, JsonNode expectedJson, JsonNode actualJson) {
        if (expectedJson instanceof ArrayNode) {
            JsonNode actualValue = actualJson;
            ArrayNode expectedArray = (ArrayNode) expectedJson;
            Assert.assertEquals("Expected JSON array but found non-array ["
                    + actualValue.getClass().getSimpleName() + "] instead.", expectedJson.getClass(),
                    actualValue.getClass());
            ArrayNode actualArray = (ArrayNode) actualValue;
            Assert.assertEquals("Array size mismatch.", expectedArray.size(), actualArray.size());
            String ordering = restTest.getExpectedResponseHeaders().get("X-RestTest-ArrayOrdering");

            JsonNode [] expected = new JsonNode[expectedArray.size()];
            JsonNode [] actual = new JsonNode[actualArray.size()];
            for (int idx = 0; idx < expected.length; idx++) {
                expected[idx] = expectedArray.get(idx);
                actual[idx] = actualArray.get(idx);
            }
            // If strict ordering is disabled, then sort both arrays
            if ("any".equals(ordering)) {
                Comparator<? super JsonNode> comparator = new Comparator<JsonNode>() {
                    @Override
                    public int compare(JsonNode o1, JsonNode o2) {
                        int cmp = o1.toString().compareTo(o2.toString());
                        if (cmp == 0)
                            cmp = 1;
                        return cmp;
                    }
                };
                Arrays.sort(expected, comparator);
                Arrays.sort(actual, comparator);
            }
            for (int idx = 0; idx < expected.length; idx++) {
                assertJson(restTest, expected[idx], actual[idx]);
            }
        } else {
            Iterator<Entry<String, JsonNode>> fields = expectedJson.fields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> entry = fields.next();
                String expectedFieldName = entry.getKey();
                JsonNode expectedValue = entry.getValue();
                if (expectedValue instanceof TextNode) {
                    TextNode tn = (TextNode) expectedValue;
                    String expected = tn.textValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);

                    if (isAssertionIgnoreCase(restTest)) {
                        expected = expected.toLowerCase();
                        if (actualValue == null) {
                            actualValue = actualJson.get(expectedFieldName.toLowerCase());
                        }
                    }

                    Assert.assertNotNull("Expected JSON text field '" + expectedFieldName + "' with value '"
                            + expected + "' but was not found.", actualValue);
                    Assert.assertEquals("Expected JSON text field '" + expectedFieldName + "' with value '"
                            + expected + "' but found non-text [" + actualValue.getClass().getSimpleName()
                            + "] field with that name instead.", TextNode.class, actualValue.getClass());
                    String actual = ((TextNode) actualValue).textValue();

                    if (isAssertionIgnoreCase(restTest)) {
                        if (actual != null) {
                            actual = actual.toLowerCase();
                        }
                    }

                    if (!expected.equals("*")) {
                        Assert.assertEquals("Value mismatch for text field '" + expectedFieldName + "'.", expected,
                                actual);
                    }
                } else if (expectedValue instanceof NumericNode) {
                    NumericNode numeric = (NumericNode) expectedValue;
                    Number expected = numeric.numberValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected JSON numeric field '" + expectedFieldName + "' with value '"
                            + expected + "' but was not found.", actualValue);
                    Assert.assertEquals("Expected JSON numeric field '" + expectedFieldName + "' with value '"
                            + expected + "' but found non-numeric [" + actualValue.getClass().getSimpleName()
                            + "] field with that name instead.", expectedValue.getClass(), actualValue.getClass());
                    Number actual = ((NumericNode) actualValue).numberValue();
                    Assert.assertEquals("Value mismatch for numeric field '" + expectedFieldName + "'.", expected,
                            actual);
                } else if (expectedValue instanceof BooleanNode) {
                    BooleanNode bool = (BooleanNode) expectedValue;
                    Boolean expected = bool.booleanValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected JSON boolean field '" + expectedFieldName + "' with value '"
                            + expected + "' but was not found.", actualValue);
                    Assert.assertEquals("Expected JSON boolean field '" + expectedFieldName + "' with value '"
                            + expected + "' but found non-boolean [" + actualValue.getClass().getSimpleName()
                            + "] field with that name instead.", expectedValue.getClass(), actualValue.getClass());
                    Boolean actual = ((BooleanNode) actualValue).booleanValue();
                    Assert.assertEquals("Value mismatch for boolean field '" + expectedFieldName + "'.", expected,
                            actual);
                } else if (expectedValue instanceof ObjectNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected parent JSON field '" + expectedFieldName
                            + "' but was not found.", actualValue);
                    Assert.assertEquals("Expected parent JSON field '" + expectedFieldName
                            + "' but found field of type '" + actualValue.getClass().getSimpleName() + "'.",
                            ObjectNode.class, actualValue.getClass());
                    assertJson(restTest, expectedValue, actualValue);
                } else if (expectedValue instanceof ArrayNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected JSON array field '" + expectedFieldName
                            + "' but was not found.", actualValue);
                    ArrayNode expectedArray = (ArrayNode) expectedValue;
                    Assert.assertEquals("Expected JSON array field '" + expectedFieldName
                            + "' but found non-array [" + actualValue.getClass().getSimpleName()
                            + "] field with that name instead.", expectedValue.getClass(), actualValue.getClass());
                    ArrayNode actualArray = (ArrayNode) actualValue;
                    Assert.assertEquals("Field '" + expectedFieldName + "' array size mismatch.",
                            expectedArray.size(), actualArray.size());
                    assertJson(restTest, expectedArray, actualArray);
                } else if (expectedValue instanceof NullNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected Null JSON field '" + expectedFieldName
                            + "' but was not found.", actualValue);
                    Assert.assertEquals("Expected Null JSON field '" + expectedFieldName
                            + "' but found field of type '" + actualValue.getClass().getSimpleName() + "'.",
                            NullNode.class, actualValue.getClass());
                } else {
                    Assert.fail("Unsupported field type: " + expectedValue.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * @param restTest
     */
    private boolean isAssertionIgnoreCase(RestTest restTest) {
        return "true".equals(restTest.getExpectedResponseHeaders().get("X-RestTest-Assert-IgnoreCase"));
    }

    /**
     * Assume the payload is Text and do some assertions based on the configuration
     * in the REST Test.
     * @param restTest
     * @param response
     */
    private void assertTextPayload(RestTest restTest, Response response) {
        InputStream inputStream = null;
        try {
            inputStream = response.body().byteStream();
            List<String> lines = IOUtils.readLines(inputStream);
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                builder.append(line).append("\n");
            }

            String actual = builder.toString();
            String expected = restTest.getExpectedResponsePayload();
            if (expected != null) {
                Assert.assertEquals("Response payload (text/plain) mismatch.", expected, actual);
            }
        } catch (Exception e) {
            throw new Error(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Gets the absolute URL to use to invoke a rest API at a given path.
     * @param path
     * @throws URISyntaxException
     */
    public URI getUri(String baseApiUrl, String path) throws URISyntaxException {
        if (baseApiUrl.endsWith("/")) {
            baseApiUrl = baseApiUrl.substring(0, baseApiUrl.length() - 1);
        }
        if (path == null) {
            return new URI(baseApiUrl);
        } else {
            return new URI(baseApiUrl + path);
        }
    }

    /**
     * Logs a message.
     *
     * @param message
     * @param params
     */
    private void log(String message, Object... params) {
        String outmsg = MessageFormat.format(message, params);
        logger.info("    >> " + outmsg);
    }

    /**
     * Logs a message.
     *
     * @param message
     */
    private void logPlain(String message) {
        logger.info("    >> " + message);
    }
}
