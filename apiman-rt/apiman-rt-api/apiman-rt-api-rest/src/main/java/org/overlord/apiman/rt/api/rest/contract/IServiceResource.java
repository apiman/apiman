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

package org.overlord.apiman.rt.api.rest.contract;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.overlord.apiman.rt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;

/**
 * The Service API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("api/services")
public interface IServiceResource {

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void publish(Service service) throws PublishingException, NotAuthorizedException;
    
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void retire(Service service) throws PublishingException, NotAuthorizedException;
}
