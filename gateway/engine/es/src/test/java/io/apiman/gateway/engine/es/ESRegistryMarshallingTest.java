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
package io.apiman.gateway.engine.es;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({"nls", "javadoc"})
public class ESRegistryMarshallingTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#marshall(io.apiman.gateway.engine.beans.Api)}.
     */
    @Test
    public void testMarshall_Api() throws Exception {
        Api api = new Api();
        api.setEndpoint("http://host/path/to/api");
        api.setEndpointType("REST");
        api.setEndpointContentType("xml");
        api.setOrganizationId("test-org");
        api.setPublicAPI(true);
        api.setApiId("api-id");
        api.setVersion("1.0");

        Assert.assertEquals("{"
                    + "\"endpoint\":\"http://host/path/to/api\","
                    + "\"endpointType\":\"REST\","
                    + "\"endpointContentType\":\"xml\","
                    + "\"publicAPI\":true,"
                    + "\"organizationId\":\"test-org\","
                    + "\"apiId\":\"api-id\","
                    + "\"version\":\"1.0\","
                    + "\"endpointProperties\":[],"
                    + "\"policies\":[]"
                + "}", ESRegistryMarshalling.marshall(api).string());

        // Set to a tree map so we can guarantee ordering.
        api.setEndpointProperties(new TreeMap<String, String>());
        api.getEndpointProperties().put("property-1", "prop-1-value");
        api.getEndpointProperties().put("property-2", "prop-2-value");
        Assert.assertEquals("{"
                + "\"endpoint\":\"http://host/path/to/api\","
                + "\"endpointType\":\"REST\","
                + "\"endpointContentType\":\"xml\","
                + "\"publicAPI\":true,"
                + "\"organizationId\":\"test-org\","
                + "\"apiId\":\"api-id\","
                + "\"version\":\"1.0\","
                + "\"endpointProperties\":[{\"property-1\":\"prop-1-value\"},{\"property-2\":\"prop-2-value\"}],"
                + "\"policies\":[]"
            + "}", ESRegistryMarshalling.marshall(api).string());

        Policy policy = new Policy();
        policy.setPolicyImpl("policy-1-impl");
        policy.setPolicyJsonConfig("POLICY-1-JSON-CONFIG");
        api.getApiPolicies().add(policy);

        Policy policy2 = new Policy();
        policy2.setPolicyImpl("policy-2-impl");
        policy2.setPolicyJsonConfig("POLICY-2-JSON-CONFIG");
        api.getApiPolicies().add(policy2);

        Assert.assertEquals("{"
                + "\"endpoint\":\"http://host/path/to/api\","
                + "\"endpointType\":\"REST\","
                + "\"endpointContentType\":\"xml\","
                + "\"publicAPI\":true,"
                + "\"organizationId\":\"test-org\","
                + "\"apiId\":\"api-id\","
                + "\"version\":\"1.0\","
                + "\"endpointProperties\":[{\"property-1\":\"prop-1-value\"},{\"property-2\":\"prop-2-value\"}],"
                + "\"policies\":["
                    + "{"
                        + "\"policyImpl\":\"policy-1-impl\","
                        + "\"policyJsonConfig\":\"POLICY-1-JSON-CONFIG\""
                    + "},"
                    + "{"
                        + "\"policyImpl\":\"policy-2-impl\","
                        + "\"policyJsonConfig\":\"POLICY-2-JSON-CONFIG\""
                    + "}"
                + "]"
            + "}", ESRegistryMarshalling.marshall(api).string());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#marshall(io.apiman.gateway.engine.beans.Client)}.
     */
    @Test
    public void testMarshall_Client() throws Exception {
        Client client = new Client();
        client.setClientId("client-id");
        client.setOrganizationId("test-org");
        client.setVersion("1.0");

        Assert.assertEquals("{"
                + "\"organizationId\":\"test-org\","
                + "\"clientId\":\"client-id\","
                + "\"version\":\"1.0\","
                + "\"contracts\":[]"
            + "}", ESRegistryMarshalling.marshall(client).string());

        Contract contract = new Contract();
        contract.setApiKey("12345");
        contract.setPlan("Silver");
        contract.setApiId("api-id");
        contract.setApiOrgId("api-test-org");
        contract.setApiVersion("1.7");
        client.getContracts().add(contract);

        Assert.assertEquals("{"
                + "\"organizationId\":\"test-org\","
                + "\"clientId\":\"client-id\","
                + "\"version\":\"1.0\","
                + "\"contracts\":["
                    + "{"
                        + "\"apiKey\":\"12345\","
                        + "\"plan\":\"Silver\","
                        + "\"apiOrgId\":\"api-test-org\","
                        + "\"apiId\":\"api-id\","
                        + "\"apiVersion\":\"1.7\","
                        + "\"policies\":[]"
                    + "}"
                + "]"
            + "}", ESRegistryMarshalling.marshall(client).string());

        Policy policy = new Policy();
        policy.setPolicyImpl("policy-1-impl");
        policy.setPolicyJsonConfig("POLICY-1-JSON-CONFIG");
        contract.getPolicies().add(policy);

        Policy policy2 = new Policy();
        policy2.setPolicyImpl("policy-2-impl");
        policy2.setPolicyJsonConfig("POLICY-2-JSON-CONFIG");
        contract.getPolicies().add(policy2);

        Assert.assertEquals("{"
                + "\"organizationId\":\"test-org\","
                + "\"clientId\":\"client-id\","
                + "\"version\":\"1.0\","
                + "\"contracts\":["
                    + "{"
                        + "\"apiKey\":\"12345\","
                        + "\"plan\":\"Silver\","
                        + "\"apiOrgId\":\"api-test-org\","
                        + "\"apiId\":\"api-id\","
                        + "\"apiVersion\":\"1.7\","
                        + "\"policies\":["
                            + "{"
                                + "\"policyImpl\":\"policy-1-impl\","
                                + "\"policyJsonConfig\":\"POLICY-1-JSON-CONFIG\""
                            + "},"
                            + "{"
                                + "\"policyImpl\":\"policy-2-impl\","
                                + "\"policyJsonConfig\":\"POLICY-2-JSON-CONFIG\""
                            + "}"
                        + "]"
                    + "}"
                + "]"
            + "}", ESRegistryMarshalling.marshall(client).string());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#marshall(ApiContract)}
     */
    @Test
    public void testMarshall_ApiContract() throws Exception {
        ApiContract sc = new ApiContract();
        sc.setApikey("12345");
        sc.setPlan("Gold");
        sc.setPolicies(new ArrayList<Policy>());

        Api api = new Api();
        api.setApiPolicies(null);
        api.setEndpoint("http://host/path/to/api");
        api.setEndpointType("REST");
        api.setEndpointContentType("json");
        api.setOrganizationId("test-org");
        api.setPublicAPI(true);
        api.setApiId("api-id");
        api.setVersion("1.0");
        sc.setApi(api);

        Client client = new Client();
        client.setClientId("client-id");
        client.setOrganizationId("test-org");
        client.setVersion("1.0");
        sc.setClient(client);

        Policy policy = new Policy();
        policy.setPolicyImpl("policy-1-impl");
        policy.setPolicyJsonConfig("POLICY-1-JSON-CONFIG");
        sc.getPolicies().add(policy);

        Policy policy2 = new Policy();
        policy2.setPolicyImpl("policy-2-impl");
        policy2.setPolicyJsonConfig("POLICY-2-JSON-CONFIG");
        sc.getPolicies().add(policy2);

        Assert.assertEquals("{"
                + "\"apiKey\":\"12345\","
                + "\"plan\":\"Gold\","
                + "\"client\":{"
                    + "\"organizationId\":\"test-org\","
                    + "\"clientId\":\"client-id\","
                    + "\"version\":\"1.0\","
                    + "\"contracts\":[]"
                + "},"
                + "\"api\":{"
                    + "\"endpoint\":\"http://host/path/to/api\","
                    + "\"endpointType\":\"REST\","
                    + "\"endpointContentType\":\"json\","
                    + "\"publicAPI\":true,"
                    + "\"organizationId\":\"test-org\","
                    + "\"apiId\":\"api-id\","
                    + "\"version\":\"1.0\","
                    + "\"endpointProperties\":[]"
                + "},"
                + "\"policies\":["
                    + "{"
                        + "\"policyImpl\":\"policy-1-impl\","
                        + "\"policyJsonConfig\":\"POLICY-1-JSON-CONFIG\""
                    + "},"
                    + "{"
                        + "\"policyImpl\":\"policy-2-impl\","
                        + "\"policyJsonConfig\":\"POLICY-2-JSON-CONFIG\""
                    + "}"
                + "]"
            + "}", ESRegistryMarshalling.marshall(sc).string());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#unmarshallApi(java.util.Map)}.
     */
    @Test
    public void testUnmarshall_Api() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("endpoint", "http://host:port/blah");
        data.put("endpointType", "REST");
        data.put("endpointContentType", "xml");
        data.put("organizationId", "test-org");
        data.put("publicAPI", Boolean.TRUE);
        data.put("apiId", "test-api");
        data.put("version", "1.2");
        Api api = ESRegistryMarshalling.unmarshallApi(data);

        Assert.assertEquals("http://host:port/blah", api.getEndpoint());
        Assert.assertEquals("REST", api.getEndpointType());
        Assert.assertEquals("xml", api.getEndpointContentType());
        Assert.assertEquals("test-org", api.getOrganizationId());
        Assert.assertEquals("test-api", api.getApiId());
        Assert.assertEquals("1.2", api.getVersion());
        Assert.assertEquals(Boolean.TRUE, api.isPublicAPI());

        List<Map<String, Object>> policiesData = new ArrayList<>();
        data.put("policies", policiesData);

        Map<String, Object> policyData = new HashMap<>();
        policyData.put("policyImpl", "impl-1");
        policyData.put("policyJsonConfig", "json-config-1");
        policiesData.add(policyData);

        api = ESRegistryMarshalling.unmarshallApi(data);
        Assert.assertEquals("http://host:port/blah", api.getEndpoint());
        Assert.assertEquals("REST", api.getEndpointType());
        Assert.assertEquals("test-org", api.getOrganizationId());
        Assert.assertEquals("test-api", api.getApiId());
        Assert.assertEquals("1.2", api.getVersion());
        Assert.assertEquals(Boolean.TRUE, api.isPublicAPI());
        Assert.assertEquals(1, api.getApiPolicies().size());
        Policy policy = api.getApiPolicies().iterator().next();
        Assert.assertEquals("impl-1", policy.getPolicyImpl());
        Assert.assertEquals("json-config-1", policy.getPolicyJsonConfig());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#unmarshallClient(Map)}.
     */
    @Test
    public void testUnmarshall_Client() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("organizationId", "test-org");
        data.put("clientId", "test-client");
        data.put("version", "3.1");

        Client client = ESRegistryMarshalling.unmarshallClient(data);
        Assert.assertEquals("test-org", client.getOrganizationId());
        Assert.assertEquals("test-client", client.getClientId());
        Assert.assertEquals("3.1", client.getVersion());

        List<Map<String, Object>> contractsData = new ArrayList<>();
        data.put("contracts", contractsData);
        Map<String, Object> contractData = new HashMap<>();
        contractData.put("apiKey", "12345");
        contractData.put("apiOrgId", "api-org");
        contractData.put("apiId", "api-id");
        contractData.put("apiVersion", "19");
        contractsData.add(contractData);

        List<Map<String, Object>> policiesData = new ArrayList<>();
        contractData.put("policies", policiesData);

        Map<String, Object> policyData = new HashMap<>();
        policyData.put("policyImpl", "impl-1");
        policyData.put("policyJsonConfig", "json-config-1");
        policiesData.add(policyData);

        client = ESRegistryMarshalling.unmarshallClient(data);
        Assert.assertEquals("test-org", client.getOrganizationId());
        Assert.assertEquals("test-client", client.getClientId());
        Assert.assertEquals("3.1", client.getVersion());
        Assert.assertEquals(1, client.getContracts().size());
        Contract contract = client.getContracts().iterator().next();
        Assert.assertEquals("12345", contract.getApiKey());
        Assert.assertEquals("api-id", contract.getApiId());
        Assert.assertEquals("api-org", contract.getApiOrgId());
        Assert.assertEquals("19", contract.getApiVersion());

        Assert.assertEquals(1, contract.getPolicies().size());
        Policy policy = contract.getPolicies().iterator().next();
        Assert.assertEquals("impl-1", policy.getPolicyImpl());
        Assert.assertEquals("json-config-1", policy.getPolicyJsonConfig());
    }

}
