package io.apiman.plugins.log_policy;

import io.apiman.test.common.mock.EchoResponse;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.PolicyFailureError;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import org.junit.Assert;
import org.junit.Test;

@TestingPolicy(LogHeadersPolicy.class)
@SuppressWarnings("nls")
public class LogHeadersPolicyTest extends ApimanPolicyTest {

	/**
	 * A simple happy flow test to verify the policy does not blow up in our face.
	 */
	@Test
	@Configuration("{}")
	public void testLogHeadersWithoutAnyRequestHeaders() throws PolicyFailureError, Throwable {
        PolicyTestResponse response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource"));
		Assert.assertEquals(200, response.code());
	}

	/**
	 * A simple happy flow test to verify the policy does not blow up in our face.
	 */
	@Test
	@Configuration("{}")
	public void testLogHeadersHappyFlow() throws PolicyFailureError, Throwable {
		PolicyTestResponse response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource")
				.header("X-Test-Name", "testGet"));
		Assert.assertEquals(200, response.code());
		EchoResponse entity = response.entity(EchoResponse.class);
		Assert.assertEquals("testGet", entity.getHeaders().get("X-Test-Name"));
	}
}
