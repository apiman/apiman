package io.apiman.manager.api.rest.exceptions;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class InvalidContractStatusException extends AbstractUserException {

    private static final long serialVersionUID = -5204892141429625L;

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
