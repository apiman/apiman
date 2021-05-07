package io.apiman.gateway.engine.beans;

import java.util.HashMap;
import java.util.Map;

//@JsonTypeInfo(
//    use = JsonTypeInfo.Id.NAME,
//    include = JsonTypeInfo.As.PROPERTY,
//    property = "type",
//    visible = true
//)
//@JsonSubTypes({
//    @Type(value = .class, name = "FULL_CONFIG_FILE_OVERRIDE"),
//    @Type(value = LogMappings.class, name = "MAPPINGS")
//})
public class LoggingChangeRequest {

    private Map<String, LogLevel> logOverrides = new HashMap<>();

    private byte[] loggerConfig;

    public Map<String, LogLevel> getLogOverrides() {
        return logOverrides;
    }

    public LoggingChangeRequest setLogOverrides(
        Map<String, LogLevel> logOverrides) {
        this.logOverrides = logOverrides;
        return this;
    }

    public byte[] getLoggerConfig() {
        return loggerConfig;
    }

    public LoggingChangeRequest setLoggerConfig(byte[] loggerConfig) {
        this.loggerConfig = loggerConfig;
        return this;
    }

    enum LogLevel {
        ERROR, WARN, INFO, DEBUG, TRACE
    }

}
