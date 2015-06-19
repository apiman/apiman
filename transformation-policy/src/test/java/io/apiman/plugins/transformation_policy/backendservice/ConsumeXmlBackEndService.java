package io.apiman.plugins.transformation_policy.backendservice;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.test.policies.IPolicyTestBackEndService;
import io.apiman.test.policies.PolicyTestBackEndServiceResponse;

public class ConsumeXmlBackEndService implements IPolicyTestBackEndService {

    @Override
    public PolicyTestBackEndServiceResponse invoke(ServiceRequest serviceRequest, byte[] requestBody) {
        if (!new String(requestBody).equals("{\"name\":\"apiman\"}")) {
            throw new AssertionError();
        }
        ServiceResponse serviceResponse = new ServiceResponse();
        return new PolicyTestBackEndServiceResponse(serviceResponse, null);
    }
    
}