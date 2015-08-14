package io.apiman.gateway.engine.influxdb;

import io.apiman.gateway.engine.components.http.IHttpClientResponse;

public class InfluxException extends RuntimeException {
    private static final long serialVersionUID = 3481055452967828740L;
    private IHttpClientResponse response;
    private Throwable exception;

    public InfluxException(IHttpClientResponse r) {
        this.response = r;
    }

    public InfluxException(Throwable e) {
        super(e);
        this.exception = e;
    }

    public InfluxException(String message) {
        super(message);
    }

    public boolean isBadResponse() {
        return response != null;
    }

    public IHttpClientResponse getResponse() {
        return response;
    }

    public boolean isException() {
        return exception != null;
    }

    public Throwable getException() {
        return exception;
    }
}