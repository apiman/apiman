package io.apiman.plugins.transformation_policy.backend;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.test.policies.IPolicyTestBackEndApi;
import io.apiman.test.policies.PolicyTestBackEndApiResponse;

import java.io.UnsupportedEncodingException;

@SuppressWarnings("nls")
public class ProduceXmlBackEndApi implements IPolicyTestBackEndApi {

    @Override
    public PolicyTestBackEndApiResponse invoke(ApiRequest apiRequest, byte[] requestBody) {
        try {
            String responseBody = "<name>apiman</name>";
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.getHeaders().put("Content-Type", "application/xml");
            apiResponse.getHeaders().put("Content-Length", String.valueOf(responseBody.getBytes("UTF-8").length));
            return new PolicyTestBackEndApiResponse(apiResponse, responseBody);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
