package io.apiman.manager.api.rest.exceptions;

import io.apiman.manager.api.rest.exceptions.ContractNotFoundException;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class InvalidContractStatusException extends AbstractUserException {

    public InvalidContractStatusException(String message) {
        super(message);
    }

    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.CONTRACT_STATUS_ERROR;
    }

    /**
     * @see AbstractRestException#getHttpCode()
     */
    @Override
    public int getHttpCode() {
        return ErrorCodes.HTTP_STATUS_CODE_INVALID_STATE;
    }

    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.CONTRACT_STATUS_ERROR_INFO;
    }

}
