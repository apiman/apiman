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

import io.apiman.common.util.SimpleStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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

    private long servletCounter = 0L;

    /**
     * Create an echo response from the inbound information in the http server
     * request.
     * @param request the request
     * @param withBody if request is with body
     * @return a new echo response
     */
    public static EchoResponse response(HttpServletRequest request, boolean withBody) {
        EchoResponse response = new EchoResponse();
        response.setMethod(request.getMethod());
        if (request.getQueryString() != null) {
            String[] normalisedQueryString = request.getQueryString().split("&");
            Arrays.sort(normalisedQueryString);
            response.setResource(request.getRequestURI() + "?" + SimpleStringUtils.join("&", normalisedQueryString)); //$NON-NLS-1$
        } else {
            response.setResource(request.getRequestURI());
        }
        response.setUri(request.getRequestURI());
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            response.getHeaders().put(name, value);
        }
        if (withBody) {
            long totalBytes = 0;
            InputStream is = null;
            try {
                is = request.getInputStream();
                MessageDigest sha1 = MessageDigest.getInstance("SHA1"); //$NON-NLS-1$
                byte[] data = new byte[1024];
                int read = 0;
                while ((read = is.read(data)) != -1) {
                    sha1.update(data, 0, read);
                    totalBytes += read;
                };

                byte[] hashBytes = sha1.digest();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < hashBytes.length; i++) {
                  sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                String fileHash = sb.toString();

                response.setBodyLength(totalBytes);
                response.setBodySha1(fileHash);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return response;
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
        EchoResponse response = response(req, withBody);
        response.setCounter(++servletCounter);
        resp.setContentType("application/json"); //$NON-NLS-1$
        resp.setHeader("Response-Counter", response.getCounter().toString()); //$NON-NLS-1$
        try {
            mapper.writeValue(resp.getOutputStream(), response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}