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

package io.apiman.manager.api.rest.exceptions.mappers;

import io.apiman.common.config.ConfigFactory;
import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.exceptions.ErrorBean;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.output.StringBuilderWriter;

/**
 * Provider that maps an error.
 *
 * @author eric.wittmann@redhat.com
 */
@Provider
@ApplicationScoped
public class BeanValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(BeanValidationExceptionMapper.class);
    private static final String ENABLE_STACKTRACE = "apiman-manager.config.features.rest-response-should-contain-stacktraces";
    private static final Configuration CONFIG = ConfigFactory.createConfig();

    /**
     * Constructor.
     */
    public BeanValidationExceptionMapper() {
    }

    /**
     * @see ExceptionMapper#toResponse(Throwable)
     */
    @Override
    public Response toResponse(ValidationException data) {
        ErrorBean error = new ErrorBean();
        error.setType(data.getClass().getSimpleName());
        error.setErrorCode(422);
        error.setMessage(data.getMessage());
        error.setStacktrace(getStackTrace(data));
        ResponseBuilder builder = Response.status(422).header("X-Apiman-Error", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        // If CORS is being used, make sure to add X-Apiman-Error to the exposed headers
        builder.type(MediaType.APPLICATION_JSON_TYPE);
        return builder.entity(error).build();
    }

    /**
     * Gets the full stack trace for the given exception and returns it as a
     * string.
     * @param exception
     */
    private String getStackTrace(Throwable exception) {
        LOGGER.error(exception);
        if (isStackTraceEnabled()) {
            try (StringBuilderWriter writer = new StringBuilderWriter()) {
                return writer.getBuilder().toString();
            }
        } else {
            return null;
        }
    }

    private boolean isStackTraceEnabled() {
        return CONFIG.getBoolean(ENABLE_STACKTRACE, false);
    }
}
