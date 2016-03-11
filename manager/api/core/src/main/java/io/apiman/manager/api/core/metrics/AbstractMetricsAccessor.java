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

package io.apiman.manager.api.core.metrics;

import io.apiman.manager.api.beans.metrics.HistogramBean;
import io.apiman.manager.api.beans.metrics.HistogramDataPoint;
import io.apiman.manager.api.beans.metrics.HistogramIntervalType;
import io.apiman.manager.api.core.IMetricsAccessor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Optional base class for metrics implementations.
 * @author eric.wittmann@gmail.com
 */
public abstract class AbstractMetricsAccessor implements IMetricsAccessor {
    
    /**
     * Based on the given interval, create a "floor" value relative to the
     * given date.  This basically means zero'ing out certain fields (different
     * fields based on the interval type) and returning the modified date.
     * @param date
     * @param interval
     */
    protected static DateTime floor(DateTime date, HistogramIntervalType interval) {
        DateTime rval = date.withMillisOfSecond(0);
        
        switch (interval) {
        case day:
            rval = rval.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
            break;
        case hour:
            rval = rval.withMinuteOfHour(0).withSecondOfMinute(0);
            break;
        case minute:
            rval = rval.withSecondOfMinute(0);
            break;
        case month:
            rval = rval.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
            break;
        case week:
            rval = rval.withDayOfWeek(DateTimeConstants.MONDAY).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
            break;
        }
        
        return rval;
    }

    /**
     * @param date
     */
    protected static String formatDate(DateTime date) {
        return ISODateTimeFormat.dateTimeNoMillis().print(date);
    }

    /**
     * @param date
     */
    protected static String formatDateWithMillis(DateTime date) {
        return ISODateTimeFormat.dateTime().print(date);
    }

    /**
     * @param date
     */
    protected static String formatDate(Calendar date) {
        return DateFormatUtils.formatUTC(date.getTimeInMillis(), "yyyy-MM-dd'T'HH:mm:ss'Z'"); //$NON-NLS-1$
    }

    /**
     * @param date
     */
    protected static String formatDateWithMillis(Calendar date) {
        return DateFormatUtils.formatUTC(date.getTimeInMillis(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //$NON-NLS-1$
    }

    /**
     * Generate the histogram buckets based on the time frame requested and the interval.  This will
     * add an entry for each 'slot' or 'bucket' in the histogram, setting the count to 0.
     * @param rval
     * @param from
     * @param to
     * @param interval
     */
    public static <T extends HistogramDataPoint> Map<String, T> generateHistogramSkeleton(HistogramBean<T> rval, DateTime from, DateTime to,
            HistogramIntervalType interval, Class<T> dataType) {
        Map<String, T> index = new HashMap<>();

        Calendar fromCal = from.toGregorianCalendar();
        Calendar toCal = to.toGregorianCalendar();

        switch(interval) {
            case day:
                fromCal.set(Calendar.HOUR_OF_DAY, 0);
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                break;
            case hour:
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                break;
            case minute:
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                break;
            case month:
                fromCal.set(Calendar.DAY_OF_MONTH, 1);
                fromCal.set(Calendar.HOUR_OF_DAY, 0);
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                break;
            case week:
                fromCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                fromCal.set(Calendar.HOUR_OF_DAY, 0);
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                break;
            default:
                break;
        }

        while (fromCal.before(toCal)) {
            String label = formatDateWithMillis(fromCal);
            T point;
            try {
                point = dataType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            point.setLabel(label);
            rval.addDataPoint(point);
            index.put(label, point);
            switch (interval) {
                case day:
                    fromCal.add(Calendar.DAY_OF_YEAR, 1);
                    break;
                case hour:
                    fromCal.add(Calendar.HOUR_OF_DAY, 1);
                    break;
                case minute:
                    fromCal.add(Calendar.MINUTE, 1);
                    break;
                case month:
                    fromCal.add(Calendar.MONTH, 1);
                    break;
                case week:
                    fromCal.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                default:
                    break;
            }
        }

        return index;

    }
}
