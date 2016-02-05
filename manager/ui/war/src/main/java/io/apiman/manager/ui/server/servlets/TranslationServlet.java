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
package io.apiman.manager.ui.server.servlets;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.apiman.manager.ui.server.i18n.Messages;

/**
 * A servlet that returns a JSONP response containing all of the UI strings
 * translated into the appropriate locale of the user.
 *
 * @author eric.wittmann@redhat.com
 */
public class TranslationServlet extends AbstractUIServlet {

    private static final long serialVersionUID = -7209551552522960775L;

    /**
     * Constructor.
     */
    public TranslationServlet() {
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        Messages.setLocale(request.getLocale());
        try {
            Map<String, String> strings = Messages.i18n.all();

            response.getOutputStream().write("window.APIMAN_TRANSLATION_DATA = ".getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createGenerator(response.getOutputStream(), JsonEncoding.UTF8);
            g.useDefaultPrettyPrinter();

            // Write string data here.
            g.writeStartObject();
            for (Entry<String, String> entry : strings.entrySet()) {
                g.writeStringField(entry.getKey(), entry.getValue());
            }
            g.writeEndObject();

            g.flush();
            response.getOutputStream().write(";".getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
            g.close();
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            Messages.clearLocale();
        }
    }
}
