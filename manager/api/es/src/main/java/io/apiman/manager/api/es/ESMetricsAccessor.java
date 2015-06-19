/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.es;

import io.apiman.manager.api.beans.metrics.UsageHistogramBean;
import io.apiman.manager.api.beans.metrics.UsageHistogramIntervalType;
import io.apiman.manager.api.beans.metrics.UsagePerAppBean;
import io.apiman.manager.api.beans.metrics.UsagePerPlanBean;
import io.apiman.manager.api.core.IMetricsAccessor;
import io.searchbox.client.JestClient;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * An elasticsearch implementation of the {@link IMetricsAccessor} interface.  This
 * implementation knows how to get metrics/analytics information out of an
 * elasticsearch store.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class ESMetricsAccessor implements IMetricsAccessor {

    private static final String INDEX_NAME = "apiman_metrics"; //$NON-NLS-1$

    @Inject @Named("metrics")
    JestClient esClient;

    /**
     * Constructor.
     */
    public ESMetricsAccessor() {
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.UsageHistogramIntervalType, java.util.Date, java.util.Date)
     */
    @Override
    public UsageHistogramBean getUsage(String organizationId, String serviceId, String version,
            UsageHistogramIntervalType interval, Date from, Date to) {
        UsageHistogramBean rval = new UsageHistogramBean();
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerApp(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)
     */
    @Override
    public UsagePerAppBean getUsagePerApp(String organizationId, String serviceId, String version,
            Date fromDate, Date toDate) {
        UsagePerAppBean rval = new UsagePerAppBean();
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerPlan(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)
     */
    @Override
    public UsagePerPlanBean getUsagePerPlan(String organizationId, String serviceId, String version,
            Date fromDate, Date toDate) {
        UsagePerPlanBean rval = new UsagePerPlanBean();
        return rval;
    }

}
