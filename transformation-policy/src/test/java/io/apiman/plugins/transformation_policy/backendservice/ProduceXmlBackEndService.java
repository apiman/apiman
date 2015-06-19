package io.apiman.plugins.transformation_policy.backendservice;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.test.policies.IPolicyTestBackEndService;
import io.apiman.test.policies.PolicyTestBackEndServiceResponse;

import java.io.UnsupportedEncodingException;

public class ProduceXmlBackEndService implements IPolicyTestBackEndService {

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