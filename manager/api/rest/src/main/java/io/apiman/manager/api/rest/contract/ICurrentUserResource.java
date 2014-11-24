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

package io.apiman.manager.api.rest.contract;

import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The Current User API.  Returns information about the authenticated
 * user.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("currentuser")
public interface ICurrentUserResource {

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public UserBean getInfo();

    @PUT
    @Path("info")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateInfo(UserBean info);
    
    @GET
    @Path("organizations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrganizationSummaryBean> getOrganizations();

    @GET
    @Path("applications")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApplicationSummaryBean> getApplications();

    @GET
    @Path("services")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceSummaryBean> getServices();

}
