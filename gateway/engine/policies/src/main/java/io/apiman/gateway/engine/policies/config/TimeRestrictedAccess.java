/*
 * Copyright 2016 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.engine.policies.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Determines timeslot when service with specific path pattern can be called.
 * Extendible by adding additional cron type ranges like weeks,month years.
 * Empty values represents full range.
 * <p>
 * Elements:
 * <p>
 * - timeStart,timeEnd represents time range (in current implementation date part is ignored)
 * - dayStart,dayEnd: represents one of the weekdays with values from 1 to 7 (from Monday to Sunday)
 *
 * @author wtr@redhat.com
 * @author florian.volk@scheer-group.com
 */
public class TimeRestrictedAccess {

    private static final String TIME_PATTERN = "HH:mm:ss"; //$NON-NLS-1$


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIME_PATTERN)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime timeStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIME_PATTERN)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime timeEnd;
    private Integer dayStart;
    private Integer dayEnd;
    private String pathPattern;

    /**
     * Constructor.
     */
    public TimeRestrictedAccess() {
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public OffsetDateTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(OffsetDateTime timeStart) {
        this.timeStart = timeStart;
    }

    public OffsetDateTime getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(OffsetDateTime timeEnd) {
        this.timeEnd = timeEnd;
    }

    public Integer getDayStart() {
        return dayStart;
    }

    public void setDayStart(Integer dayStart) {
        this.dayStart = dayStart;
    }

    public Integer getDayEnd() {
        return dayEnd;
    }

    public void setDayEnd(Integer dayEnd) {
        this.dayEnd = dayEnd;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dayEnd == null) ? 0 : dayEnd.hashCode());
        result = prime * result + ((dayStart == null) ? 0 : dayStart.hashCode());
        result = prime * result + ((pathPattern == null) ? 0 : pathPattern.hashCode());
        result = prime * result + ((timeEnd == null) ? 0 : timeEnd.hashCode());
        result = prime * result + ((timeStart == null) ? 0 : timeStart.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeRestrictedAccess other = (TimeRestrictedAccess) obj;
        if (dayEnd == null) {
            if (other.dayEnd != null)
                return false;
        } else if (!dayEnd.equals(other.dayEnd))
            return false;
        if (dayStart == null) {
            if (other.dayStart != null)
                return false;
        } else if (!dayStart.equals(other.dayStart))
            return false;
        if (pathPattern == null) {
            if (other.pathPattern != null)
                return false;
        } else if (!pathPattern.equals(other.pathPattern))
            return false;
        if (timeEnd == null) {
            if (other.timeEnd != null)
                return false;
        } else if (!timeEnd.equals(other.timeEnd))
            return false;
        if (timeStart == null) {
            return other.timeStart == null;
        } else return timeStart.equals(other.timeStart);
    }


    /**
     * Custom deserializer as @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIME_PATTERN) is not able
     * to parse the config string ("07:58:13") directly into the format that is currently need (1970-01-01T07:58:13Z)
     */
    public static class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
        // make sure we are parsing the config always as UTC despite the gateways TZ
        private final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern(TIME_PATTERN)
                .parseDefaulting(ChronoField.EPOCH_DAY, 0) // set default date
                .toFormatter()
                .withZone(ZoneOffset.UTC);

        @Override
        public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return OffsetDateTime.parse(p.getText(), formatter);
        }
    }
}
