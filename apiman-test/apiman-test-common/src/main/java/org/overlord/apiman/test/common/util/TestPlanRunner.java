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
package org.overlord.apiman.test.common.util;

import java.io.InputStream;
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
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.NumericNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.junit.Assert;
import org.overlord.apiman.test.common.plan.TestGroupType;
import org.overlord.apiman.test.common.plan.TestPlan;
import org.overlord.apiman.test.common.plan.TestType;
import org.overlord.apiman.test.common.resttest.RestTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a test plan.
 *
 * @author eric.wittmann@redhat.com
 */
public class TestPlanRunner {

    private static Logger logger = LoggerFactory.getLogger(TestPlanRunner.class);

    private String baseApiUrl;

    /**
     * Constructor.
     * @param baseApiUrl
     */
    public TestPlanRunner(String baseApiUrl) {
        this.baseApiUrl = baseApiUrl;
    }

    /**
     * Called to run a test plan.
     *
     * @param resourcePath
     * @param cl
     */
    public void runTestPlan(String resourcePath, ClassLoader cl) {
        TestPlan testPlan = TestUtil.loadTestPlan(resourcePath, cl);
        log(""); //$NON-NLS-1$
        log("-------------------------------------------------------------------------------"); //$NON-NLS-1$
        log("Executing Test Plan: " + resourcePath); //$NON-NLS-1$
        log("   Base API URL: " + baseApiUrl); //$NON-NLS-1$
        log("-------------------------------------------------------------------------------"); //$NON-NLS-1$
        log(""); //$NON-NLS-1$
        for (TestGroupType group : testPlan.getTestGroup()) {
            log("-----------------------------------------------------------"); //$NON-NLS-1$
            log("Starting Test Group [{0}]", group.getName()); //$NON-NLS-1$
            log("-----------------------------------------------------------"); //$NON-NLS-1$

            for (TestType test : group.getTest()) {
                String rtPath = test.getValue();
                log("Executing REST Test [{0}] - {1}", test.getName(), rtPath); //$NON-NLS-1$
                RestTest restTest = TestUtil.loadRestTest(rtPath, cl);
                runTest(restTest);
                log("REST Test Completed"); //$NON-NLS-1$
                log("+++++++++++++++++++"); //$NON-NLS-1$
            }

            log("Test Group [{0}] Completed Successfully", group.getName()); //$NON-NLS-1$
        }

        log(""); //$NON-NLS-1$
        log("-------------------------------------------------------------------------------"); //$NON-NLS-1$
        log("Test Plan successfully executed: " + resourcePath); //$NON-NLS-1$
        log("-------------------------------------------------------------------------------"); //$NON-NLS-1$
        log(""); //$NON-NLS-1$
    }

