package io.apiman.plugins.transformation_policy;

import static org.junit.Assert.*;

import io.apiman.plugins.transformation_policy.backend.ConsumeJsonBackEndApi;
import io.apiman.plugins.transformation_policy.backend.ConsumeXmlBackEndApi;
import io.apiman.plugins.transformation_policy.backend.ProduceJsonBackEndApi;
import io.apiman.plugins.transformation_policy.backend.ProduceXmlBackEndApi;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.BackEndApi;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import org.junit.Test;

@SuppressWarnings("nls")
@TestingPolicy(TransformationPolicy.class)
public class TransformationPolicyTest extends ApimanPolicyTest {

    @Test
    @Configuration("{\"clientFormat\": \"XML\", \"serverFormat\": \"JSON\"}")
    @BackEndApi(ProduceJsonBackEndApi.class)
    public void transformServerJsonResponseToXml() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        
        PolicyTestResponse response = send(request);
        
        String expectedResponse = "<name>apiman</name>";
        assertEquals("application/xml", response.header("Content-Type"));
        assertNull(response.header("Content-Length"));
        assertEquals(expectedResponse, response.body());
    }

    @Test
    @Configuration("{\"clientFormat\": \"XML\", \"serverFormat\": \"JSON\"}")
    @BackEndApi(ConsumeJsonBackEndApi.class)
    public void transformClientXmlRequesttToJson() throws Throwable {
        String xml = "<a><b>test</b></a>";
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.POST, "/some/resource");
        request.header("Content-Type", "application/xml");
        request.header("Content-Length", String.valueOf(xml.getBytes("UTF-8").length));
        request.body(xml);
        
        send(request);
    }

    @Test
    @Configuration("{\"clientFormat\": \"JSON\", \"serverFormat\": \"XML\"}")
    @BackEndApi(ProduceXmlBackEndApi.class)
    public void transformServerXmlResponseToJson() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        
        PolicyTestResponse response = send(request);
        
        String expectedResponse = "{\"name\":\"apiman\"}";
        assertEquals("application/json", response.header("Content-Type"));
        assertNull(response.header("Content-Length"));
        assertEquals(expectedResponse, response.body());
    }

    @Test
    @Configuration("{\"clientFormat\": \"JSON\", \"serverFormat\": \"XML\"}")
    @BackEndApi(ConsumeXmlBackEndApi.class)
    public void transformClientJsonRequestToXml() throws Throwable {
        String json = "{\"name\":\"apiman\"}";
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.POST, "/some/resource");
        request.header("Content-Type", "application/json");
        request.header("Content-Length", String.valueOf(json.getBytes("UTF-8").length));
        request.body(json);
        
        send(request);
    }

    @Test
    @Configuration("{\"clientFormat\": \"JSON\", \"serverFormat\": \"JSON\"}")
    @BackEndApi(ProduceJsonBackEndApi.class)
    public void keepServerJsonResponseAsJson() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        
        PolicyTestResponse response = send(request);
        
        String expectedResponse = "{\"name\":\"apiman\"}";
        assertEquals("application/json", response.header("Content-Type"));
        assertEquals(String.valueOf(expectedResponse.getBytes("UTF-8").length), response.header("Content-Length"));
        assertEquals(expectedResponse, response.body());
    }

    @Test
    @Configuration("{\"clientFormat\": \"XML\", \"serverFormat\": \"XML\"}")
    @BackEndApi(ProduceXmlBackEndApi.class)
    public void keepServerXmlResponseAsXml() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        
        PolicyTestResponse response = send(request);
        
        String expectedResponse = "<name>apiman</name>";
        assertEquals("application/xml", response.header("Content-Type"));
        assertEquals(String.valueOf(expectedResponse.getBytes("UTF-8").length), response.header("Content-Length"));
        assertEquals(expectedResponse, response.body());
    }

}
