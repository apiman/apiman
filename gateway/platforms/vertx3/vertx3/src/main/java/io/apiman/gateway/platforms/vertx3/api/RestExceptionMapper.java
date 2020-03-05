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

package io.apiman.gateway.platforms.vertx3.api;

import io.apiman.gateway.api.rest.exceptions.GatewayApiErrorBean;
import io.apiman.gateway.api.rest.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.exceptions.AbstractEngineException;
import io.apiman.gateway.engine.beans.exceptions.IStatusCode;

import java.io.PrintWriter;

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
public class RestExceptionMapper implements ExceptionMapper<AbstractEngineException> {

    /**
     * Constructor.
     */
    public RestExceptionMapper() {
    }
    /**
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
     */
    @Override
    public Response toResponse(AbstractEngineException data) {
        int errorCode;
        if (data instanceof IStatusCode) {
            errorCode = ((IStatusCode) data).getStatusCode();
        } else {
            errorCode = mapExceptionToHttpErrorCode(data);
        }
        GatewayApiErrorBean error = new GatewayApiErrorBean();
        error.setErrorType(data.getClass().getSimpleName());
        error.setMessage(data.getMessage());
        error.setStacktrace(getStackTrace(data));
        ResponseBuilder builder = Response.status(errorCode).header("X-API-Gateway-Error", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        builder.type(MediaType.APPLICATION_JSON_TYPE);
        return builder.entity(error).build();
    }

    private int mapExceptionToHttpErrorCode(AbstractEngineException data) {
        int errorCode = 500;
        if (data instanceof NotAuthorizedException) {
            errorCode = 403;
        }
        return errorCode;
    }

    /**
     * Gets the full stack trace for the given exception and returns it as a
     * string.
     * @param data
     */
    private String getStackTrace(AbstractEngineException data) {
        try (StringBuilderWriter writer = new StringBuilderWriter()) {
            data.printStackTrace(new PrintWriter(writer));
            return writer.getBuilder().toString();
        }
    }
}
