/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.test.common.mock.EchoResponse;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.BackEndApi;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.IPolicyTestBackEndApi;
import io.apiman.test.policies.PolicyFailureError;
import io.apiman.test.policies.PolicyTestBackEndApiResponse;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@TestingPolicy(TransferQuotaPolicy.class)
@SuppressWarnings("nls")
public class TransferQuotaPolicyTest extends ApimanPolicyTest {

    @Test
    @Configuration("{" +
            "  \"limit\" : 100," +
            "  \"direction\" : \"upload\"," +
            "  \"granularity\" : \"Api\"," +
            "  \"period\" : \"Day\"," +
            "  \"headerRemaining\" : \"X-Bytes-Remaining\"," +
            "  \"headerLimit\" : \"X-Bytes-Limit\"," +
            "  \"headerReset\" : \"X-Bytes-Reset\"" +
            "}")
    public void testUploadLimit() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.POST, "/some/resource");
        request.body("0123456789");

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Assert.assertEquals("90", response.header("X-Bytes-Remaining"));
        Assert.assertEquals("100", response.header("X-Bytes-Limit"));

        // Now try sending a few more times to get closer to the limit
        for (int i = 0; i < 9; i++) {
            response = send(request);
        }
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Assert.assertEquals("0", response.header("X-Bytes-Remaining"));
        Assert.assertEquals("100", response.header("X-Bytes-Limit"));

        // Now if we try it one more time, we'll get a failure!
        try {
            send(request);
            Assert.fail("Expected a policy failure!");
        } catch (PolicyFailureError e) {
            PolicyFailure failure = e.getFailure();
            Assert.assertEquals(PolicyFailureCodes.BYTE_QUOTA_EXCEEDED, failure.getFailureCode());
            Assert.assertEquals("Transfer quota exceeded.", failure.getMessage());
            Assert.assertEquals(429, failure.getResponseCode());
        }
    }

    @Test
    @Configuration("{" +
            "  \"limit\" : 10485760," +
            "  \"direction\" : \"upload\"," +
            "  \"granularity\" : \"Api\"," +
            "  \"period\" : \"Day\"," +
            "  \"headerRemaining\" : \"X-Data-Remaining\"," +
            "  \"headerLimit\" : \"X-Data-Limit\"," +
            "  \"headerReset\" : \"X-Data-Reset\"" +
            "}")
    public void testLargeUploadLimit() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.POST, "/some/large-resource");
        // The 4th of these should exceed our limits
        byte [] data = new byte[11000000 / 4];
        Arrays.fill(data, (byte) 80);
        request.body(new String(data));

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Assert.assertEquals("7735760", response.header("X-Data-Remaining"));
        Assert.assertEquals("10485760", response.header("X-Data-Limit"));

        send(request);
        send(request);
        send(request);

        // Now if we try it one more time, we'll get a failure!
        try {
            send(request);
            Assert.fail("Expected a policy failure!");
        } catch (PolicyFailureError e) {
            PolicyFailure failure = e.getFailure();
            Assert.assertEquals(PolicyFailureCodes.BYTE_QUOTA_EXCEEDED, failure.getFailureCode());
            Assert.assertEquals("Transfer quota exceeded.", failure.getMessage());
            Assert.assertEquals(429, failure.getResponseCode());
            String remaining = failure.getHeaders().get("X-Data-Remaining");
            Assert.assertEquals("-514240", remaining);
        }
    }

    @Test
    @Configuration("{" +
            "  \"limit\" : 1000," +
            "  \"direction\" : \"download\"," +
            "  \"granularity\" : \"Api\"," +
            "  \"period\" : \"Day\"," +
            "  \"headerRemaining\" : \"X-DBytes-Remaining\"," +
            "  \"headerLimit\" : \"X-DBytes-Limit\"," +
            "  \"headerReset\" : \"X-DBytes-Reset\"" +
            "}")
    @BackEndApi(DownloadTestBackEndApi.class)
    public void testDownloadLimit() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        request.header("X-Payload-Size", "389");

        PolicyTestResponse response = send(request);
        Assert.assertNotNull(response.body());
        Assert.assertEquals("1000", response.header("X-DBytes-Remaining"));
        Assert.assertEquals("1000", response.header("X-DBytes-Limit"));

        send(request);
        send(request);

        // Now if we try it one more time, we'll get a failure!
        try {
            send(request);
            Assert.fail("Expected a policy failure!");
        } catch (PolicyFailureError e) {
            PolicyFailure failure = e.getFailure();
            Assert.assertEquals(PolicyFailureCodes.BYTE_QUOTA_EXCEEDED, failure.getFailureCode());
            Assert.assertEquals("Transfer quota exceeded.", failure.getMessage());
            Assert.assertEquals(429, failure.getResponseCode());
        }
    }


    @Test
    @Configuration("{" +
            "  \"limit\" : 500," +
            "  \"direction\" : \"both\"," +
            "  \"granularity\" : \"Api\"," +
            "  \"period\" : \"Day\"," +
            "  \"headerRemaining\" : \"X-Bytes-Remaining\"," +
            "  \"headerLimit\" : \"X-Bytes-Limit\"," +
            "  \"headerReset\" : \"X-Bytes-Reset\"" +
            "}")
    @BackEndApi(DownloadTestBackEndApi.class)
    public void testBothLimit() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.PUT, "/some/resource");
        request.header("X-Payload-Size", "50");
        byte [] data = new byte[50];
        Arrays.fill(data, (byte) 80);
        request.body(new String(data));

        PolicyTestResponse response = send(request);
        Assert.assertNotNull(response.body());
        Assert.assertEquals("450", response.header("X-Bytes-Remaining"));
        Assert.assertEquals("500", response.header("X-Bytes-Limit"));

        send(request);
        send(request);
        send(request);
        send(request);

        // Now if we try it one more time, we'll get a failure!
        try {
            send(request);
            Assert.fail("Expected a policy failure!");
        } catch (PolicyFailureError e) {
            PolicyFailure failure = e.getFailure();
            Assert.assertEquals(PolicyFailureCodes.BYTE_QUOTA_EXCEEDED, failure.getFailureCode());
            Assert.assertEquals("Transfer quota exceeded.", failure.getMessage());
            Assert.assertEquals(429, failure.getResponseCode());
        }
    }

    public static final class DownloadTestBackEndApi implements IPolicyTestBackEndApi {

        /**
         * @see io.apiman.test.policies.IPolicyTestBackEndApi#invoke(io.apiman.gateway.engine.beans.ApiRequest, byte[])
         */
        @Override
        public PolicyTestBackEndApiResponse invoke(ApiRequest request, byte[] requestBody) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(200);
            apiResponse.setMessage("OK"); //$NON-NLS-1$
            apiResponse.getHeaders().put("Date", new Date().toString()); //$NON-NLS-1$
            apiResponse.getHeaders().put("Server", "apiman.policy-test"); //$NON-NLS-1$ //$NON-NLS-2$
            apiResponse.getHeaders().put("Content-Type", "text/plain"); //$NON-NLS-1$ //$NON-NLS-2$

            int payloadSize = 20;
            String payloadSizeHeader = request.getHeaders().get("X-Payload-Size");
            if (payloadSizeHeader != null) {
                payloadSize = new Integer(payloadSizeHeader);
            }

            byte [] payloadData = new byte[payloadSize];
            Arrays.fill(payloadData, (byte) 80);
            String payload = new String(payloadData);
            PolicyTestBackEndApiResponse response = new PolicyTestBackEndApiResponse(apiResponse, payload);
            return response;
        }

    }
}
