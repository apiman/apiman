package io.apiman.plugins.transformation_policy.backend;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.test.policies.IPolicyTestBackEndApi;
import io.apiman.test.policies.PolicyTestBackEndApiResponse;

import java.io.UnsupportedEncodingException;

@SuppressWarnings("nls")
public class ProduceComplexJsonBackEndApi implements IPolicyTestBackEndApi {

    @Override
    public PolicyTestBackEndApiResponse invoke(ApiRequest apiRequest, byte[] requestBody) {
        try {
            String responseBody = "{\r\n" + 
                    "  \"property-1\" : \"value-1\",\r\n" + 
                    "  \"property-2\" : \"value-2\",\r\n" + 
                    "  \"object-1\" : {\r\n" + 
                    "    \"p1\" : \"v1\",\r\n" + 
                    "    \"p2\" : \"v2\"\r\n" + 
                    "  },\r\n" + 
                    "  \"array-1\" : [\r\n" + 
                    "    10,\r\n" + 
                    "    5,\r\n" + 
                    "    3,\r\n" + 
                    "    12\r\n" + 
                    "  ]\r\n" + 
                    "}";
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.getHeaders().put("Content-Type", "application/json");
            apiResponse.getHeaders().put("Content-Length", String.valueOf(responseBody.getBytes("UTF-8").length));
            return new PolicyTestBackEndApiResponse(apiResponse, responseBody);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
