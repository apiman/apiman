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
package org.overlord.rtgov.testing.proxy;

import java.net.HttpURLConnection;
import java.net.URL;

import org.overlord.apiman.gateway.DefaultGateway;
import org.overlord.apiman.gateway.DefaultServiceClientManager;
import org.overlord.apiman.gateway.undertow.UndertowGateway;
import org.overlord.apiman.inmemory.repository.InMemoryAPIManRepository;
import org.overlord.apiman.model.Account;
import org.overlord.apiman.model.App;
import org.overlord.apiman.model.Service;
import org.overlord.apiman.service.client.http.HTTPServiceClient;
import org.overlord.apiman.services.internal.DefaultAccountService;
import org.overlord.apiman.services.internal.DefaultManagerService;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * This class provides the client for sending SOAP messages
 * to the Orders switchyard application.
 *
 */
public class ProxyServer {
    
    private URL _url=null;

    /**
     * Private no-args constructor.
     */
    private ProxyServer() {
    }

    /**
     * Main method for echo client.
     * 
     * @param args The arguments
     * @throws Exception Failed
     */
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: proxyserver URL");
            System.exit(1);
        }
        
        ProxyServer server=new ProxyServer();
        
        server.run(args[0]);
    }
    
    protected void run(String url) {
        try {
            _url = new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        String handlerName=System.getProperty("handler");
        HttpHandler handler=null;
        
        if (handlerName != null && handlerName.equals("apiman")) {
            handler = new UndertowGateway();
            
            // Need to directly create the gateway for now
            DefaultGateway gw=new DefaultGateway();
            DefaultServiceClientManager scm=new DefaultServiceClientManager();
            InMemoryAPIManRepository repo=new InMemoryAPIManRepository();
            HTTPServiceClient hsc=new HTTPServiceClient();
            
            gw.setRepository(repo);
            gw.setServiceClientManager(scm);
            
            scm.getServiceClients().add(hsc);
            
            // Configure the gateway
            DefaultAccountService accountService=new DefaultAccountService();
            accountService.setRepository(repo);
            DefaultManagerService managerService=new DefaultManagerService();
            managerService.setRepository(repo);
            
            try {
                Account account=new Account();
                account.setUserId("admin");
                account.setPassword("admin");
                accountService.createAccount(account);
                
                App app=new App();
                app.setDomainId("admin");
                app.setId("1234");
                app.setName("MyApp");
                accountService.createApp("admin", app);
                
                Service service=new Service();
                service.setName("testingservice");
                service.setURI("http://localhost:8080/testingservice/");
                managerService.registerService(service);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            ((UndertowGateway)handler).setGateway(gw);
        }
        
        if (handler == null) {
            handler = new DefaultHttpHandler();
        }
        
        System.out.println("USING HANDLER="+handler);
        
        Undertow server = Undertow.builder()
                .addListener(8282, "localhost")
                .setHandler(handler).build();
        server.start();
    }
    
    protected String testCall(String value) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
        
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
        
        return (new String(b));
    }
    
    public class DefaultHttpHandler implements HttpHandler {
        @Override
        public void handleRequest(final HttpServerExchange exchange) throws Exception {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send(testCall("Hello"));
                    // value when simply echoing: "Hello World");
        }
    }
}
