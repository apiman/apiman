package io.apiman.plugins.jsonp_policy.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration object for the JSONP policy.
 *
 * @author Alexandre Kieling {@literal <alex.kieling@gmail.com>}
 */
public class JsonpConfigBean {

	@JsonProperty
    private String callbackParamName;

    /**
     * @return the parameter name used to specify the callback function 
     */
    public String getCallbackParamName() {
        return callbackParamName;
    }

    /**
     * @param callbackParamName the parameter name used to specify the callback function
     */
    public void setCallbackParamName(String callbackParamName) {
        this.callbackParamName = callbackParamName;
    }
}
