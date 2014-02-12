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
import org.overlord.apiman.dt.api.rest.contract.exceptions.ErrorBean;
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
            if (type.equals("UserNotFoundException")) { //$NON-NLS-1$
                return new UserNotFoundException(errorBean.getMessage());
            }
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
