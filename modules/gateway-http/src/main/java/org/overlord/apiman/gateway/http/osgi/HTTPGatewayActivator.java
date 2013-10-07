/*
 * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.apiman.gateway.http.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import org.overlord.apiman.gateway.Gateway;
import org.overlord.apiman.gateway.http.HTTPGateway;

public class HTTPGatewayActivator implements BundleActivator {
    
    private HTTPGateway _gateway=null;

    public void start(final BundleContext context) throws Exception {
        
        ServiceReference<org.overlord.apiman.gateway.Gateway> gwsRef =
                context.getServiceReference(org.overlord.apiman.gateway.Gateway.class);
        
        ServiceReference<HttpService> sRef = context.getServiceReference(HttpService.class);

        if (sRef != null) {
            HttpService service = context.getService(sRef);
            
            _gateway = new HTTPGateway();
            
            if (gwsRef != null) {
                _gateway.setGateway(context.getService(gwsRef));
            } else {
                String filter = "(objectclass=" + Gateway.class.getName() + ")";
                        
                context.addServiceListener(new ServiceListener() {

                    @Override
                    public void serviceChanged(ServiceEvent event) {
                        if (event.getType() == ServiceEvent.REGISTERED) {
                            _gateway.setGateway((Gateway)context.getService(event.getServiceReference()));
                        }
                    }
                    
                }, filter);
            }
            
            service.registerServlet("/apiman/gateway", _gateway, null, null);
        } else {
            throw new Exception("HttpService reference was not found");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
