package io.apiman.plugins.transformation_policy.beans;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Configuration object for the Transformation policy.
 *
 * @author Alexandre Kieling {@literal <alex.kieling@gmail.com>}
 */
public class TransformationConfigBean {

    @JsonProperty
    private DataFormat clientFormat;
	@JsonProperty
    private DataFormat serverFormat;

    /**
     * @return the transformation's client format 
     */
    public DataFormat getClientFormat() {
        return clientFormat;
    }

    /**
     * @param clientFormat the transformation's client format
     */
    public void setClientFormat(DataFormat clientFormat) {
        this.clientFormat = clientFormat;
    }

    /**
     * @return the transformation's server format 
     */
    public DataFormat getServerFormat() {
        return serverFormat;
    }

    /**
     * @param serverFormat the transformation's server format
     */
    public void setServerFormat(DataFormat serverFormat) {
        this.serverFormat = serverFormat;
    }
}
