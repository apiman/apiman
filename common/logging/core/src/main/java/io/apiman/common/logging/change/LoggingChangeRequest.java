package io.apiman.common.logging.change;

import io.apiman.common.logging.LogLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoggingChangeRequest that = (LoggingChangeRequest) o;
        return Objects.equals(logOverrides, that.logOverrides)
            && Arrays.equals(loggerConfig, that.loggerConfig);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(logOverrides);
        result = 31 * result + Arrays.hashCode(loggerConfig);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LoggingChangeRequest.class.getSimpleName() + "[", "]")
            .add("logOverrides=" + logOverrides)
            .add("loggerConfig=" + Arrays.toString(loggerConfig))
            .toString();
    }
}
