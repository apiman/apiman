package io.apiman.plugins.log_policy;

import io.apiman.test.common.mock.EchoResponse;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.PolicyFailureError;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

@TestingPolicy(LogHeadersPolicy.class)
@SuppressWarnings("nls")
public class LogHeadersPolicyTest extends ApimanPolicyTest {

	/**
	 * A simple happy flow test to verify the policy does not blow up in our face.
	 */
	@Test
	@Configuration("{ \"direction\" : \"both\" }")
	public void testLogHeadersWithoutAnyRequestHeaders() throws PolicyFailureError, Throwable {
	    PrintStream out = System.out;
	    ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutput));
        try {
            PolicyTestResponse response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource"));
    		Assert.assertEquals(200, response.code());
    		String output = testOutput.toString("UTF-8");
    		output = redactDates(output);
    		output = normalize(output);
    		String expected = "Logging 0 HTTP Request headers for io.apiman.test.policies.EchoBackEndApi\n" +
    		        "Logging 4 HTTP Response headers for io.apiman.test.policies.EchoBackEndApi\n" +
    		        "Key : Content-Length, Value : 175\n" +
    		        "Key : Content-Type, Value : application/json\n" +
    		        "Key : Date, Value : XXX\n" +
    		        "Key : Server, Value : apiman.policy-test\n" +
    		        "";
    		Assert.assertEquals(expected, output);
        } finally {
            System.setOut(out);
        }
	}

    /**
	 * A simple happy flow test to verify the policy does not blow up in our face.
	 */
	@Test
    @Configuration("{ \"direction\" : \"both\" }")
	public void testLogHeadersHappyFlow() throws PolicyFailureError, Throwable {
        PrintStream out = System.out;
        ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutput));
        try {
    		PolicyTestResponse response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource")
    				.header("X-Test-Name", "testGet"));
    		Assert.assertEquals(200, response.code());
    		EchoResponse entity = response.entity(EchoResponse.class);
    		Assert.assertEquals("testGet", entity.getHeaders().get("X-Test-Name"));
            String output = testOutput.toString("UTF-8");
            output = redactDates(output);
            output = normalize(output);
            String expected = "Logging 1 HTTP Request headers for io.apiman.test.policies.EchoBackEndApi\n" +
                    "Key : X-Test-Name, Value : testGet\n" +
                    "Logging 4 HTTP Response headers for io.apiman.test.policies.EchoBackEndApi\n" +
                    "Key : Content-Length, Value : 209\n" +
                    "Key : Content-Type, Value : application/json\n" +
                    "Key : Date, Value : XXX\n" +
                    "Key : Server, Value : apiman.policy-test\n" +
                    "";
            Assert.assertEquals(expected, output);
        } finally {
            System.setOut(out);
        }
	}

    /**
     * A simple happy flow test to verify the policy does not blow up in our face.
     */
    @Test
    @Configuration("{ \"direction\" : \"request\" }")
    public void testLogHeadersHappyFlowRequestOnly() throws PolicyFailureError, Throwable {
        PrintStream out = System.out;
        ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutput));
        try {
            PolicyTestResponse response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource")
                    .header("X-Test-Name", "testGet"));
            Assert.assertEquals(200, response.code());
            EchoResponse entity = response.entity(EchoResponse.class);
            Assert.assertEquals("testGet", entity.getHeaders().get("X-Test-Name"));
            String output = testOutput.toString("UTF-8");
            output = redactDates(output);
            output = normalize(output);
            String expected = "Logging 1 HTTP Request headers for io.apiman.test.policies.EchoBackEndApi\n" +
                    "Key : X-Test-Name, Value : testGet\n" +
                    "";
            Assert.assertEquals(expected, output);
        } finally {
            System.setOut(out);
        }
    }

    /**
     * A simple happy flow test to verify the policy does not blow up in our face.
     */
    @Test
    @Configuration("{ \"direction\" : \"response\" }")
    public void testLogHeadersHappyFlowResponseOnly() throws PolicyFailureError, Throwable {
        PrintStream out = System.out;
        ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutput));
        try {
            PolicyTestResponse response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource")
                    .header("X-Test-Name", "testGet"));
            Assert.assertEquals(200, response.code());
            EchoResponse entity = response.entity(EchoResponse.class);
            Assert.assertEquals("testGet", entity.getHeaders().get("X-Test-Name"));
            String output = testOutput.toString("UTF-8");
            output = redactDates(output);
            output = normalize(output);
            String expected = "Logging 4 HTTP Response headers for io.apiman.test.policies.EchoBackEndApi\n" +
                    "Key : Content-Length, Value : 209\n" +
                    "Key : Content-Type, Value : application/json\n" +
                    "Key : Date, Value : XXX\n" +
                    "Key : Server, Value : apiman.policy-test\n" +
                    "";
            Assert.assertEquals(expected, output);
        } finally {
            System.setOut(out);
        }
    }

    /**
     * Normalize newlines across platforms.
     * @param output
     */
    private static String normalize(String output) {
        return output.replace("\r\n", "\n");
    }

    /**
     * Replace dates with XXX so we can do assertions.
     * @param output
     */
    private static String redactDates(String output) {
        return output.replaceAll("Date, Value : [\\w :]*", "Date, Value : XXX");
    }
}
