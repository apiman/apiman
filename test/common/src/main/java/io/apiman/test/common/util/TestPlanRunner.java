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

import io.apiman.gateway.engine.beans.util.CaseInsensitiveStringMultiMap;
import io.apiman.test.common.json.JsonArrayOrderingType;
import io.apiman.test.common.json.JsonCompare;
import io.apiman.test.common.json.JsonMissingFieldType;
import io.apiman.test.common.plan.TestGroupType;
import io.apiman.test.common.plan.TestPlan;
import io.apiman.test.common.plan.TestType;
import io.apiman.test.common.resttest.RestTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.http.request.ApacheRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs a test plan.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls", "javadoc" })
public class TestPlanRunner {

    //private static Logger logger = LoggerFactory.getLogger(TestPlanRunner.class);
    private static Logger logger = LogManager.getLogger(TestPlanRunner.class);
    private final CaseInsensitiveStringMultiMap testMetaHeaders = new CaseInsensitiveStringMultiMap();

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
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                    }
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
        String requestPath = TestUtil.doPropertyReplacement(restTest.getRequestPath());
        URI uri = null;
        try {
            uri = getUri(baseApiUrl, requestPath);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI", e);
        }

        log("Sending HTTP request to: " + uri);

        // Retries attempt to connect up to 4 times if there's an IOException
        // e.g. occasional CI issue connecting to localhost on first attempt)
        Request request = new ApacheRequest(uri.toString())
            .through(RetryIfUnableToConnect.class)
            .method(restTest.getRequestMethod());

        try {
            Map<String, String> requestHeaders = restTest.getRequestHeaders();
            for (Entry<String, String> entry : requestHeaders.entrySet()) {
                String value = TestUtil.doPropertyReplacement(entry.getValue());
                // Handle system properties that may be configured in the rest-test itself
                if (entry.getKey().startsWith("X-RestTest-System-Property")) {
                    String[] split = value.split("=");
                    System.setProperty(split[0], split[1]);
                    continue;
                }

                if (entry.getKey().equals("Content-Type")) {
                    String contentType = entry.getKey() != null ? StringUtils.appendIfMissing(value, "; charset=UTF-8") : "text/plain; charset=UTF-8";
                    request = request.header(entry.getKey(), contentType);
                } else {
                    request = request.header(entry.getKey(), value);
                }
            }

            // Set up basic auth
            String authorization = createBasicAuthorization(restTest.getUsername(), restTest.getPassword());
            if (authorization != null) {
                request = request.header("Authorization", authorization);
            }

            if (restTest.getRequestPayload() != null && !restTest.getRequestPayload().isEmpty()) {
                request = request.body().set(restTest.getRequestPayload()).back();
            }

            assertResponse(restTest, request.fetch());
        } catch (Error e) {
            logPlain("[ERROR] " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (ProtocolException e) {
            logPlain("[HTTP PROTOCOL EXCEPTION] " + e.getMessage());
            throw new Error(e);
        } catch (IOException e) {
            logPlain("[IO EXCEPTION] " + e.getMessage());
            throw new Error(e);
        } catch (Exception e) {
            logPlain("[EXCEPTION] " + e.getMessage());
            throw new Error(e);
        }
    }

    /**
     * Create the basic auth header value.
     *
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
     *
     * @param restTest
     * @param response
     */
    private void assertResponse(RestTest restTest, Response response) {
        int actualStatusCode = response.status();
        try {
            Assert.assertEquals("Unexpected REST response status code.  Status message: " + response.reason(), restTest.getExpectedStatusCode(),
                    actualStatusCode);
        } catch (Error e) {
            if (actualStatusCode >= 400) {
                InputStream content = null;
                try {
                    String payload = response.body();
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
            if (expectedHeaderName.startsWith("X-RestTest-")) {
                testMetaHeaders.put(entry.getKey(), entry.getValue());
                continue;
            }
            String expectedHeaderValue = entry.getValue();
            List<String> headers = response.headers().get(expectedHeaderName);

            Assert.assertNotNull("Expected header to exist but was not found: " + expectedHeaderName, headers);
            Assert.assertEquals("For response header " + expectedHeaderName, expectedHeaderValue, headers.get(0));
        }
        List<String> ctValueList = response.headers().get("Content-Type");
        if (ctValueList == null) {
            assertNoPayload(restTest, response);
        } else {
            String ctValueFirst = ctValueList.get(0);
            if (ctValueFirst.startsWith("application/json")) {
                assertJsonPayload(restTest, response);
            } else if (ctValueFirst.startsWith("text/plain") || ctValueFirst.startsWith("text/html")) {
                assertTextPayload(restTest, response);
            } else if (ctValueFirst.startsWith("application/xml") || ctValueFirst.startsWith("application/wsdl+xml") || ctValueFirst.startsWith("text/xml")) {
                assertXmlPayload(restTest, response);
            } else if (ctValueFirst.startsWith("image/")) {
                assertBinaryEquals(restTest, response);
            }
        }
    }

    private void assertBinaryEquals(RestTest restTest, Response response) {
        byte[] expected = restTest.getExpectedResponsePayload().getBytes(StandardCharsets.UTF_8);
        assertThat(response.binary()).isEqualTo(expected);
    }

    /**
     * Asserts that the response has no payload and that we are not expecting one.
     *
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
     * Assume the payload is JSON and do some assertions based on the configuration in the REST
     * Test.
     *
     * @param restTest
     * @param response
     */
    private void assertJsonPayload(RestTest restTest, Response response) {
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(response.binary());
            ObjectMapper jacksonParser = new ObjectMapper();
            JsonNode actualJson = jacksonParser.readTree(inputStream);
            bindVariables(actualJson, restTest);
            String expectedPayload = TestUtil.doPropertyReplacement(restTest.getExpectedResponsePayload());
            Assert.assertNotNull("REST Test missing expected JSON payload.", expectedPayload);
            JsonNode expectedJson = jacksonParser.readTree(expectedPayload);
            try {
                JsonCompare jsonCompare = new JsonCompare();
                jsonCompare.setArrayOrdering(JsonArrayOrderingType.fromString(restTest.getExpectedResponseHeaders().get("X-RestTest-ArrayOrdering")));
                jsonCompare.setIgnoreCase("true".equals(restTest.getExpectedResponseHeaders().get("X-RestTest-Assert-IgnoreCase")));
                jsonCompare.setCompareNumericIds("true".equals(restTest.getExpectedResponseHeaders().get("X-RestTest-Assert-NumericIds")));
                jsonCompare.setMissingField(
                        JsonMissingFieldType.fromString(restTest.getExpectedResponseHeaders().get("X-RestTest-Assert-MissingField")));
                jsonCompare.assertJson(expectedJson, actualJson);
            } catch (Error e) {
                System.out.println("--- START FAILED JSON PAYLOAD ---");
                System.out.println(actualJson.toString());
                System.out.println("--- END FAILED JSON PAYLOAD ---");
                throw e;
            }
        } catch (Exception e) {
            System.err.println("--- Exception ---");
            System.err.println(response.body());
            throw new Error(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * The payload is expected to be XML. Parse it and then use XmlUnit to compare the payload
     * with the expected payload (obviously also XML).
     *
     * @param restTest
     * @param response
     */
    private void assertXmlPayload(RestTest restTest, com.jcabi.http.Response response) {
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(response.binary());
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
                // compares the XML nodes. In this case, we're specially handling any
                // elements named "entry" so that we can compare the standard XML format
                // of the Echo API we use for most of our tests. The format of an
                // entry looks like:
                // <entry>
                // <key>Name</key>
                // <value>Value</value>
                // </entry>
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
     * Binds any variables found in the response JSON to system properties so they can be used
     * in later rest tests.
     *
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
            public Object setProperty(String name, Object contextObj, VariableResolverFactory variableFactory, Object value) {
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
     * Assume the payload is Text and do some assertions based on the configuration in the REST
     * Test.
     *
     * @param restTest
     * @param response
     */
    private void assertTextPayload(RestTest restTest, com.jcabi.http.Response response) {
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(response.binary());
            List<String> lines = IOUtils.readLines(inputStream);
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                // Replace single backslashes with double backslashes to escape them for the regex compiler.
                // These occur for example in Windows paths
                builder.append(line.replace("\\","\\\\")).append("\n");
            }

            String actual = builder.toString();
            String expected = restTest.getExpectedResponsePayload();

            // If Regex-Match header set on the request, we use this to signify that we want regex matching.
            boolean regexMatchMod = BooleanUtils.toBoolean(testMetaHeaders.get("X-RestTest-RegexMatching"));
            if (expected != null) {
                if (regexMatchMod) {
                    assertThat(actual)
                        .withFailMessage("Response payload (text/plain) mismatch. Expected: <%s> but was: <%s>", expected, actual)
                        .matches(expected);
                } else {
                    assertThat(actual).isEqualTo(expected);
                }
            }
        } catch (Exception e) {
            throw new Error(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Gets the absolute URL to use to invoke a rest API at a given path.
     *
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


    /**
     * Retry if unable to connect (used by jcabi, needs to be public as they use reflection magic).
     */
    public static final class RetryIfUnableToConnect implements Wire {

        private transient Wire origin;

        public RetryIfUnableToConnect(Wire wire) {
            this.origin = wire;
        }

        @Override
        public Response send(Request req, String home, String method,
            Collection<Entry<String, String>> headers, InputStream content, int connect, int read)
            throws IOException {
            int attempt = 0;
            while (true) {
                if (attempt > 3) {
                    throw new IOException(
                        String.format("Failed after %d attempts", attempt)
                    );
                }
                try {
                    return this.origin.send(
                        req, home, method, headers, content, connect, read
                    );
                } catch (final IOException ex) {
                    System.out.println("An IO issue occurred. Will try again momentarily: " + ex.getMessage());
                    ex.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                ++attempt;
            }
        }
    }

}
