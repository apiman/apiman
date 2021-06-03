/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.common.logging.change;

import io.apiman.common.logging.LogLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

/**
 * The type Logging change request.
 */
@JsonInclude(Include.NON_NULL)
public class LoggingChangeRequest {

    private Map<String, LogLevel> logOverrides = new HashMap<>();

    private byte[] loggerConfig;

    public Map<String, LogLevel> getLogOverrides() {
        return logOverrides;
    }

    public LoggingChangeRequest() {
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
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
