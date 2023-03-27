/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.plugins.transformation_policy.backend;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.test.policies.IPolicyTestBackEndApi;
import io.apiman.test.policies.PolicyTestBackEndApiResponse;

import java.io.UnsupportedEncodingException;

/**
 * @author eric.wittmann@gmail.com
 */
public class ProduceEchoXmlResponseBackend implements IPolicyTestBackEndApi {
    
    /**
     * Constructor.
     */
    public ProduceEchoXmlResponseBackend() {
    }

    /**
     * @see io.apiman.test.policies.IPolicyTestBackEndApi#invoke(io.apiman.gateway.engine.beans.ApiRequest, byte[])
     */
    @Override
    @SuppressWarnings("nls")
    public PolicyTestBackEndApiResponse invoke(ApiRequest request, byte[] requestBody) {
        try {
            String responseBody = "<echoResponse>\r\n" + 
                    "  <counter>24</counter>\r\n" + 
                    "  <headers>\r\n" + 
                    "    <entry>\r\n" + 
                    "      <key>Accept</key>\r\n" + 
                    "      <value>text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8</value>\r\n" + 
                    "    </entry>\r\n" + 
                    "    <entry>\r\n" + 
                    "      <key>Connection</key>\r\n" + 
                    "      <value>keep-alive</value>\r\n" + 
                    "    </entry>\r\n" + 
                    "    <entry>\r\n" + 
                    "      <key>User-Agent</key>\r\n" + 
                    "      <value>Mozilla/5.0 (Windows NT 10.0; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0</value>\r\n" + 
                    "    </entry>\r\n" + 
                    "    <entry>\r\n" + 
                    "      <key>Host</key>\r\n" + 
                    "      <value>localhost:8080</value>\r\n" + 
                    "    </entry>\r\n" + 
                    "    <entry>\r\n" + 
                    "      <key>Accept-Language</key>\r\n" + 
                    "      <value>null</value>\r\n" + 
                    "    </entry>\r\n" + 
                    "    <entry>\r\n" + 
                    "      <key>Accept-Encoding</key>\r\n" + 
                    "      <value>gzip, deflate</value>\r\n" + 
                    "    </entry>\r\n" + 
                    "  </headers>\r\n" + 
                    "  <method>GET</method>\r\n" + 
                    "  <resource>/services/echo</resource>\r\n" + 
                    "  <uri>/services/echo</uri>\r\n" + 
                    "</echoResponse>";
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.getHeaders().put("Content-Type", "application/xml");
            apiResponse.getHeaders().put("Content-Length", String.valueOf(responseBody.getBytes("UTF-8").length));
            return new PolicyTestBackEndApiResponse(apiResponse, responseBody);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
