package io.apiman.manager.api.beans.summary;

import java.io.Serializable;

/**
 * A summary bean for a gateway endpoint.
 *
 * @author benjamin.kihm@scheer-group.com
 */
public class GatewayEndpointSummaryBean implements Serializable {

    private static final long serialVersionUID = -213566882390484357L;

    private String endpoint;

    /**
     * Get gateway endpoint
     * @return endpoint the gateway endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Set gateway endpoint
     * @param endpoint gateway endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
