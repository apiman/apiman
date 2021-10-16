package io.apiman.manager.api.rest.exceptions;

import javax.ws.rs.core.Response.Status;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class InputTooLargeException extends AbstractInvalidInputException {

    public InputTooLargeException(String message) {
        super(message);
    }

    @Override
    public int getHttpCode() {
        return Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode();
    }

    @Override
    public int getErrorCode() {
        return ErrorCodes.PARAM_INVALID;
    }

    @Override
    public String getMoreInfoUrl() {
        return null;
    }
}
