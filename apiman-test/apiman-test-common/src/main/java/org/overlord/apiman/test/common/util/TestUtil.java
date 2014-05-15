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
package org.overlord.apiman.test.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.overlord.apiman.test.common.plan.TestPlan;
import org.overlord.apiman.test.common.resttest.RestTest;

/**
 * Utilities to load test plans and rest tests.
 * 
 * @author eric.wittmann@redhat.com
 */
public class TestUtil {

    /**
     * Loads a test plan from a classpath resource.
     * @param resourcePath
     * @param cl
     */
    public static final TestPlan loadTestPlan(String resourcePath, ClassLoader cl) {
        try {
            URL url = cl.getResource(resourcePath);
            if (url == null)
                throw new RuntimeException("Test Plan not found: " + resourcePath); //$NON-NLS-1$
            JAXBContext jaxbContext = JAXBContext.newInstance(TestPlan.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            TestPlan plan = (TestPlan) jaxbUnmarshaller.unmarshal(url.openStream());
            return plan;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Loads a test plan from a file resource.
     * @param planFile
     */
    public static final TestPlan loadTestPlan(File planFile) {
        try {
            if (!planFile.isFile())
                throw new RuntimeException("Test Plan not found: " + planFile.getCanonicalPath()); //$NON-NLS-1$
            JAXBContext jaxbContext = JAXBContext.newInstance(TestPlan.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            TestPlan plan = (TestPlan) jaxbUnmarshaller.unmarshal(planFile);
            return plan;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Loads a rest test from a classpath resource.
     * @param resourcePath
     * @param cl
     */
    public static final RestTest loadRestTest(String resourcePath, ClassLoader cl) {
        InputStream is = null;
        try {
            URL url = cl.getResource(resourcePath);
            if (url == null)
                throw new RuntimeException("Rest Test not found: " + resourcePath); //$NON-NLS-1$
            is = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return parseRestTest(reader);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Parse a *.resttest file.  The format of a *.resttest file is:
     * 
     * <pre>
     *     METHOD /path/to/resource username/password
     *     Request-Header-1: value
     *     Request-Header-2: value
     *  
     *     **Request Payload**
     *     ----
     *     ResponseStatusCode
     *     Response-Header-1: expected-value
     *     Response-Header-2: expected-value
     *  
     *     **Response Payload**
     * </pre>
     * @param reader
     * @throws IOException 
     */
    private static RestTest parseRestTest(BufferedReader reader) throws IOException {
        RestTest rval = new RestTest();

        try {
            // METHOD, Path, Username/Password
            String line = reader.readLine();
            String [] split = line.split(" "); //$NON-NLS-1$
            rval.setRequestMethod(split[0]);
            rval.setRequestPath(split[1]);
            String userpass = split[2];
            split = userpass.split("/"); //$NON-NLS-1$
            rval.setUsername(split[0]);
            rval.setPassword(split[1]);
            
            // Request Headers
            line = reader.readLine();
            if (!line.trim().startsWith("----")) { //$NON-NLS-1$
                while (line.trim().length() > 0) {
                    int idx = line.indexOf(':');
                    String headerName = line.substring(0, idx).trim();
                    String headerValue = line.substring(idx + 1).trim();
                    rval.getRequestHeaders().put(headerName, headerValue);
                    line = reader.readLine();
                }
                
                // Request payload
                StringBuilder builder = new StringBuilder();
                line = reader.readLine();
                while (!line.trim().startsWith("----")) { //$NON-NLS-1$
                    builder.append(line).append("\n"); //$NON-NLS-1$
                    line = reader.readLine();
                    line = doPropertyReplacement(line);
                }
                rval.setRequestPayload(builder.toString());
            }
            
            // Response
            // Expected Status Code
            line = reader.readLine();
            rval.setExpectedStatusCode(new Integer(line.trim()));
            
            // Expected Response Headers
            line = reader.readLine();
            while (line != null && line.trim().length() > 0) {
                int idx = line.indexOf(':');
                String headerName = line.substring(0, idx).trim();
                String headerValue = line.substring(idx + 1).trim();
                rval.getExpectedResponseHeaders().put(headerName, headerValue);
                line = reader.readLine();
            }
            
            // Expected Response Payload
            if (line != null) {
                StringBuilder builder = new StringBuilder();
                line = reader.readLine();
                while (line != null && !line.trim().startsWith("----")) { //$NON-NLS-1$
                    builder.append(line).append("\n"); //$NON-NLS-1$
                    line = reader.readLine();
                }
                rval.setExpectedResponsePayload(builder.toString());
            }
        } catch (Throwable t) {
            throw new IOException("Error while parsing Rest Test", t); //$NON-NLS-1$
        }
        
        return rval;
    }

    /**
     * Provides Ant-style property replacement support.  This method looks for ${property-name}
     * formatted text and replaces the property with its value.  Values are looked up from
     * the system properties.
     * @param line the line being processed
     * @return the line with all properties replaced
     */
    public static String doPropertyReplacement(String line) {
        String rval = line;
        int sidx = -1;
        while ( (sidx = rval.indexOf("${")) != -1 ) { //$NON-NLS-1$
            int eidx = rval.indexOf('}', sidx);
            String substring = rval.substring(sidx + 2, eidx);
            String propName = substring.trim();
            String propVal = System.getProperty(propName, ""); //$NON-NLS-1$
            rval = rval.replace("${" + substring + "}", propVal); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return rval;
    }

}
