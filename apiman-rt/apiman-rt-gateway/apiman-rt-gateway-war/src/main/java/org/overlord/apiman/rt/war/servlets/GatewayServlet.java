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
package org.overlord.apiman.rt.war.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.war.Gateway;

/**
 * The API Management gateway servlet.  This servlet is responsible for converting inbound
 * http servlet requests into {@link ServiceRequest}s so that they can be fed into the 
 * API Management machinery.  It also is responsible for converting the resulting 
 * {@link ServiceResponse} into an HTTP Servlet Response that is suitable for returning
 * to the caller.
 *
 * @author eric.wittmann@redhat.com
 */
public class GatewayServlet extends HttpServlet {

    private static final long serialVersionUID = 958726685958622333L;
    
    /**
     * Constructor.
     */
    public GatewayServlet() {
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "GET");
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "POST");
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "PUT");
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "DELETE");
    }

    /**
     * @param req
     * @param resp
     * @param action 
     */
    protected void doAction(HttpServletRequest req, HttpServletResponse resp, String action) {
        try {
            ServiceRequest srequest = readRequest(req);
            srequest.setType(action);
            ServiceResponse sresponse = Gateway.engine.execute(srequest);
            writeResponse(resp, sresponse);
        } catch (Exception e) {
            writeError(resp, e);
        }
    }
    
    /**
     * Reads a {@link ServiceRequest} from information found in the inbound
     * portion of the http request.
     * @param request the undertow http server request
     * @return a valid {@link ServiceRequest}
     * @throws IOException 
     */
    protected ServiceRequest readRequest(HttpServletRequest request) throws IOException {
        // TODO get the service request from a pool (re-use these objects)
        ServiceRequest srequest = new ServiceRequest();
        srequest.setOrganization(getOrganization(request));
        srequest.setService(getService(request));
        srequest.setVersion(getVersion(request));
        srequest.setApiKey(getApiKey(request));
        srequest.setDestination(getDestination(request));
        readHeaders(srequest, request);
        srequest.setBody(request.getInputStream());
        srequest.setRawRequest(request);
        return srequest;
    }

    /**
     * @param request
     * @return
     */
    protected String getOrganization(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path.split("/")[1];
    }

    /**
     * @param request
     * @return
     */
    protected String getService(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path.split("/")[2];
    }

    /**
     * @param request
     * @return
     */
    protected String getVersion(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path.split("/")[3];
    }

    /**
     * @param request
     * @return
     */
    protected String getApiKey(HttpServletRequest request) {
        return request.getHeader("X-API-Key");
    }

    /**
     * @param request
     * @return
     */
    protected String getDestination(HttpServletRequest request) {
        // Format:  /org/svc/version/dest/in/a/tion
        String path = request.getPathInfo();
        int idx = -1;
        for (int i=0; i<4; i++) {
            idx = path.indexOf('/', idx+1);
        }
        return path.substring(idx);
    }

    /**
     * Reads the inbound request headers from the request and sets them on
     * the {@link ServiceRequest}.
     * @param request
     * @param request
     */
    protected void readHeaders(ServiceRequest srequest, HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hname = headerNames.nextElement();
            String hval = request.getHeader(hname);
            srequest.getHeaders().put(hname, hval);
        }
    }

    /**
     * Writes the service response to the HTTP servlet response object.
     * @param response
     * @param sresponse
     */
    protected void writeResponse(HttpServletResponse response, ServiceResponse sresponse) {
        response.setStatus(sresponse.getCode());
        Map<String, String> headers = sresponse.getHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String hname = entry.getKey();
            String hval = entry.getValue();
            response.setHeader(hname, hval);
        }
        if (sresponse.getBody() != null) {
            InputStream body = null;
            OutputStream out = null;
            try {
                body = sresponse.getBody();
                out = response.getOutputStream();
                IOUtils.copy(body, out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(body);
                IOUtils.closeQuietly(out);
            }
        }
    }

    /**
     * @param resp
     * @param e
     */
    protected void writeError(HttpServletResponse resp, Exception e) {
        try {
            resp.setHeader("X-Exception", e.getMessage());
            resp.sendError(500, e.getMessage());
        } catch (IOException e1) {
            throw new RuntimeException(e);
        }
    }

}
