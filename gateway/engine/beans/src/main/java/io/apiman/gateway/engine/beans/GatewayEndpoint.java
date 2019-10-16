package io.apiman.gateway.engine.beans;

import java.io.Serializable;

/**
 * Gateway endpoint.
 *
 * @author benjamin.kihm@scheer-group.com
 */
public class GatewayEndpoint implements Serializable {

    private static final long serialVersionUID = 5462692112594798640L;

    private String endpoint;

    /**
     * Constructor.
     */
    public GatewayEndpoint() {
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
