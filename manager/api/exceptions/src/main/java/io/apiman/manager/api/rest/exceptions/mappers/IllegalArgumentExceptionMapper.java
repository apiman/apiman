package io.apiman.manager.api.rest.exceptions.mappers;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.exceptions.ErrorBean;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Provider
@ApplicationScoped
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    private final IApimanLogger log = ApimanLoggerFactory.getLogger(IllegalArgumentExceptionMapper.class);

    public IllegalArgumentExceptionMapper() {
    }

    @Override
    public Response toResponse(IllegalArgumentException data) {
        ErrorBean error = new ErrorBean();
        error.setType(data.getClass().getSimpleName());
        error.setErrorCode(Status.BAD_REQUEST.getStatusCode());
        error.setMessage("A provided parameter could not be accepted: " + data.getMessage());
        // error.setMoreInfoUrl(data);
        // error.setStacktrace(getStackTrace(data));
        ResponseBuilder builder = Response.status(Status.BAD_REQUEST)
                .header("X-Apiman-Error", "true");
        builder.type(MediaType.APPLICATION_JSON_TYPE);
        log.error(data, data.getMessage());
        return builder.entity(error).build();
    }

}
