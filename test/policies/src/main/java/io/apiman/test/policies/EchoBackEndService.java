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
package io.apiman.test.policies;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.test.common.mock.EchoResponse;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * The default policy test backend service.
 *
 * @author eric.wittmann@redhat.com
 */
public class EchoBackEndService implements IPolicyTestBackEndService {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static long counter = 0;

    /**
     * @see io.apiman.test.policies.IPolicyTestBackEndService#invoke(io.apiman.gateway.engine.beans.ServiceRequest, byte[])
     */
    @Override
    public PolicyTestBackEndServiceResponse invoke(ServiceRequest request, byte[] requestBody) {
        try {
            EchoResponse echoResponse = new EchoResponse();
            if (requestBody != null) {
                echoResponse.setBodyLength(new Long(requestBody.length));
                echoResponse.setBodySha1(DigestUtils.shaHex(requestBody));
            }
            echoResponse.setCounter(counter++);
            echoResponse.setHeaders(request.getHeaders());
            echoResponse.setMethod(request.getType());
            echoResponse.setResource(request.getDestination());
            echoResponse.setUri("urn:" + request.getDestination()); //$NON-NLS-1$

            ServiceResponse serviceResponse = new ServiceResponse();
            serviceResponse.setCode(200);
            serviceResponse.setMessage("OK"); //$NON-NLS-1$
            serviceResponse.getHeaders().put("Date", new Date().toString()); //$NON-NLS-1$
            serviceResponse.getHeaders().put("Server", "apiman.policy-test"); //$NON-NLS-1$ //$NON-NLS-2$
            serviceResponse.getHeaders().put("Content-Type", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$

            String responseBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(echoResponse);
            serviceResponse.getHeaders().put("Content-Length", String.valueOf(responseBody.length())); //$NON-NLS-1$

            PolicyTestBackEndServiceResponse response = new PolicyTestBackEndServiceResponse(serviceResponse, responseBody);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
