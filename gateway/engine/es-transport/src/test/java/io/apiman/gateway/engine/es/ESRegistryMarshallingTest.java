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

import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;

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
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#marshall(io.apiman.gateway.engine.beans.Service)}.
     */
    @Test
    public void testMarshall_Service() throws Exception {
        Service service = new Service();
        service.setEndpoint("http://host/path/to/svc");
        service.setEndpointType("REST");
        service.setOrganizationId("test-org");
        service.setPublicService(true);
        service.setServiceId("service-id");
        service.setVersion("1.0");

        Assert.assertEquals("{"
                    + "\"endpoint\":\"http://host/path/to/svc\","
                    + "\"endpointType\":\"REST\","
                    + "\"publicService\":true,"
                    + "\"organizationId\":\"test-org\","
                    + "\"serviceId\":\"service-id\","
                    + "\"version\":\"1.0\","
                    + "\"endpointProperties\":[],"
                    + "\"policies\":[]"
                + "}", ESRegistryMarshalling.marshall(service).string());

        // Set to a tree map so we can guarantee ordering.
        service.setEndpointProperties(new TreeMap<String, String>());
        service.getEndpointProperties().put("property-1", "prop-1-value");
        service.getEndpointProperties().put("property-2", "prop-2-value");
        Assert.assertEquals("{"
                + "\"endpoint\":\"http://host/path/to/svc\","
                + "\"endpointType\":\"REST\","
                + "\"publicService\":true,"
                + "\"organizationId\":\"test-org\","
                + "\"serviceId\":\"service-id\","
                + "\"version\":\"1.0\","
                + "\"endpointProperties\":[{\"property-1\":\"prop-1-value\"},{\"property-2\":\"prop-2-value\"}],"
                + "\"policies\":[]"
            + "}", ESRegistryMarshalling.marshall(service).string());

        Policy policy = new Policy();
        policy.setPolicyImpl("policy-1-impl");
        policy.setPolicyJsonConfig("POLICY-1-JSON-CONFIG");
        service.getServicePolicies().add(policy);

        Policy policy2 = new Policy();
        policy2.setPolicyImpl("policy-2-impl");
        policy2.setPolicyJsonConfig("POLICY-2-JSON-CONFIG");
        service.getServicePolicies().add(policy2);

        Assert.assertEquals("{"
                + "\"endpoint\":\"http://host/path/to/svc\","
                + "\"endpointType\":\"REST\","
                + "\"publicService\":true,"
                + "\"organizationId\":\"test-org\","
                + "\"serviceId\":\"service-id\","
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
            + "}", ESRegistryMarshalling.marshall(service).string());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#marshall(io.apiman.gateway.engine.beans.Application)}.
     */
    @Test
    public void testMarshall_Application() throws Exception {
        Application app = new Application();
        app.setApplicationId("app-id");
        app.setOrganizationId("test-org");
        app.setVersion("1.0");

        Assert.assertEquals("{"
                + "\"organizationId\":\"test-org\","
                + "\"applicationId\":\"app-id\","
                + "\"version\":\"1.0\","
                + "\"contracts\":[]"
            + "}", ESRegistryMarshalling.marshall(app).string());

        Contract contract = new Contract();
        contract.setApiKey("12345");
        contract.setPlan("Silver");
        contract.setServiceId("service-id");
        contract.setServiceOrgId("service-test-org");
        contract.setServiceVersion("1.7");
        app.getContracts().add(contract);

        Assert.assertEquals("{"
                + "\"organizationId\":\"test-org\","
                + "\"applicationId\":\"app-id\","
                + "\"version\":\"1.0\","
                + "\"contracts\":["
                    + "{"
                        + "\"apiKey\":\"12345\","
                        + "\"plan\":\"Silver\","
                        + "\"serviceOrgId\":\"service-test-org\","
                        + "\"serviceId\":\"service-id\","
                        + "\"serviceVersion\":\"1.7\","
                        + "\"policies\":[]"
                    + "}"
                + "]"
            + "}", ESRegistryMarshalling.marshall(app).string());

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
                + "\"applicationId\":\"app-id\","
                + "\"version\":\"1.0\","
                + "\"contracts\":["
                    + "{"
                        + "\"apiKey\":\"12345\","
                        + "\"plan\":\"Silver\","
                        + "\"serviceOrgId\":\"service-test-org\","
                        + "\"serviceId\":\"service-id\","
                        + "\"serviceVersion\":\"1.7\","
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
            + "}", ESRegistryMarshalling.marshall(app).string());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#marshall(ServiceContract)}
     */
    @Test
    public void testMarshall_ServiceContract() throws Exception {
        ServiceContract sc = new ServiceContract();
        sc.setApikey("12345");
        sc.setPlan("Gold");
        sc.setPolicies(new ArrayList<Policy>());

        Service service = new Service();
        service.setServicePolicies(null);
        service.setEndpoint("http://host/path/to/svc");
        service.setEndpointType("REST");
        service.setOrganizationId("test-org");
        service.setPublicService(true);
        service.setServiceId("service-id");
        service.setVersion("1.0");
        sc.setService(service);

        Application app = new Application();
        app.setApplicationId("app-id");
        app.setOrganizationId("test-org");
        app.setVersion("1.0");
        sc.setApplication(app);

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
                + "\"application\":{"
                    + "\"organizationId\":\"test-org\","
                    + "\"applicationId\":\"app-id\","
                    + "\"version\":\"1.0\","
                    + "\"contracts\":[]"
                + "},"
                + "\"service\":{"
                    + "\"endpoint\":\"http://host/path/to/svc\","
                    + "\"endpointType\":\"REST\","
                    + "\"publicService\":true,"
                    + "\"organizationId\":\"test-org\","
                    + "\"serviceId\":\"service-id\","
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
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#unmarshallService(java.util.Map)}.
     */
    @Test
    public void testUnmarshall_Service() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("endpoint", "http://host:port/blah");
        data.put("endpointType", "REST");
        data.put("organizationId", "test-org");
        data.put("publicService", Boolean.TRUE);
        data.put("serviceId", "test-service");
        data.put("version", "1.2");
        Service service = ESRegistryMarshalling.unmarshallService(data);

        Assert.assertEquals("http://host:port/blah", service.getEndpoint());
        Assert.assertEquals("REST", service.getEndpointType());
        Assert.assertEquals("test-org", service.getOrganizationId());
        Assert.assertEquals("test-service", service.getServiceId());
        Assert.assertEquals("1.2", service.getVersion());
        Assert.assertEquals(Boolean.TRUE, service.isPublicService());

        List<Map<String, Object>> policiesData = new ArrayList<>();
        data.put("policies", policiesData);

        Map<String, Object> policyData = new HashMap<>();
        policyData.put("policyImpl", "impl-1");
        policyData.put("policyJsonConfig", "json-config-1");
        policiesData.add(policyData);

        service = ESRegistryMarshalling.unmarshallService(data);
        Assert.assertEquals("http://host:port/blah", service.getEndpoint());
        Assert.assertEquals("REST", service.getEndpointType());
        Assert.assertEquals("test-org", service.getOrganizationId());
        Assert.assertEquals("test-service", service.getServiceId());
        Assert.assertEquals("1.2", service.getVersion());
        Assert.assertEquals(Boolean.TRUE, service.isPublicService());
        Assert.assertEquals(1, service.getServicePolicies().size());
        Policy policy = service.getServicePolicies().iterator().next();
        Assert.assertEquals("impl-1", policy.getPolicyImpl());
        Assert.assertEquals("json-config-1", policy.getPolicyJsonConfig());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.es.ESRegistryMarshalling#unmarshallApplication(Map)}.
     */
    @Test
    public void testUnmarshall_App() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("organizationId", "test-org");
        data.put("applicationId", "test-app");
        data.put("version", "3.1");

        Application app = ESRegistryMarshalling.unmarshallApplication(data);
        Assert.assertEquals("test-org", app.getOrganizationId());
        Assert.assertEquals("test-app", app.getApplicationId());
        Assert.assertEquals("3.1", app.getVersion());

        List<Map<String, Object>> contractsData = new ArrayList<>();
        data.put("contracts", contractsData);
        Map<String, Object> contractData = new HashMap<>();
        contractData.put("apiKey", "12345");
        contractData.put("serviceOrgId", "svc-org");
        contractData.put("serviceId", "svc-id");
        contractData.put("serviceVersion", "19");
        contractsData.add(contractData);

        List<Map<String, Object>> policiesData = new ArrayList<>();
        contractData.put("policies", policiesData);

        Map<String, Object> policyData = new HashMap<>();
        policyData.put("policyImpl", "impl-1");
        policyData.put("policyJsonConfig", "json-config-1");
        policiesData.add(policyData);

        app = ESRegistryMarshalling.unmarshallApplication(data);
        Assert.assertEquals("test-org", app.getOrganizationId());
        Assert.assertEquals("test-app", app.getApplicationId());
        Assert.assertEquals("3.1", app.getVersion());
        Assert.assertEquals(1, app.getContracts().size());
        Contract contract = app.getContracts().iterator().next();
        Assert.assertEquals("12345", contract.getApiKey());
        Assert.assertEquals("svc-id", contract.getServiceId());
        Assert.assertEquals("svc-org", contract.getServiceOrgId());
        Assert.assertEquals("19", contract.getServiceVersion());

        Assert.assertEquals(1, contract.getPolicies().size());
        Policy policy = contract.getPolicies().iterator().next();
        Assert.assertEquals("impl-1", policy.getPolicyImpl());
        Assert.assertEquals("json-config-1", policy.getPolicyJsonConfig());
    }

}
