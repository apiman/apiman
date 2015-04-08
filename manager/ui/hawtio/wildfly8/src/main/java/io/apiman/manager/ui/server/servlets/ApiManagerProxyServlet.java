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
package io.apiman.manager.ui.server.servlets;

import io.apiman.manager.ui.server.auth.ITokenGenerator;
import io.apiman.manager.ui.server.beans.ApiAuthType;
import io.apiman.manager.ui.server.beans.BearerTokenCredentialsBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * A proxy servlet used to proxy calls from the UI to the 
 * API Manager backend.  This should be used very sparingly.
 * As of this implementation it is only used in order to get
 * a download link for the application's API Registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiManagerProxyServlet extends AbstractUIServlet {
    
    private static final long serialVersionUID = -4532742030638806510L;

    private static Set<String> EXCLUDE_HEADERS = new HashSet<>();
    static {
        EXCLUDE_HEADERS.add("ETag"); //$NON-NLS-1$
        EXCLUDE_HEADERS.add("Last-Modified"); //$NON-NLS-1$
        EXCLUDE_HEADERS.add("Date"); //$NON-NLS-1$
        EXCLUDE_HEADERS.add("Cache-control"); //$NON-NLS-1$
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        StringBuilder url = new StringBuilder();

        String endpoint = getConfig().getManagementApiEndpoint();
        if (endpoint == null) {
            endpoint = getDefaultEndpoint(req);
        }

        url.append(endpoint);
        if (!url.toString().endsWith("/")) { //$NON-NLS-1$
            url.append('/');
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/")) { //$NON-NLS-1$
            url.append(pathInfo.substring(1));
        } else {
            url.append(pathInfo);
        }

        String authHeaderValue = null;
        ApiAuthType authType = getConfig().getManagementApiAuthType();
        switch (authType) {
            case basic: {
                String username = getConfig().getManagementApiAuthUsername();
                String password = getConfig().getManagementApiAuthPassword();
                String encoded = base64Encode(username + ":" + password); //$NON-NLS-1$
                authHeaderValue = "Basic " + encoded; //$NON-NLS-1$
                break;
            }
            case authToken: {
                ITokenGenerator tokenGenerator = getTokenGenerator();
                BearerTokenCredentialsBean creds = tokenGenerator.generateToken(req);
                String token = creds.getToken();
                authHeaderValue = "AUTH-TOKEN " + token; //$NON-NLS-1$
                break;
            }
            case bearerToken: {
                ITokenGenerator tokenGenerator = getTokenGenerator();
                BearerTokenCredentialsBean creds = tokenGenerator.generateToken(req);
                String token = creds.getToken();
                authHeaderValue = "Bearer " + token; //$NON-NLS-1$
                break;
            }
            case samlBearerToken: {
                ITokenGenerator tokenGenerator = getTokenGenerator();
                BearerTokenCredentialsBean creds = tokenGenerator.generateToken(req);
                String token = creds.getToken();
                // TODO base64 decode the token, then re-encode it with "SAML-BEARER-TOKEN:"
                authHeaderValue = "Basic SAML-BEARER-TOKEN:" + token; //$NON-NLS-1$
                break;
            }
        }
        
        URL remoteUrl = new URL(url.toString());
        HttpURLConnection remoteConn = (HttpURLConnection) remoteUrl.openConnection();
        InputStream remoteIS = null;
        OutputStream responseOS = null;
        try {
            if (authHeaderValue != null) {
                remoteConn.setRequestProperty("Authorization", authHeaderValue); //$NON-NLS-1$
            }
            remoteConn.connect();
            Map<String, List<String>> headerFields = remoteConn.getHeaderFields();
            for (String headerName : headerFields.keySet()) {
                if (headerName == null) {
                    continue;
                }
                if (EXCLUDE_HEADERS.contains(headerName)) {
                    continue;
                }
                String headerValue = remoteConn.getHeaderField(headerName);
                resp.setHeader(headerName, headerValue);
                if (url.toString().contains("apiregistry")) { //$NON-NLS-1$
                    String type = "json"; //$NON-NLS-1$
                    if (url.toString().endsWith("xml")) { //$NON-NLS-1$
                        type = "xml"; //$NON-NLS-1$
                    }
                    resp.setHeader("Content-Disposition", "attachment; filename=api-registry." + type); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            resp.setHeader("Cache-control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$
            remoteIS = remoteConn.getInputStream();
            responseOS = resp.getOutputStream();
            IOUtils.copy(remoteIS, responseOS);
            resp.flushBuffer();
        } catch (Exception e) {
            resp.sendError(500, e.getMessage());
        } finally {
            IOUtils.closeQuietly(responseOS);
            IOUtils.closeQuietly(remoteIS);
        }
    }

    /**
     * base64 encodes a string.
     * @param value
     * @throws UnsupportedEncodingException 
     */
    private String base64Encode(String value) throws UnsupportedEncodingException {
        return Base64.encodeBase64String(value.getBytes("UTF-8")); //$NON-NLS-1$
    }

}
