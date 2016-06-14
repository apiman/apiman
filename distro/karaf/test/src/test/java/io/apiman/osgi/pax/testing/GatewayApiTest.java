/**
 *
 */
package io.apiman.osgi.pax.testing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.squareup.okhttp.*;
import io.apiman.osgi.pax.testing.util.ElasticSearchEmbed;
import io.apiman.test.common.echo.EchoServer;
import io.apiman.test.common.plan.TestGroupType;
import io.apiman.test.common.plan.TestPlan;
import io.apiman.test.common.plan.TestType;
import io.apiman.test.common.resttest.RestTest;
import io.apiman.test.common.util.TestUtil;
import io.apiman.test.common.util.TestVariableResolver;
import io.apiman.test.common.util.TestVariableResolverFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mvel2.MVEL;
import org.mvel2.integration.PropertyHandler;
import org.mvel2.integration.PropertyHandlerFactory;
import org.mvel2.integration.VariableResolverFactory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.net.ssl.*;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.*;

import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * Test Gateway API
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayApiTest extends TestPlanUtil {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayApiTest.class);
    private static final String baseApiUrl = "https://localhost:8444/apiman-gateway-api/";
    protected static final int ECHO_PORT = 7654;
    private final String Plan_To_Test = "test-plans/api/api-testPlan.xml";
    private EchoServer echoServer = new EchoServer(ECHO_PORT);

    private Bundle warBundle;
    private ElasticSearchEmbed es;
    private Map<String, TestInfo> restTests;

    private OkHttpClient httpClient;

    @Configuration public Option[] config() {
        return combine(baseConfig());
    }

    @Test
    public void test1() throws Exception {
        RestTest restTest = TestUtil.loadRestTest(restTests.get("Publish Api").test.getValue(),
                GatewayApiTest.class.getClassLoader());
        if(LOG.isDebugEnabled()){
            LOG.debug("User : " + restTest.getUsername() + ", pwd : " + restTest.getPassword());
            LOG.debug("Rest Path : " + restTest.getRequestPath() + ", Method : " + restTest.getRequestMethod()
                    + ", Payload : " + restTest.getRequestPayload());
        }
        runTest(restTest, baseApiUrl);
    }

    @Test
    public void test2() throws Exception {
        RestTest restTest = TestUtil.loadRestTest(restTests.get("Publish Duplicate Api").test.getValue(),
                GatewayApiTest.class.getClassLoader());
        runTest(restTest, baseApiUrl);
    }

    @Test
    public void test3() throws Exception {
        RestTest restTest = TestUtil.loadRestTest(restTests.get("Publish Api 2.0").test.getValue(),
                GatewayApiTest.class.getClassLoader());
        runTest(restTest, baseApiUrl);
    }

    @Test
    public void test4() throws Exception {
        RestTest restTest = TestUtil.loadRestTest(restTests.get("Publish Api 3.0").test.getValue(),
                GatewayApiTest.class.getClassLoader());
        runTest(restTest, baseApiUrl);
    }

    @Test
    public void test5() throws Exception {
        RestTest restTest = TestUtil.loadRestTest(restTests.get("Retire Api 3.0").test.getValue(),
                GatewayApiTest.class.getClassLoader());
        runTest(restTest, baseApiUrl);
    }

    @Test
    public void test6() throws Exception {
        RestTest restTest = TestUtil.loadRestTest(restTests.get("Get Api Endpoint").test.getValue(),
                GatewayApiTest.class.getClassLoader());
        runTest(restTest, baseApiUrl);
    }

    @Test
    public void test7() throws Exception {
        RestTest restTest = TestUtil.loadRestTest(restTests.get("Republish Api 3.0").test.getValue(),
                GatewayApiTest.class.getClassLoader());
        runTest(restTest, baseApiUrl);
    }

    @Test
    public void test8() throws Exception {
        RestTest restTest = TestUtil.loadRestTest(restTests.get("Retire Api 3.0 (again)").test.getValue(),
                GatewayApiTest.class.getClassLoader());
        runTest(restTest, baseApiUrl);
    }

    @Before
    public void setUp() throws Exception {

        es = new ElasticSearchEmbed();
        es.launch();

        // Configure the okHTTPClient
        createHTTPClient();

        // Define the endpoint of the echo server
        System.setProperty("apiman-gateway-test.endpoints.echo", getEchoTestEndpoint());

        // Start Echo Server
        echoServer.start();

        // Load Test Plan, Test Group and TestType
        loadTestPlans(Plan_To_Test);

        // Create TestInfo from XML Plan loaded
        restTests = new HashMap<String, TestInfo>();
        for (TestPlanUtil.TestPlanInfo planInfo : testPlans) {
            TestPlan plan = planInfo.plan;
            List<TestGroupType> groups = plan.getTestGroup();
            for (TestGroupType group : groups) {
                List<TestType> testTypeList = group.getTest();
                for (TestType test : testTypeList) {
                    TestInfo testInfo = new TestInfo();
                    if (testPlans.size() > 1) {
                        testInfo.name = planInfo.name + " / " + test.getName();
                    } else {
                        testInfo.name = test.getName();
                    }
                    testInfo.plan = planInfo;
                    testInfo.group = group;
                    testInfo.test = test;
                    restTests.put(testInfo.name, testInfo);
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            for (TestInfo info : restTests.values()) {
                LOG.debug("Test Name : " + info.name + " of the Group : " + info.group.getName()
                        + " AND Plan : " + info.plan.name);
            }
        }

        // Init OSGI HTTP Listener
        initWebListener();
        final String bundlePath = "mvn:io.apiman/apiman-gateway-osgi-api/1.2.2-SNAPSHOT";
        warBundle = installAndStartBundle(bundlePath);
        waitForWebListener();
    }

    @After public void tearDown() throws Exception {
        if (warBundle != null) {
            warBundle.stop();
            warBundle.uninstall();
        }
        // Stop Echo Server
        echoServer.stop();
    }

    /**
     * Create OK HTTP Client
     */
    public void createHTTPClient() throws KeyManagementException {

        httpClient = new OkHttpClient();
        httpClient.setFollowRedirects(false);
        httpClient.setFollowSslRedirects(false);

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
                }
            } };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            httpClient.setSslSocketFactory(sslSocketFactory);
            httpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                    restTest.getRequestHeaders().get("Content-Type") :
                    "text/plain; charset=UTF-8";
            MediaType mediaType = MediaType.parse(rawType);

            LOG.info("Sending HTTP request to: " + uri);

            RequestBody body = null;
            if (restTest.getRequestPayload() != null && !restTest.getRequestPayload().isEmpty()) {
                body = RequestBody.create(mediaType, restTest.getRequestPayload());
            }

            Request.Builder requestBuilder = new Request.Builder().url(uri.toString())
                    .method(restTest.getRequestMethod(), body);

            Map<String, String> requestHeaders = restTest.getRequestHeaders();
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                String value = TestUtil.doPropertyReplacement(entry.getValue());
                // Handle system properties that may be configured in the rest-test itself
                if (entry.getKey().startsWith("X-RestTest-System-Property")) {
                    String[] split = value.split("=");
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

            Response response = httpClient.newCall(requestBuilder.build()).execute();
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
        int actualStatusCode = response.code();
        try {
            Assert.assertEquals(
                    "Unexpected REST response status code.  Status message: " + response.message(),
                    restTest.getExpectedStatusCode(), actualStatusCode);
        } catch (Error e) {
            if (actualStatusCode >= 400) {
                InputStream content = null;
                try {
                    String payload = response.body().string();
                    LOG.info("------ START ERROR PAYLOAD ------");
                    if (payload.startsWith("{")) {
                        payload = payload.replace("\\r\\n", "\r\n").replace("\\t", "\t");
                    }
                    LOG.info(payload);
                    LOG.info("------ END   ERROR PAYLOAD ------");
                } catch (Exception e1) {
                } finally {
                    IOUtils.closeQuietly(content);
                }
            }
            throw e;
        }
        for (Map.Entry<String, String> entry : restTest.getExpectedResponseHeaders().entrySet()) {
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
     * Assume the payload is JSON and do some assertions based on the configuration
     * in the REST Test.
     *
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
                LOG.info("--- START FAILED JSON PAYLOAD ---");
                LOG.info(actualJson.toString());
                LOG.info("--- END FAILED JSON PAYLOAD ---");
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
     *
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
                    @Override public boolean qualifyForComparison(Element control, Element test) {
                        if (control == null || test == null) {
                            return super.qualifyForComparison(control, test);
                        }
                        if (control.getNodeName().equals("entry") && test.getNodeName().equals("entry")) {
                            String controlKeyName = control.getElementsByTagName("key").item(0)
                                    .getTextContent();
                            String testKeyName = test.getElementsByTagName("key").item(0).getTextContent();
                            return controlKeyName.equals(testKeyName);
                        }
                        return super.qualifyForComparison(control, test);
                    }
                });
                diff.overrideDifferenceListener(new DifferenceListener() {
                    @Override public void skippedComparison(Node control, Node test) {
                    }

                    @Override public int differenceFound(Difference difference) {
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
                LOG.info("--- START FAILED XML PAYLOAD ---");
                LOG.info(xmlPayload);
                LOG.info("--- END FAILED XML PAYLOAD ---");
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
            @Override public Object setProperty(String name, Object contextObj,
                    VariableResolverFactory variableFactory, Object value) {
                throw new RuntimeException("Not supported!");
            }

            @Override public Object getProperty(String name, Object contextObj,
                    VariableResolverFactory variableFactory) {
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
     *
     * @param restTest
     * @param expectedJson
     * @param actualJson
     */
    public void assertJson(RestTest restTest, JsonNode expectedJson, JsonNode actualJson) {
        if (expectedJson instanceof ArrayNode) {
            JsonNode actualValue = actualJson;
            ArrayNode expectedArray = (ArrayNode) expectedJson;
            Assert.assertEquals(
                    "Expected JSON array but found non-array [" + actualValue.getClass().getSimpleName()
                            + "] instead.", expectedJson.getClass(), actualValue.getClass());
            ArrayNode actualArray = (ArrayNode) actualValue;
            Assert.assertEquals("Array size mismatch.", expectedArray.size(), actualArray.size());
            String ordering = restTest.getExpectedResponseHeaders().get("X-RestTest-ArrayOrdering");

            JsonNode[] expected = new JsonNode[expectedArray.size()];
            JsonNode[] actual = new JsonNode[actualArray.size()];
            for (int idx = 0; idx < expected.length; idx++) {
                expected[idx] = expectedArray.get(idx);
                actual[idx] = actualArray.get(idx);
            }
            // If strict ordering is disabled, then sort both arrays
            if ("any".equals(ordering)) {
                Comparator<? super JsonNode> comparator = new Comparator<JsonNode>() {
                    @Override public int compare(JsonNode o1, JsonNode o2) {
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
            Iterator<Map.Entry<String, JsonNode>> fields = expectedJson.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
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

                    Assert.assertNotNull(
                            "Expected JSON text field '" + expectedFieldName + "' with value '" + expected
                                    + "' but was not found.", actualValue);
                    Assert.assertEquals(
                            "Expected JSON text field '" + expectedFieldName + "' with value '" + expected
                                    + "' but found non-text [" + actualValue.getClass().getSimpleName()
                                    + "] field with that name instead.", TextNode.class,
                            actualValue.getClass());
                    String actual = ((TextNode) actualValue).textValue();

                    if (isAssertionIgnoreCase(restTest)) {
                        if (actual != null) {
                            actual = actual.toLowerCase();
                        }
                    }

                    Assert.assertEquals("Value mismatch for text field '" + expectedFieldName + "'.",
                            expected, actual);
                } else if (expectedValue instanceof NumericNode) {
                    NumericNode numeric = (NumericNode) expectedValue;
                    Number expected = numeric.numberValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(
                            "Expected JSON numeric field '" + expectedFieldName + "' with value '" + expected
                                    + "' but was not found.", actualValue);
                    Assert.assertEquals(
                            "Expected JSON numeric field '" + expectedFieldName + "' with value '" + expected
                                    + "' but found non-numeric [" + actualValue.getClass().getSimpleName()
                                    + "] field with that name instead.", expectedValue.getClass(),
                            actualValue.getClass());
                    Number actual = ((NumericNode) actualValue).numberValue();
                    Assert.assertEquals("Value mismatch for numeric field '" + expectedFieldName + "'.",
                            expected, actual);
                } else if (expectedValue instanceof BooleanNode) {
                    BooleanNode bool = (BooleanNode) expectedValue;
                    Boolean expected = bool.booleanValue();
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(
                            "Expected JSON boolean field '" + expectedFieldName + "' with value '" + expected
                                    + "' but was not found.", actualValue);
                    Assert.assertEquals(
                            "Expected JSON boolean field '" + expectedFieldName + "' with value '" + expected
                                    + "' but found non-boolean [" + actualValue.getClass().getSimpleName()
                                    + "] field with that name instead.", expectedValue.getClass(),
                            actualValue.getClass());
                    Boolean actual = ((BooleanNode) actualValue).booleanValue();
                    Assert.assertEquals("Value mismatch for boolean field '" + expectedFieldName + "'.",
                            expected, actual);
                } else if (expectedValue instanceof ObjectNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(
                            "Expected parent JSON field '" + expectedFieldName + "' but was not found.",
                            actualValue);
                    Assert.assertEquals(
                            "Expected parent JSON field '" + expectedFieldName + "' but found field of type '"
                                    + actualValue.getClass().getSimpleName() + "'.", ObjectNode.class,
                            actualValue.getClass());
                    assertJson(restTest, expectedValue, actualValue);
                } else if (expectedValue instanceof ArrayNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(
                            "Expected JSON array field '" + expectedFieldName + "' but was not found.",
                            actualValue);
                    ArrayNode expectedArray = (ArrayNode) expectedValue;
                    Assert.assertEquals(
                            "Expected JSON array field '" + expectedFieldName + "' but found non-array ["
                                    + actualValue.getClass().getSimpleName()
                                    + "] field with that name instead.", expectedValue.getClass(),
                            actualValue.getClass());
                    ArrayNode actualArray = (ArrayNode) actualValue;
                    Assert.assertEquals("Field '" + expectedFieldName + "' array size mismatch.",
                            expectedArray.size(), actualArray.size());
                    assertJson(restTest, expectedArray, actualArray);
                } else if (expectedValue instanceof NullNode) {
                    JsonNode actualValue = actualJson.get(expectedFieldName);
                    Assert.assertNotNull(
                            "Expected Null JSON field '" + expectedFieldName + "' but was not found.",
                            actualValue);
                    Assert.assertEquals(
                            "Expected Null JSON field '" + expectedFieldName + "' but found field of type '"
                                    + actualValue.getClass().getSimpleName() + "'.", NullNode.class,
                            actualValue.getClass());
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
     *
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
     * Logs a message.
     *
     * @param message
     * @param params
     */
    private void log(String message, Object... params) {
        String outmsg = MessageFormat.format(message, params);
        LOG.info("    >> " + outmsg);
    }

    /**
     * Logs a message.
     *
     * @param message
     */
    private void logPlain(String message) {
        LOG.info("    >> " + message);
    }

    /**
     * GetEchoTestEndpoint()
     */
    public String getEchoTestEndpoint() {
        return "http://localhost:" + ECHO_PORT;
    }

}