    /**
     * Runs a single REST test.
     *
     * @param restTest
     */
    private void runTest(RestTest restTest) throws Error {
        try {
            String requestPath = TestUtil.doPropertyReplacement(restTest.getRequestPath());
            URI uri = getUri(requestPath);
            HttpRequestBase request = null;
            if (restTest.getRequestMethod().equalsIgnoreCase("GET")) { //$NON-NLS-1$
                request = new HttpGet();
            } else if (restTest.getRequestMethod().equalsIgnoreCase("POST")) { //$NON-NLS-1$
                request = new HttpPost();
                HttpEntity entity = new StringEntity(restTest.getRequestPayload());
                ((HttpPost) request).setEntity(entity);
            } else if (restTest.getRequestMethod().equalsIgnoreCase("PUT")) { //$NON-NLS-1$
                request = new HttpPut();
                HttpEntity entity = new StringEntity(restTest.getRequestPayload());
                ((HttpPut) request).setEntity(entity);
            } else if (restTest.getRequestMethod().equalsIgnoreCase("DELETE")) { //$NON-NLS-1$
                request = new HttpDelete();
            }
            if (request == null) {
                Assert.fail("Unsupported method in REST Test: " + restTest.getRequestMethod()); //$NON-NLS-1$
            }
            request.setURI(uri);

            Map<String, String> requestHeaders = restTest.getRequestHeaders();
            for (Entry<String, String> entry : requestHeaders.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }

            // Set up basic auth
            String authorization = createBasicAuthorization(restTest.getUsername(), restTest.getPassword());
            if (authorization != null) {
                request.setHeader("Authorization", authorization); //$NON-NLS-1$
            }

            DefaultHttpClient client = new DefaultHttpClient();

            HttpResponse response = client.execute(request);
            assertResponse(restTest, response);
        } catch (Error e) {
            logPlain("[ERROR] " + e.getMessage()); //$NON-NLS-1$
            throw e;
        } catch (Exception e) {
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
        String val = username + ":" + password; //$NON-NLS-1$
        return "Basic " + Base64.encodeBase64String(val.getBytes()).trim(); //$NON-NLS-1$
    }

    /**
     * Assert that the response matched the expected.
     * @param restTest
     * @param response
     */
    private void assertResponse(RestTest restTest, HttpResponse response) {
        int actualStatusCode = response.getStatusLine().getStatusCode();
        try {
            Assert.assertEquals("Unexpected REST response status code.  Status message: " //$NON-NLS-1$
                    + response.getStatusLine().getReasonPhrase(), restTest.getExpectedStatusCode(),
                    actualStatusCode);
        } catch (Error e) {
            if (actualStatusCode >= 400) {
                try {
                    InputStream content = response.getEntity().getContent();
                    String payload = IOUtils.toString(content);
                    System.out.println("------ START ERROR PAYLOAD ------"); //$NON-NLS-1$
                    System.out.println(payload);
                    System.out.println("------ END   ERROR PAYLOAD ------"); //$NON-NLS-1$
                } catch (Exception e1) {
                }
            }
            throw e;
        }
        for (Entry<String, String> entry : restTest.getExpectedResponseHeaders().entrySet()) {
            String expectedHeaderName = entry.getKey();
            if (expectedHeaderName.startsWith("X-RestTest-")) //$NON-NLS-1$
                continue;
            String expectedHeaderValue = entry.getValue();
            Header header = response.getFirstHeader(expectedHeaderName);
            Assert.assertNotNull("Expected header to exist but was not found: " + expectedHeaderName, header); //$NON-NLS-1$
            String actualValue = header.getValue();
            Assert.assertEquals(expectedHeaderValue, actualValue);
        }
        Header ctHeader = response.getFirstHeader("Content-Type"); //$NON-NLS-1$
        if (ctHeader == null) {
            assertNoPayload(restTest, response);
        } else {
            String ct = ctHeader.getValue();
            if (ct.equals("application/json")) { //$NON-NLS-1$
                assertJsonPayload(restTest, response);
            } else if (ct.equals("text/plain")) { //$NON-NLS-1$
                assertTextPayload(restTest, response);
            } else {
                Assert.fail("Unsupported response payload type: " + ct); //$NON-NLS-1$
            }
        }
    }

    /**
     * Asserts that the response has no payload and that we are not expecting one.
     * @param restTest
     * @param response
     */
    private void assertNoPayload(RestTest restTest, HttpResponse response) {
        String expectedPayload = restTest.getExpectedResponsePayload();
        if (expectedPayload != null && expectedPayload.trim().length() > 0) {
            Assert.fail("Expected a payload but didn't get one."); //$NON-NLS-1$
        }
    }

    /**
     * Assume the payload is JSON and do some assertions based on the configuration
     * in the REST Test.
     * @param restTest
     * @param response
     */
    private void assertJsonPayload(RestTest restTest, HttpResponse response) {
        InputStream inputStream = null;
        try {
            inputStream = response.getEntity().getContent();
            ObjectMapper jacksonParser = new ObjectMapper();
            JsonNode actualJson = jacksonParser.readTree(inputStream);
            bindVariables(actualJson, restTest);
            String expectedPayload = TestUtil.doPropertyReplacement(restTest.getExpectedResponsePayload());
            Assert.assertNotNull("REST Test missing expected JSON payload.", expectedPayload); //$NON-NLS-1$
            JsonNode expectedJson = jacksonParser.readTree(expectedPayload);
            try {
                assertJson(restTest, expectedJson, actualJson);
            } catch (Error e) {
                System.out.println("--- START FAILED JSON PAYLOAD ---"); //$NON-NLS-1$
                System.out.println(actualJson.toString());
                System.out.println("--- END FAILED JSON PAYLOAD ---"); //$NON-NLS-1$
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
            if (headerName.startsWith("X-RestTest-BindTo-")) { //$NON-NLS-1$
                String bindExpression = restTest.getExpectedResponseHeaders().get(headerName);
                String bindVarName = headerName.substring("X-RestTest-BindTo-".length()); //$NON-NLS-1$
                String bindValue = evaluate(bindExpression, actualJson);
                log("-- Binding value in response --"); //$NON-NLS-1$
                log("\tExpression: " + bindExpression); //$NON-NLS-1$
                log("\t    To Var: " + bindVarName); //$NON-NLS-1$
                log("\t New Value: " + bindValue); //$NON-NLS-1$
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
     * TODO replace with MVEL
     * 
     * @param bindExpression
     * @param json
     */
    private String evaluate(String bindExpression, JsonNode json) {
        String [] segments = bindExpression.split("\\."); //$NON-NLS-1$
        JsonNode currentNode = json;
        for (String segment : segments) {
            if (segment.startsWith("$[")) { //$NON-NLS-1$
                throw new RuntimeException("Not yet implemented: bind value in array response."); //$NON-NLS-1$
            } else if ("$".equals(segment)) { //$NON-NLS-1$
                currentNode = json;
            } else {
                if (segment.contains("[")) { //$NON-NLS-1$
                    throw new RuntimeException("Not yet implemented: bind value from array."); //$NON-NLS-1$
                }
                currentNode = currentNode.get(segment);
                if (currentNode == null) {
                    return null;
                }
            }
        }
        return currentNode.asText();
    }

    /**
     * Asserts that the JSON payload matches what we expected, as defined
     * in the configuration of the rest test.
     * @param restTest
     * @param expectedJson
     * @param actualJson
     */
    private void assertJson(RestTest restTest, JsonNode expectedJson, JsonNode actualJson) {
        if (expectedJson instanceof ArrayNode) {
            JsonNode actualValue = actualJson;
            ArrayNode expectedArray = (ArrayNode) expectedJson;
            Assert.assertEquals("Expected JSON array but found non-array [" //$NON-NLS-1$
                    + actualValue.getClass().getSimpleName() + "] instead.", expectedJson.getClass(), //$NON-NLS-1$
                    actualValue.getClass());
            ArrayNode actualArray = (ArrayNode) actualValue;
            Assert.assertEquals("Array size mismatch.", expectedArray.size(), actualArray.size()); //$NON-NLS-1$
            String ordering = restTest.getExpectedResponseHeaders().get("X-RestTest-ArrayOrdering"); //$NON-NLS-1$

            JsonNode [] expected = new JsonNode[expectedArray.size()];
            JsonNode [] actual = new JsonNode[actualArray.size()];
            for (int idx = 0; idx < expected.length; idx++) {
                expected[idx] = expectedArray.get(idx);
                actual[idx] = actualArray.get(idx);
            }
            // If strict ordering is disabled, then sort both arrays
            if ("any".equals(ordering)) { //$NON-NLS-1$
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
            Iterator<Entry<String, JsonNode>> fields = expectedJson.getFields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> entry = fields.next();
                String expectedFieldName = entry.getKey();
                JsonNode expectedValue = entry.getValue();
                if (expectedValue instanceof TextNode) {
                    TextNode tn = (TextNode) expectedValue;
                    String expected = tn.getTextValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected JSON text field '" + expectedFieldName + "' with value '" //$NON-NLS-1$ //$NON-NLS-2$
                            + expected + "' but was not found.", actualValue); //$NON-NLS-1$
                    Assert.assertEquals("Expected JSON text field '" + expectedFieldName + "' with value '" //$NON-NLS-1$ //$NON-NLS-2$
                            + expected + "' but found non-text [" + actualValue.getClass().getSimpleName() //$NON-NLS-1$
                            + "] field with that name instead.", TextNode.class, actualValue.getClass()); //$NON-NLS-1$
                    String actual = ((TextNode) actualValue).getTextValue();
                    Assert.assertEquals("Value mismatch for text field '" + expectedFieldName + "'.", expected, //$NON-NLS-1$ //$NON-NLS-2$
                            actual);
                } else if (expectedValue instanceof NumericNode) {
                    NumericNode numeric = (NumericNode) expectedValue;
                    Number expected = numeric.getNumberValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected JSON numeric field '" + expectedFieldName + "' with value '" //$NON-NLS-1$ //$NON-NLS-2$
                            + expected + "' but was not found.", actualValue); //$NON-NLS-1$
                    Assert.assertEquals("Expected JSON numeric field '" + expectedFieldName + "' with value '" //$NON-NLS-1$ //$NON-NLS-2$
                            + expected + "' but found non-numeric [" + actualValue.getClass().getSimpleName() //$NON-NLS-1$
                            + "] field with that name instead.", expectedValue.getClass(), actualValue.getClass()); //$NON-NLS-1$
                    Number actual = ((NumericNode) actualValue).getNumberValue();
                    Assert.assertEquals("Value mismatch for numeric field '" + expectedFieldName + "'.", expected, //$NON-NLS-1$ //$NON-NLS-2$
                            actual);
                } else if (expectedValue instanceof BooleanNode) {
                    BooleanNode bool = (BooleanNode) expectedValue;
                    Boolean expected = bool.getBooleanValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected JSON boolean field '" + expectedFieldName + "' with value '" //$NON-NLS-1$ //$NON-NLS-2$
                            + expected + "' but was not found.", actualValue); //$NON-NLS-1$
                    Assert.assertEquals("Expected JSON boolean field '" + expectedFieldName + "' with value '" //$NON-NLS-1$ //$NON-NLS-2$
                            + expected + "' but found non-boolean [" + actualValue.getClass().getSimpleName() //$NON-NLS-1$
                            + "] field with that name instead.", expectedValue.getClass(), actualValue.getClass()); //$NON-NLS-1$
                    Boolean actual = ((BooleanNode) actualValue).getBooleanValue();
                    Assert.assertEquals("Value mismatch for boolean field '" + expectedFieldName + "'.", expected, //$NON-NLS-1$ //$NON-NLS-2$
                            actual);
                } else if (expectedValue instanceof ObjectNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected parent JSON field '" + expectedFieldName //$NON-NLS-1$
                            + "' but was not found.", actualValue); //$NON-NLS-1$
                    Assert.assertEquals("Expected parent JSON field '" + expectedFieldName //$NON-NLS-1$
                            + "' but found field of type '" + actualValue.getClass().getSimpleName() + "'.", //$NON-NLS-1$ //$NON-NLS-2$
                            ObjectNode.class, actualValue.getClass());
                    assertJson(restTest, expectedValue, actualValue);
                } else if (expectedValue instanceof ArrayNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected JSON array field '" + expectedFieldName //$NON-NLS-1$
                            + "' but was not found.", actualValue); //$NON-NLS-1$
                    ArrayNode expectedArray = (ArrayNode) expectedValue;
                    Assert.assertEquals("Expected JSON array field '" + expectedFieldName //$NON-NLS-1$
                            + "' but found non-array [" + actualValue.getClass().getSimpleName() //$NON-NLS-1$
                            + "] field with that name instead.", expectedValue.getClass(), actualValue.getClass()); //$NON-NLS-1$
                    ArrayNode actualArray = (ArrayNode) actualValue;
                    Assert.assertEquals("Field '" + expectedFieldName + "' array size mismatch.", //$NON-NLS-1$ //$NON-NLS-2$
                            expectedArray.size(), actualArray.size());
                    assertJson(restTest, expectedArray, actualArray);
                } else if (expectedValue instanceof NullNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull("Expected Null JSON field '" + expectedFieldName //$NON-NLS-1$
                            + "' but was not found.", actualValue); //$NON-NLS-1$
                    Assert.assertEquals("Expected Null JSON field '" + expectedFieldName //$NON-NLS-1$
                            + "' but found field of type '" + actualValue.getClass().getSimpleName() + "'.", //$NON-NLS-1$ //$NON-NLS-2$
                            NullNode.class, actualValue.getClass());
                } else {
                    Assert.fail("Unsupported field type: " + expectedValue.getClass().getSimpleName()); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Assume the payload is Text and do some assertions based on the configuration
     * in the REST Test.
     * @param restTest
     * @param response
     */
    private void assertTextPayload(RestTest restTest, HttpResponse response) {
        InputStream inputStream = null;
        try {
            inputStream = response.getEntity().getContent();
            List<String> lines = IOUtils.readLines(inputStream);
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                builder.append(line).append("\n"); //$NON-NLS-1$
            }

            String actual = builder.toString();
            String expected = restTest.getExpectedResponsePayload();
            Assert.assertEquals("Response payload (text/plain) mismatch.", expected, actual); //$NON-NLS-1$
        } catch (Exception e) {
            throw new Error(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Gets the absolute URL to use to invoke a rest service at a given path.
     * @param path
     * @throws URISyntaxException
     */
    public URI getUri(String path) throws URISyntaxException {
        return new URI(baseApiUrl + path);
    }

    /**
     * Logs a message.
     *
     * @param message
     * @param params
     */
    private void log(String message, Object... params) {
        String outmsg = MessageFormat.format(message, params);
        logger.info("    >> " + outmsg); //$NON-NLS-1$
    }

    /**
     * Logs a message.
     *
     * @param message
     */
    private void logPlain(String message) {
        logger.info("    >> " + message); //$NON-NLS-1$
    }

}
