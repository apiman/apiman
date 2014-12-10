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
package io.apiman.test.suite;

import io.apiman.test.common.util.TestPlanRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * A full apiman test suite.  Run this against an empty apiman installation as a smoke
 * test.  The suite will do the following:
 * 
 * <pre>
 * 1) Global configuration
 *     a) Add policy definitions
 *     b) Add role definitions
 *     c) Create a Gateway
 * 2) Create a Test organization
 * 3) Add two Plans (Gold, Silver)
 * 4) Add an "Echo" service
 * 5) Create an application
 * 6) Create a contract from the app to the service via the Gold plan
 * 7) Create a contract from the app to the service via the Silver plan
 * 8) Publish and Register the service and app
 * 9) Send requests to the gateway
 * </pre>
 * 
 * The following are pre-requisites for this test suite:
 * 
 * <pre>
 * 1) apiman must be running :)
 * 2) a valid user must exist with admin privs (role = apiadmin)
 * 3) no data exists yet in apiman (empty database)
 * 4) the echo REST service must be running somewhere (can be found in apiman-quickstarts)
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class FullTestSuite {
    
    /**
     * The test suite main entry point.
     * @param args
     * @throws Exception
     */
    public static void main(String [] args) throws Exception {
        String apiEndpoint = param("apiman.suite.api-endpoint", "API Endpoint", "http://localhost:8080/apiman");
        param("apiman.suite.api-username", "  Username", "admin");
        param("apiman.suite.api-password", "  Password", "admin123!");
        param("apiman.suite.gateway-config-endpoint", "Gateway Config Endpoint", "http://localhost:8080/apiman-gateway");
        param("apiman.suite.gateway-config-username", "  Username", "admin");
        param("apiman.suite.gateway-config-password", "  Password", "admin123!");
        String gatewayEndpoint = param("apiman.suite.gateway-endpoint", "Gateway HTTP Endpoint", "http://localhost:8080/apiman-gateway/gateway");
        param("apiman.suite.echo-endpoint", "Echo Service Endpoint", "http://localhost:8080/services/echo");
        
        TestPlanRunner runner = new TestPlanRunner(apiEndpoint);
        runner.runTestPlan("scripts/api-manager-init-testPlan.xml", FullTestSuite.class.getClassLoader());
        runner.runTestPlan("scripts/api-manager-testPlan.xml", FullTestSuite.class.getClassLoader());
        runner = new TestPlanRunner(gatewayEndpoint);
        runner.runTestPlan("scripts/api-gateway-testPlan.xml", FullTestSuite.class.getClassLoader());
    }

    /**
     * Gets a param value, updates the system properties if necessary.
     * @param propertyName
     * @param prompt
     * @param defaultValue
     * @throws Exception
     */
    private static String param(String propertyName, String prompt, String defaultValue) throws Exception {
        if (System.getProperty(propertyName) != null) {
            return System.getProperty(propertyName);
        }
        System.out.print(prompt + " [" + defaultValue + "]: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = br.readLine();
        if (input == null || input.trim().isEmpty()) {
            input = defaultValue;
        }
        
        System.setProperty(propertyName, input);
        return input;
    }

}
