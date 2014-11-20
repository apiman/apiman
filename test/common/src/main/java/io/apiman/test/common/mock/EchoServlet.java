/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.test.common.mock;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

/**
 * Simple echo servlet - for testing the gateway.
 *
 * @author eric.wittmann@redhat.com
 */
public class EchoServlet extends HttpServlet {
    
    private static final long serialVersionUID = 3185466526830586555L;
    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
    }
    
    /**
     * Constructor.
     */
    public EchoServlet() {
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doEchoResponse(req, resp, false);
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doEchoResponse(req, resp, true);
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doEchoResponse(req, resp, true);
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doEchoResponse(req, resp, false);
    }

    /**
     * Responds with a comprehensive echo.  This means bundling up all the
     * information about the inbound request into a java bean and responding
     * with that data as a JSON response.
     * @param req
     * @param resp
     * @param withBody
     */
    protected void doEchoResponse(HttpServletRequest req, HttpServletResponse resp, boolean withBody) {
        EchoResponse response = EchoResponse.from(req, withBody);
        
        resp.setContentType("application/json"); //$NON-NLS-1$
        try {
            mapper.writeValue(resp.getOutputStream(), response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}