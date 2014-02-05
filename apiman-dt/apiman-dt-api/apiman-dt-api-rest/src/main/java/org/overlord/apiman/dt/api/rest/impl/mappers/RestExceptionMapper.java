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

package org.overlord.apiman.dt.api.rest.impl.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.overlord.apiman.dt.api.rest.contract.exceptions.AbstractRestException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ErrorBean;

/**
 * Provider that maps an error.
 *
 * @author eric.wittmann@redhat.com
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<AbstractRestException> {
    
    /**
     * Constructor.
     */
    public RestExceptionMapper() {
    }
    
    /**
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
     */
    @Override
    public Response toResponse(AbstractRestException data) {
        ErrorBean error = new ErrorBean();
        error.setType(data.getClass().getSimpleName());
        error.setErrorCode(data.getErrorCode());
        error.setMessage(data.getMessage());
        error.setMoreInfoUrl(data.getMoreInfoUrl());
        return Response.status(data.getHttpCode()).entity(error).build();
    }

}
