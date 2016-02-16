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

import java.util.Date;

/**
 * Determines timeslot when service with specific path pattern can be called.
 * Extendible by adding additional cron type ranges like weeks,month years.
 * Empty values represents full range.
 * 
 * Elements:
 * 
 * - timeStart,timeEnd represents time range (in current implementation date part is ignored)
 * - dayStart,dayEnd: represents one of the weekdays with values from 1 to 7 (from Monday to Sunday)
 * 
 * @see http://joda-time.sourceforge.net/apidocs/org/joda/time/DateTimeConstants.html#MONDAY
 * @author wtr@redhat.com
 */
public class TimeRestrictedAccess {
    private Date timeStart;
    private Date timeEnd;
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

    public Date getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Date timeStart) {
        this.timeStart = timeStart;
    }

    public Date getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Date timeEnd) {
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
            if (other.timeStart != null)
                return false;
        } else if (!timeStart.equals(other.timeStart))
            return false;
        return true;
    }

  
}
