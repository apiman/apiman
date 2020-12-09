package io.apiman.gateway.engine.handler;

import io.apiman.gateway.engine.beans.exceptions.ConnectorException;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

/**
 * This class provides helper methods for vert.x and servlet gateway implementations.
 */
public final class ErrorHandler {

    /**
     * Private constructor
     */
    private ErrorHandler() {
    }

    /**
     * This method handles a connection error that was caused while connecting the gateway to the backend.
     *
     * @param error the connection error to be handled
     * @return a new ConnectorException
     */
    public static ConnectorException handleConnectionError(Throwable error) {
        ConnectorException ce = null;
        if (error instanceof UnknownHostException || error instanceof ConnectException || error instanceof NoRouteToHostException) {
            ce = new ConnectorException("Unable to connect to backend", error); //$NON-NLS-1$
            ce.setStatusCode(502); // BAD GATEWAY
        } else if (error instanceof InterruptedIOException || error instanceof java.util.concurrent.TimeoutException) {
            ce = new ConnectorException("Connection to backend terminated. " + error.getMessage(), error); //$NON-NLS-1$
            ce.setStatusCode(504); // GATEWAY TIMEOUT

        }
        if (ce != null) {
            return ce;
        } else {
            return new ConnectorException(error.getMessage(), error);
        }
    }
}
