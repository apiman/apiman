package io.apiman.gateway.engine.beans;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = As.EXISTING_PROPERTY,
        property = "probeType",
        visible = true)
public interface IPolicyProbeResponse {
    String getProbeType();
}
