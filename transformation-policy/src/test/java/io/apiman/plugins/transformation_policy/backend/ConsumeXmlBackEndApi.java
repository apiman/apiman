package io.apiman.plugins.transformation_policy.backend;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.test.policies.IPolicyTestBackEndApi;
import io.apiman.test.policies.PolicyTestBackEndApiResponse;

@SuppressWarnings("nls")
public class ConsumeXmlBackEndApi implements IPolicyTestBackEndApi {

    @Override
    public PolicyTestBackEndApiResponse invoke(ApiRequest apiRequest, byte[] requestBody) {
        if (!new String(requestBody).equals("{\"name\":\"apiman\"}")) {
            throw new AssertionError();
        }
        ApiResponse apiResponse = new ApiResponse();
        return new PolicyTestBackEndApiResponse(apiResponse, null);
    }

}
