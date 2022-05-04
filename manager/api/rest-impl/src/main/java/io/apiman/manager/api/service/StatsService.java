package io.apiman.manager.api.service;

import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.metrics.ClientUsagePerApiBean;
import io.apiman.manager.api.beans.metrics.HistogramIntervalType;
import io.apiman.manager.api.beans.metrics.ResponseStatsHistogramBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerClientBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerPlanBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsSummaryBean;
import io.apiman.manager.api.beans.metrics.UsageHistogramBean;
import io.apiman.manager.api.beans.metrics.UsagePerClientBean;
import io.apiman.manager.api.beans.metrics.UsagePerPlanBean;
import io.apiman.manager.api.core.IMetricsAccessor;
import io.apiman.manager.api.rest.exceptions.InvalidMetricCriteriaException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class StatsService {

    private static final long ONE_MINUTE_MILLIS = 1 * 60 * 1000L;
    private static final long ONE_HOUR_MILLIS = 1 * 60 * 60 * 1000L;
    private static final long ONE_DAY_MILLIS = 1 * 24 * 60 * 60 * 1000L;
    private static final long ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L;
    private static final long ONE_MONTH_MILLIS = 30 * 24 * 60 * 60 * 1000L;


    private IMetricsAccessor metrics;
    private ISecurityContext securityContext;

    @Inject
    public StatsService(IMetricsAccessor metrics, ISecurityContext securityContext) {
        this.metrics = metrics;
        this.securityContext = securityContext;
    }

    public StatsService() {
    }

    public UsagePerClientBean getUsagePerClient(String organizationId, String apiId, String version,
        String fromDate, String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException {

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(
                Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getUsagePerClient(organizationId, apiId, version, from, to);
    }

    public ClientUsagePerApiBean getClientUsagePerApi(String organizationId, String clientId,
        String version, String fromDate, String toDate) throws NotAuthorizedException,
        InvalidMetricCriteriaException {

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(
                Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);

        return metrics.getClientUsagePerApi(organizationId, clientId, version, from, to);
    }

    public UsageHistogramBean getUsage(String organizationId, String apiId, String version,
        HistogramIntervalType interval, String fromDate, String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException {

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);

        if (interval == null) {
            interval = HistogramIntervalType.day;
        }
        validateMetricRange(from, to);
        validateTimeSeriesMetric(from, to, interval);
        return metrics.getUsage(organizationId, apiId, version, interval, from, to);
    }

    public UsagePerPlanBean getUsagePerPlan(String organizationId, String apiId, String version,
        String fromDate, String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException {

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getUsagePerPlan(organizationId, apiId, version, from, to);
    }

    public ResponseStatsHistogramBean getResponseStats(String organizationId, String apiId,
        String version, HistogramIntervalType interval, String fromDate, String toDate)
        throws NotAuthorizedException, InvalidMetricCriteriaException {

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        if (interval == null) {
            interval = HistogramIntervalType.day;
        }
        validateMetricRange(from, to);
        validateTimeSeriesMetric(from, to, interval);
        return metrics.getResponseStats(organizationId, apiId, version, interval, from, to);
    }

    public ResponseStatsSummaryBean getResponseStatsSummary(String organizationId, String apiId,
        String version, String fromDate, String toDate) throws NotAuthorizedException,
        InvalidMetricCriteriaException {

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getResponseStatsSummary(organizationId, apiId, version, from, to);
    }

    public ResponseStatsPerClientBean getResponseStatsPerClient(String organizationId, String apiId,
        String version, String fromDate, String toDate) throws NotAuthorizedException,
        InvalidMetricCriteriaException {

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getResponseStatsPerClient(organizationId, apiId, version, from, to);
    }

    public ResponseStatsPerPlanBean getResponseStatsPerPlan(String organizationId, String apiId,
        String version, String fromDate, String toDate) throws NotAuthorizedException,
        InvalidMetricCriteriaException {

        if (fromDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(
                Messages.i18n.format("MissingOrInvalidParam", "fromDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (toDate == null) {
            throw ExceptionFactory.invalidMetricCriteriaException(
                Messages.i18n.format("MissingOrInvalidParam", "toDate")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        DateTime from = parseFromDate(fromDate);
        DateTime to = parseToDate(toDate);
        validateMetricRange(from, to);
        return metrics.getResponseStatsPerPlan(organizationId, apiId, version, from, to);
    }

    /**
     * Ensures that the given date range is valid.
     */
    private void validateMetricRange(DateTime from, DateTime to) throws InvalidMetricCriteriaException {
        if (from.isAfter(to)) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("OrganizationResourceImpl.InvalidMetricDateRange")); //$NON-NLS-1$
        }
    }

    /**
     * Ensures that a time series can be created for the given date range and
     * interval, and that the
     */
    private void validateTimeSeriesMetric(DateTime from, DateTime to, HistogramIntervalType interval)
        throws InvalidMetricCriteriaException {
        long millis = to.getMillis() - from.getMillis();
        long divBy = ONE_DAY_MILLIS;
        switch (interval) {
            case day:
                divBy = ONE_DAY_MILLIS;
                break;
            case hour:
                divBy = ONE_HOUR_MILLIS;
                break;
            case minute:
                divBy = ONE_MINUTE_MILLIS;
                break;
            case month:
                divBy = ONE_MONTH_MILLIS;
                break;
            case week:
                divBy = ONE_WEEK_MILLIS;
                break;
            default:
                break;
        }
        long totalDataPoints = millis / divBy;
        if (totalDataPoints > 5000) {
            throw ExceptionFactory.invalidMetricCriteriaException(Messages.i18n.format("OrganizationResourceImpl.MetricDataSetTooLarge")); //$NON-NLS-1$
        }
    }


    /**
     * Parse the to date query param.
     */
    private DateTime parseFromDate(String fromDate) {
        // Default to the last 30 days
        DateTime defaultFrom = new DateTime().withZone(DateTimeZone.UTC).minusDays(30).withHourOfDay(0)
            .withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        return parseDate(fromDate, defaultFrom, true);
    }

    /**
     * Parse the from date query param.
     */
    private DateTime parseToDate(String toDate) {
        // Default to now
        return parseDate(toDate, new DateTime().withZone(DateTimeZone.UTC), false);
    }

    /**
     * Parses a query param representing a date into an actual date object.
     */
    private static DateTime parseDate(String dateStr, DateTime defaultDate, boolean floor) {
        if ("now".equals(dateStr)) { //$NON-NLS-1$
            return new DateTime();
        }
        if (dateStr.length() == 10) {
            DateTime parsed = ISODateTimeFormat.date().withZone(DateTimeZone.UTC).parseDateTime(dateStr);
            // If what we want is the floor, then just return it.  But if we want the
            // ceiling of the date, then we need to set the right params.
            if (!floor) {
                parsed = parsed.plusDays(1).minusMillis(1);
            }
            return parsed;
        }
        if (dateStr.length() == 20) {
            return ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime(dateStr);
        }
        if (dateStr.length() == 24) {
            return ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).parseDateTime(dateStr);
        }
        return defaultDate;
    }

}
