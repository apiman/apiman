package io.apiman.plugins.transformation_policy;

import static org.junit.Assert.*;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.plugins.transformation_policy.transformer.XmlToJsonTransformer;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.BackEndService;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.IPolicyTestBackEndService;
import io.apiman.test.policies.PolicyTestBackEndServiceResponse;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

@SuppressWarnings("nls")
@TestingPolicy(TransformationPolicy.class)
public class TransformationPolicyTest extends ApimanPolicyTest {

    @Test
    @Configuration("{\"clientFormat\": \"XML\", \"serverFormat\": \"JSON\"}")
    @BackEndService(ProduceJsonBackEndService.class)
    public void transformServerJsonResponseToXml() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        
        PolicyTestResponse response = send(request);
        
        String expectedResponse = "<root><name>apiman</name></root>";
        assertEquals("application/xml", response.header("Content-Type"));
        assertNull(response.header("Content-Length"));
        assertEquals(expectedResponse, response.body());
    }

    public static class ProduceJsonBackEndService implements IPolicyTestBackEndService {

        @Override
        public PolicyTestBackEndServiceResponse invoke(ServiceRequest serviceRequest, byte[] requestBody) {
            try {
                String responseBody = "{\"name\":\"apiman\"}";
                ServiceResponse serviceResponse = new ServiceResponse();
                serviceResponse.getHeaders().put("Content-Type", "application/json");
                serviceResponse.getHeaders().put("Content-Length", String.valueOf(responseBody.getBytes("UTF-8").length));
                return new PolicyTestBackEndServiceResponse(serviceResponse, responseBody);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    @Test
    @Configuration("{\"clientFormat\": \"XML\", \"serverFormat\": \"JSON\"}")
    @BackEndService(ConsumeJsonBackEndService.class)
    public void transformClientXmlRequesttToJson() throws Throwable {
        String xml = "<a><b>test</b></a>";
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.POST, "/some/resource");
        request.header("Content-Type", "application/xml");
        request.header("Content-Length", String.valueOf(xml.getBytes("UTF-8").length));
        request.body(xml);
        
        send(request);
    }

    public static class ConsumeJsonBackEndService implements IPolicyTestBackEndService {

        @Override
        public PolicyTestBackEndServiceResponse invoke(ServiceRequest serviceRequest, byte[] requestBody) {
            if (!new String(requestBody).equals("<a><b>test</b></a>")) {
                throw new AssertionError();
            }
            ServiceResponse serviceResponse = new ServiceResponse();
            return new PolicyTestBackEndServiceResponse(serviceResponse, null);
        }
        
    }

    @Test
    @Configuration("{\"clientFormat\": \"JSON\", \"serverFormat\": \"XML\"}")
    @BackEndService(ProduceXmlBackEndService.class)
    public void transformServerXmlResponseToJson() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        
        PolicyTestResponse response = send(request);
        
        String expectedResponse = "{\"name\":\"apiman\"}";
        assertEquals("application/json", response.header("Content-Type"));
        assertNull(response.header("Content-Length"));
        assertEquals(expectedResponse, response.body());
    }

    public static class ProduceXmlBackEndService implements IPolicyTestBackEndService {

        @Override
        public PolicyTestBackEndServiceResponse invoke(ServiceRequest serviceRequest, byte[] requestBody) {
            try {
                String responseBody = "<name>apiman</name>";
                ServiceResponse serviceResponse = new ServiceResponse();
                serviceResponse.getHeaders().put("Content-Type", "application/xml");
                serviceResponse.getHeaders().put("Content-Length", String.valueOf(responseBody.getBytes("UTF-8").length));
                return new PolicyTestBackEndServiceResponse(serviceResponse, responseBody);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        
    }

    @Test
    @Configuration("{\"clientFormat\": \"JSON\", \"serverFormat\": \"XML\"}")
    @BackEndService(ConsumeXmlBackEndService.class)
    public void transformClientJsonRequestToXml() throws Throwable {
        String json = "{\"name\":\"apiman\"}";
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.POST, "/some/resource");
        request.header("Content-Type", "application/json");
        request.header("Content-Length", String.valueOf(json.getBytes("UTF-8").length));
        request.body(json);
        
        send(request);
    }

    public static class ConsumeXmlBackEndService implements IPolicyTestBackEndService {

        @Override
        public PolicyTestBackEndServiceResponse invoke(ServiceRequest serviceRequest, byte[] requestBody) {
            if (!new String(requestBody).equals("{\"name\":\"apiman\"}")) {
                throw new AssertionError();
            }
            ServiceResponse serviceResponse = new ServiceResponse();
            return new PolicyTestBackEndServiceResponse(serviceResponse, null);
        }
        
    }
    
    @Test
    @Configuration("{\"clientFormat\": \"JSON\", \"serverFormat\": \"JSON\"}")
    @BackEndService(ProduceJsonBackEndService.class)
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
    @BackEndService(ProduceXmlBackEndService.class)
    public void keepServerXmlResponseAsXml() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        
        PolicyTestResponse response = send(request);
        
        String expectedResponse = "<name>apiman</name>";
        assertEquals("application/xml", response.header("Content-Type"));
        assertEquals(String.valueOf(expectedResponse.getBytes("UTF-8").length), response.header("Content-Length"));
        assertEquals(expectedResponse, response.body());
    }

}
