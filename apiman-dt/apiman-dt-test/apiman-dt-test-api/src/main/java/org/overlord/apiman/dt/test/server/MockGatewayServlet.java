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
package org.overlord.apiman.dt.test.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * A simple servlet that pretends to be an API Management Gateway REST endpoint.  But
 * really all it does is record information about what requests it handled so that we 
 * can do assertions in the tests.
 *
 * @author eric.wittmann@redhat.com
 */
public class MockGatewayServlet extends HttpServlet {

    private static final long serialVersionUID = 4814869997856449004L;
    
    private static StringBuilder builder = new StringBuilder();
    private static List<String> payloads = new ArrayList<String>();
    public static void reset() {
        builder = new StringBuilder();
        payloads.clear();
    }
    public static String getRequestLog() {
        return builder.toString();
    }
    public static List<String> getPayloads() {
        return payloads;
    }
    
    /**
     * Constructor.
     */
    public MockGatewayServlet() {
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        builder.append("GET:").append(req.getRequestURI()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        payloads.add(null);
        if (req.getRequestURI().endsWith("/system/status")) { //$NON-NLS-1$
            resp.setStatus(200);
            resp.setContentType("application/json"); //$NON-NLS-1$
            PrintWriter printWriter = new PrintWriter(resp.getOutputStream());
            printWriter.println("{ \"up\" : true, \"version\" : \"0\" }"); //$NON-NLS-1$
            printWriter.flush();
            printWriter.close();
        } else {
            resp.setStatus(204);
        }
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        builder.append("POST:").append(req.getRequestURI()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        payloads.add(getPayload(req));
        resp.setStatus(204);
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        builder.append("PUT:").append(req.getRequestURI()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        payloads.add(getPayload(req));
        resp.setStatus(204);
    }
    
    /**
     * Gets the payload body and returns it as a string.
     * @param req
     */
    private String getPayload(HttpServletRequest req) {
        ServletInputStream is = null;
        try {
            is = req.getInputStream();
            return IOUtils.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

}
