/*
 * Copyright 2013 JBoss Inc
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

package io.apiman.gateway.test;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

/**
 * Does a simple calculation of the overhead introduced by the War gateway
 * implementation. This should give a rough measure of how performant the engine
 * is.
 * 
 * @author eric.wittmann@redhat.com
 */
public class PerfOverheadTest extends AbstractGatewayTest {

    @Test
    public void test() throws Exception {
        // Configures the gateway
        runTestPlan("test-plans/perf/overhead-testPlan.xml"); //$NON-NLS-1$
        
        // Make a bunch of requests directly to the echo server and measure
        // the average response time.
        int avgTime_Echo = doTest(getEchoEndpoint(), 1);
        avgTime_Echo = doTest(getEchoEndpoint(), 200);
        
        // Now do the same thing but through the gateway.
        int avgTime_Gateway = doTest(getGatewayEndpoint() + "/gateway/PerfOverheadTest/echo/1.0.0/echo?apikey=12345", 200); //$NON-NLS-1$
        
        System.out.println("Average echo response time:    " + avgTime_Echo); //$NON-NLS-1$
        System.out.println("Average gateway response time: " + avgTime_Gateway); //$NON-NLS-1$
    }

    /**
     * Send a bunch of GET requests to the given endpoint and measure the response
     * time in millis.
     * @param endpoint
     * @param numIterations
     * @throws Exception 
     */
    private static int doTest(String endpoint, int numIterations) throws Exception {
        System.out.print("Testing endpoint " + endpoint + ": \n    ["); //$NON-NLS-1$ //$NON-NLS-2$
        CloseableHttpClient client = HttpClientBuilder.create().build();
        
        try {
            long totalResponseTime = 0;
            for (int i = 0; i < numIterations; i++) {
                HttpGet get = new HttpGet(endpoint);
                long start = System.currentTimeMillis();
                HttpResponse response = client.execute(get);
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new Exception("Status Code Error: " + response.getStatusLine().getStatusCode()); //$NON-NLS-1$
                }
                response.getAllHeaders();
                IOUtils.toString(response.getEntity().getContent());
                long end = System.currentTimeMillis();
                totalResponseTime += (end - start);
                System.out.print("-"); //$NON-NLS-1$
                if (i % 80 == 0) {
                    System.out.println(""); //$NON-NLS-1$
                }
            }
            System.out.println("]  Total=" + totalResponseTime); //$NON-NLS-1$
            return (int) (totalResponseTime / numIterations);
        } finally {
            client.close();
        }
    }
    
    /**
     * Run the test against a running instance of APIMan instead of the embedded
     * unit test environment.
     * @param args
     */
    public static void main(String [] args) throws Exception {
        final String rawServiceEndpoint = "http://localhost:8080/services/echo"; //$NON-NLS-1$
        final String gatewayEndpoint = "http://localhost:8080/apiman-gateway"; //$NON-NLS-1$
        
        PerfOverheadTest test = new PerfOverheadTest() {
            @Override
            protected String getEchoEndpoint() {
                return rawServiceEndpoint;
            }
            @Override
            protected String getGatewayEndpoint() {
                return gatewayEndpoint;
            }
        };
        test.test();
    }

}
