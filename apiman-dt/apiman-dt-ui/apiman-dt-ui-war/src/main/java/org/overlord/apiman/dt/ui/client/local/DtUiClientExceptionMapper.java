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

package org.overlord.apiman.dt.ui.client.local;

import javax.ws.rs.ext.Provider;

import org.jboss.errai.enterprise.client.jaxrs.AbstractJSONClientExceptionMapper;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ErrorBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;

import com.google.gwt.http.client.Response;

/**
 * Maps Apiman DT API errors to exceptions.
 * 
 * @author eric.wittmann@redhat.com
 */
@Provider
public class DtUiClientExceptionMapper extends AbstractJSONClientExceptionMapper {

    /**
     * Constructor.
     */
    public DtUiClientExceptionMapper() {
    }

    /**
     * @see org.jboss.errai.enterprise.client.jaxrs.ClientExceptionMapper#fromResponse(com.google.gwt.http.client.Response)
     */
    @Override
    public Throwable fromResponse(Response response) {
        ErrorBean errorBean = fromJSON(response, ErrorBean.class);
        String type = errorBean.getType();
        if (type.equals("UserNotFoundException")) {
            return new UserNotFoundException(errorBean.getMessage());
        }
        // Default - simple exception.
        return new RuntimeException(errorBean.getMessage());
    }

}
