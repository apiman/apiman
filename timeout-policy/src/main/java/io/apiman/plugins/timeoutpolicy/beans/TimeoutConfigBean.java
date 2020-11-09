package io.apiman.plugins.timeoutpolicy.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeoutConfigBean {

    @JsonProperty
    private String timeoutConnect;

    @JsonProperty
    private String timeoutRead;

    public String getTimeoutConnect() {
        return timeoutConnect;
    }

    public void setTimeoutConnect(String timeoutConnect) {
        this.timeoutConnect = timeoutConnect;
    }

    public String getTimeoutRead() {
        return timeoutRead;
    }

    public void setTimeoutRead(String timeoutRead) {
        this.timeoutRead = timeoutRead;
    }

}
