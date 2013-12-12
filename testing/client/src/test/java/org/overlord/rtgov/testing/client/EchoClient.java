/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-13, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.overlord.rtgov.testing.client;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class provides the client for sending SOAP messages
 * to the Orders switchyard application.
 *
 */
public class EchoClient {

    /**
     * Private no-args constructor.
     */
    private EchoClient() {
    }

    /**
     * Main method for echo client.
     * 
     * @param args The arguments
     * @throws Exception Failed
     */
    public static void main(final String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: echoclient URL value count");
            System.exit(1);
        }
        
        int count=0;
        
        try {
            count = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.err.println("Unable to parse count '"+args[2]+"': "+e.getMessage());
            System.exit(1);
        }
        
        EchoClient client=new EchoClient();
        
        client.test(args[0], args[1], count);
    }
    
    protected void test(String url, String value, int count) {
        char separator=(url.indexOf('?')==-1?'?':'&');
        
        String urlString=url+separator+"value="+value;
        
        System.out.println("CALLING URL="+urlString);
        
        try {
            URL queryUrl=new URL(urlString);
            
            // Warm up call
            testCall(queryUrl, value, false);
            
            long startTime=System.currentTimeMillis();
            
            for (int i=0; i < count; i++) {
                testCall(queryUrl, value+i, false);
            }
            
            long endTime=System.currentTimeMillis();
            
            System.out.println("RESULTS: "+count+" calls took "+(endTime-startTime)+"ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
    
    protected void testCall(URL queryUrl, String value, boolean validate) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) queryUrl.openConnection();
        
        connection.setRequestMethod("GET");

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);
        connection.setRequestProperty("Content-Type",
                    "application/json");

        java.io.InputStream is=connection.getInputStream();
        
        byte[] b=new byte[is.available()];
        
        is.read(b);
        
        is.close();
        
        if (validate) {
            String resp=new String(b);
            if (!resp.equals(value)) {
                System.err.println("Response '"+resp+"' was not same as value '"+value+"'");
            }
        }
    }
}
