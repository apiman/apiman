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

package io.apiman.manager.api.rest.impl.mappers;

import io.apiman.manager.api.beans.exceptions.ErrorBean;
import io.apiman.manager.api.rest.contract.exceptions.AbstractRestException;
import io.apiman.manager.api.security.ISecurityContext;

import java.io.PrintWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.output.StringBuilderWriter;

/**
 * Provider that maps an error.
 *
 * @author eric.wittmann@redhat.com
 */
@Provider
@ApplicationScoped
public class RestExceptionMapper implements ExceptionMapper<AbstractRestException> {

    @Inject
    ISecurityContext securityContext;

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
        String origin = securityContext.getRequestHeader("Origin"); //$NON-NLS-1$
        ErrorBean error = new ErrorBean();
        error.setType(data.getClass().getSimpleName());
        error.setErrorCode(data.getErrorCode());
        error.setMessage(data.getMessage());
        error.setMoreInfoUrl(data.getMoreInfoUrl());
        error.setStacktrace(getStackTrace(data));
        ResponseBuilder builder = Response.status(data.getHttpCode()).header("X-Apiman-Error", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        // If CORS is being used, make sure to add X-Apiman-Error to the exposed headers
        if (origin != null) {
            builder = builder.header("Access-Control-Expose-Headers", "X-Apiman-Error") //$NON-NLS-1$ //$NON-NLS-2$
                    .header("Access-Control-Allow-Origin", origin) //$NON-NLS-1$
                    .header("Access-Control-Allow-Credentials", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        builder.type(MediaType.APPLICATION_JSON_TYPE);
        return builder.entity(error).build();
    }

    /**
     * Gets the full stack trace for the given exception and returns it as a
     * string.
     * @param data
     */
    private String getStackTrace(AbstractRestException data) {
        StringBuilderWriter writer = new StringBuilderWriter();
        try {
            data.printStackTrace(new PrintWriter(writer));
            return writer.getBuilder().toString();
        } finally {
            writer.close();
        }
    }

}
