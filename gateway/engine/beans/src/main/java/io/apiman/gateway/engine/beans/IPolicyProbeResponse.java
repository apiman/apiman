package io.apiman.gateway.engine.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = As.WRAPPER_OBJECT,
        //property = "probeType",
        visible = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface IPolicyProbeResponse {
    String getProbeType();
    //void setProbeType(String name);
}
