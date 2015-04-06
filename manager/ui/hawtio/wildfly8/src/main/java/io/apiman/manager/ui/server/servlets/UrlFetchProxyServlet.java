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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * A proxy servlet used to get around cross-origin problems when pulling
 * down content from remote sites (e.g. when pulling down a remote
 * WADL file to use in Service Import).
 *
 * @author eric.wittmann@redhat.com
 */
public class UrlFetchProxyServlet extends HttpServlet {
    
    private static final long serialVersionUID = -4704803997251798191L;
    
    private static Set<String> EXCLUDE_HEADERS = new HashSet<String>();
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
        String url = req.getHeader("X-Apiman-Url"); //$NON-NLS-1$
        if (url == null) {
            resp.sendError(500, "No URL specified in X-Apiman-Url"); //$NON-NLS-1$
            return;
        }
        
        URL remoteUrl = new URL(url);
        HttpURLConnection remoteConn = (HttpURLConnection) remoteUrl.openConnection();
        InputStream remoteIS = null;
        OutputStream responseOS = null;
        try {
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

}
