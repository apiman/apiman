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

package io.apiman.manager.ui.client.local.services.rest;

import io.apiman.manager.api.beans.exceptions.ErrorBean;
import io.apiman.manager.api.rest.contract.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.contract.exceptions.ActionException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ContractAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ContractNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidApplicationStatusException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidServiceStatusException;
import io.apiman.manager.api.rest.contract.exceptions.MemberNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.RoleAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.contract.exceptions.UserNotFoundException;
import io.apiman.manager.ui.client.local.exceptions.NotAuthenticatedException;

import javax.ws.rs.ext.Provider;

import org.jboss.errai.enterprise.client.jaxrs.AbstractJSONClientExceptionMapper;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;

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
        String header = response.getHeader("X-Apiman-Error");
        if (header != null && "true".equals(header)) {
            ErrorBean errorBean = fromJSON(response, ErrorBean.class);
            String type = errorBean.getType();
            AbstractRestException re = null;
            if (type.equals("PlanAlreadyExistsException"))
                re = new PlanAlreadyExistsException(errorBean.getMessage());
            else if (type.equals("ContractAlreadyExistsException"))
                re = new ContractAlreadyExistsException(errorBean.getMessage());
            else if (type.equals("PolicyDefinitionAlreadyExistsException"))
                re = new PolicyDefinitionAlreadyExistsException(errorBean.getMessage());
            else if (type.equals("InvalidSearchCriteriaException"))
                re = new InvalidSearchCriteriaException(errorBean.getMessage());
            else if (type.equals("NotAuthorizedException"))
                re = new NotAuthorizedException(errorBean.getMessage());
            else if (type.equals("OrganizationAlreadyExistsException"))
                re = new OrganizationAlreadyExistsException(errorBean.getMessage());
            else if (type.equals("OrganizationNotFoundException"))
                re = new OrganizationNotFoundException(errorBean.getMessage());
            else if (type.equals("RoleAlreadyExistsException"))
                re = new RoleAlreadyExistsException(errorBean.getMessage());
            else if (type.equals("RoleNotFoundException"))
                re = new RoleNotFoundException(errorBean.getMessage());
            else if (type.equals("SystemErrorException"))
                re = new SystemErrorException(errorBean.getMessage());
            else if (type.equals("UserNotFoundException"))
                re = new UserNotFoundException(errorBean.getMessage());
            else if (type.equals("ApplicationFoundException"))
                re = new ApplicationNotFoundException(errorBean.getMessage());
            else if (type.equals("ApplicationAlreadyExistsException"))
                re = new ApplicationAlreadyExistsException(errorBean.getMessage());
            else if (type.equals("ApplicationVersionNotFoundException"))
                re = new ApplicationVersionNotFoundException(errorBean.getMessage());
            else if (type.equals("ServiceNotFoundException"))
                re = new ServiceNotFoundException(errorBean.getMessage());
            else if (type.equals("ServiceAlreadyExistsException"))
                re = new ServiceAlreadyExistsException(errorBean.getMessage());
            else if (type.equals("ServiceVersionNotFoundException"))
                re = new ServiceVersionNotFoundException(errorBean.getMessage());
            else if (type.equals("MemberNotFoundException"))
                re = new MemberNotFoundException(errorBean.getMessage());
            else if (type.equals("ContractNotFoundException"))
                re = new ContractNotFoundException(errorBean.getMessage());
            else if (type.equals("PlanNotFoundException"))
                re = new PlanNotFoundException(errorBean.getMessage());
            else if (type.equals("PolicyDefinitionNotFoundException"))
                re = new PolicyDefinitionNotFoundException(errorBean.getMessage());
            else if (type.equals("PlanVersionNotFoundException"))
                re = new PlanVersionNotFoundException(errorBean.getMessage());
            else if (type.equals("ActionException"))
                re = new ActionException(errorBean.getMessage());
            else if (type.equals("InvalidApplicationStatusException"))
                re = new InvalidApplicationStatusException(errorBean.getMessage());
            else if (type.equals("InvalidServiceStatusException"))
                re = new InvalidServiceStatusException(errorBean.getMessage());
            else
                re = new SystemErrorException(errorBean.getMessage());
            re.setServerStack(errorBean.getStacktrace());
            return re;
        }
        
        if (response.getStatusCode() == 401) {
            throw new NotAuthenticatedException("[Not Authenticated] - The APIMan DT API expected authentication credentials but they were missing.");
        }
        
        return new ResponseException("Unexpected error [" + response.getStatusCode() + "]: "
                + response.getStatusText(), response);
    }

}
