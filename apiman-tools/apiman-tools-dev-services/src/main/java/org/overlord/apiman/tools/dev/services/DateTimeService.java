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
package org.overlord.apiman.tools.dev.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple date-time service.  This provides a way to get the current system
 * date and/or system time.
 *
 * @author eric.wittmann@redhat.com
 */
public class DateTimeService extends HttpServlet {

    private static final long serialVersionUID = 1395043067724682265L;
    
    /**
     * Constructor.
     */
    public DateTimeService() {
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        resp.setContentType("application/json"); //$NON-NLS-1$
        ServletOutputStream outputStream = resp.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        Date date = new Date();
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
        
        writer.println("{"); //$NON-NLS-1$
        writer.println("  \"date\" : \"" + dateFormat.format(date) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        writer.println("}"); //$NON-NLS-1$
        writer.flush();
        writer.close();
    }

}
