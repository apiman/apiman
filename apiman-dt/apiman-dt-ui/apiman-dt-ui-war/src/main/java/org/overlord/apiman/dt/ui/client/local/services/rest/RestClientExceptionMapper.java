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

package org.overlord.apiman.dt.ui.client.local.services.rest;

import javax.ws.rs.ext.Provider;

import org.jboss.errai.enterprise.client.jaxrs.AbstractJSONClientExceptionMapper;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ErrorBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.SystemErrorException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;
import org.overlord.apiman.dt.ui.client.local.exceptions.NotAuthenticatedException;

import com.google.gwt.http.client.Response;

/**
 * Maps Apiman DT API errors to exceptions.
 * 
 * @author eric.wittmann@redhat.com
 */
@Provider
public class RestClientExceptionMapper extends AbstractJSONClientExceptionMapper {

    /**
     * Constructor.
     */
    public RestClientExceptionMapper() {
    }

    /**
     * @see org.jboss.errai.enterprise.client.jaxrs.ClientExceptionMapper#fromResponse(com.google.gwt.http.client.Response)
     */
    @SuppressWarnings("nls") // cannot inject into a client exception mapper - so can't get the translation service easily
    @Override
    public Throwable fromResponse(Response response) {
        String header = response.getHeader("X-Apiman-Error"); //$NON-NLS-1$
        if (header != null && "true".equals(header)) { //$NON-NLS-1$
            ErrorBean errorBean = fromJSON(response, ErrorBean.class);
            String type = errorBean.getType();
            if (type.equals("InvalidSearchCriteriaException")) //$NON-NLS-1$
                return new InvalidSearchCriteriaException(errorBean.getMessage());
            if (type.equals("NotAuthorizedException")) //$NON-NLS-1$
                return new NotAuthorizedException(errorBean.getMessage());
            if (type.equals("OrganizationAlreadyExistsException")) //$NON-NLS-1$
                return new OrganizationAlreadyExistsException(errorBean.getMessage());
            if (type.equals("OrganizationNotFoundException")) //$NON-NLS-1$
                return new OrganizationNotFoundException(errorBean.getMessage());
            if (type.equals("RoleAlreadyExistsException")) //$NON-NLS-1$
                return new RoleAlreadyExistsException(errorBean.getMessage());
            if (type.equals("RoleNotFoundException")) //$NON-NLS-1$
                return new RoleNotFoundException(errorBean.getMessage());
            if (type.equals("SystemErrorException")) //$NON-NLS-1$
                return new SystemErrorException(errorBean.getMessage());
            if (type.equals("UserNotFoundException")) //$NON-NLS-1$
                return new UserNotFoundException(errorBean.getMessage());
            if (type.equals("ApplicationFoundException")) //$NON-NLS-1$
                return new ApplicationNotFoundException(errorBean.getMessage());
            if (type.equals("ApplicationAlreadyExistsException")) //$NON-NLS-1$
                return new ApplicationAlreadyExistsException(errorBean.getMessage());
            if (type.equals("ServiceFoundException")) //$NON-NLS-1$
                return new ServiceNotFoundException(errorBean.getMessage());
            if (type.equals("ServiceAlreadyExistsException")) //$NON-NLS-1$
                return new ServiceAlreadyExistsException(errorBean.getMessage());
            if (type.equals("ServiceVersionNotFoundException")) //$NON-NLS-1$
                return new ServiceVersionNotFoundException(errorBean.getMessage());
            // Default - simple exception.
            return new RuntimeException(errorBean.getMessage());
        }
        
        if (response.getStatusCode() == 401) {
            throw new NotAuthenticatedException("[Not Authenticated] - The APIMan DT API expected authentication credentials but they were missing.");
        }
        
        return new ResponseException("Unexpected error [" + response.getStatusCode() + "]: "
                + response.getStatusText(), response);
    }

}
