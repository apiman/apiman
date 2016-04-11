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

package io.apiman.manager.api.jdbc;

import io.apiman.manager.api.beans.metrics.ClientUsagePerApiBean;
import io.apiman.manager.api.beans.metrics.HistogramIntervalType;
import io.apiman.manager.api.beans.metrics.ResponseStatsDataPoint;
import io.apiman.manager.api.beans.metrics.ResponseStatsHistogramBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerClientBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerPlanBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsSummaryBean;
import io.apiman.manager.api.beans.metrics.UsageDataPoint;
import io.apiman.manager.api.beans.metrics.UsageHistogramBean;
import io.apiman.manager.api.beans.metrics.UsagePerClientBean;
import io.apiman.manager.api.beans.metrics.UsagePerPlanBean;
import io.apiman.manager.api.core.IMetricsAccessor;
import io.apiman.manager.api.core.metrics.AbstractMetricsAccessor;
import io.apiman.manager.api.jdbc.handlers.ClientUsagePerApiHandler;
import io.apiman.manager.api.jdbc.handlers.ResponseStatsHistogramHandler;
import io.apiman.manager.api.jdbc.handlers.ResponseStatsPerClientHandler;
import io.apiman.manager.api.jdbc.handlers.ResponseStatsPerPlanHandler;
import io.apiman.manager.api.jdbc.handlers.ResponseStatsSummaryHandler;
import io.apiman.manager.api.jdbc.handlers.UsageHistogramHandler;
import io.apiman.manager.api.jdbc.handlers.UsagePerClientHandler;
import io.apiman.manager.api.jdbc.handlers.UsagePerPlanHandler;

import java.sql.SQLException;
import java.util.Map;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.joda.time.DateTime;

/**
 * An implementation of a {@link IMetricsAccessor} that uses JDBC to query a relational
 * database for the appropriate information.
 * 
 * @author eric.wittmann@gmail.com
 */
public class JdbcMetricsAccessor extends AbstractMetricsAccessor implements IMetricsAccessor {
    
    protected DataSource ds;

    /**
     * Constructor.
     * @param config
     */
    public JdbcMetricsAccessor(Map<String, String> config) {
        String dsJndiLocation = config.get("datasource.jndi-location"); //$NON-NLS-1$
        if (dsJndiLocation == null) {
            throw new RuntimeException("Missing datasource JNDI location from JdbcRegistry configuration."); //$NON-NLS-1$
        }
        ds = lookupDS(dsJndiLocation);
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public UsageHistogramBean getUsage(String organizationId, String apiId, String version,
            HistogramIntervalType interval, DateTime from, DateTime to) {
        UsageHistogramBean rval = new UsageHistogramBean();
        Map<Long, UsageDataPoint> index = generateHistogramSkeleton(rval, from, to, interval, UsageDataPoint.class, Long.class);
        
        try {
            QueryRunner run = new QueryRunner(ds);
            String gbColumn = groupByColumn(interval);
            String sql = "SELECT " + gbColumn + ", count(*) FROM requests WHERE api_org_id = ? AND api_id = ? AND api_version = ? AND rstart >= ? AND rstart < ? GROUP BY " + gbColumn; //$NON-NLS-1$ //$NON-NLS-2$
            ResultSetHandler<UsageHistogramBean> handler = new UsageHistogramHandler(rval, index);
            run.query(sql, handler, organizationId, apiId, version, from.getMillis(), to.getMillis());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerClient(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public UsagePerClientBean getUsagePerClient(String organizationId, String apiId, String version,
            DateTime from, DateTime to) {
        try {
            QueryRunner run = new QueryRunner(ds);
            String sql = "SELECT client_id, count(*) FROM requests WHERE api_org_id = ? AND api_id = ? AND api_version = ? AND rstart >= ? AND rstart < ? GROUP BY client_id"; //$NON-NLS-1$
            ResultSetHandler<UsagePerClientBean> handler = new UsagePerClientHandler();
            return run.query(sql, handler, organizationId, apiId, version, from.getMillis(), to.getMillis());
        } catch (SQLException e) {
            e.printStackTrace();
            return new UsagePerClientBean();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getUsagePerPlan(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public UsagePerPlanBean getUsagePerPlan(String organizationId, String apiId, String version,
            DateTime from, DateTime to) {
        try {
            QueryRunner run = new QueryRunner(ds);
            String sql = "SELECT plan, count(*) FROM requests WHERE api_org_id = ? AND api_id = ? AND api_version = ? AND rstart >= ? AND rstart < ? GROUP BY plan"; //$NON-NLS-1$
            ResultSetHandler<UsagePerPlanBean> handler = new UsagePerPlanHandler();
            return run.query(sql, handler, organizationId, apiId, version, from.getMillis(), to.getMillis());
        } catch (SQLException e) {
            e.printStackTrace();
            return new UsagePerPlanBean();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStats(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsHistogramBean getResponseStats(String organizationId, String apiId, String version,
            HistogramIntervalType interval, DateTime from, DateTime to) {
        ResponseStatsHistogramBean rval = new ResponseStatsHistogramBean();
        Map<Long, ResponseStatsDataPoint> index = generateHistogramSkeleton(rval, from, to, interval, ResponseStatsDataPoint.class, Long.class);
        
        try {
            QueryRunner run = new QueryRunner(ds);
            String gbColumn = groupByColumn(interval);
            String sql = "SELECT " + gbColumn + ", resp_type, count(*) FROM requests WHERE api_org_id = ? AND api_id = ? AND api_version = ? AND rstart >= ? AND rstart < ? GROUP BY resp_type," + gbColumn; //$NON-NLS-1$ //$NON-NLS-2$
            ResultSetHandler<ResponseStatsHistogramBean> handler = new ResponseStatsHistogramHandler(rval, index);
            run.query(sql, handler, organizationId, apiId, version, from.getMillis(), to.getMillis());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsSummary(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsSummaryBean getResponseStatsSummary(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        try {
            QueryRunner run = new QueryRunner(ds);
            String sql = "SELECT resp_type, count(*) FROM requests WHERE api_org_id = ? AND api_id = ? AND api_version = ? AND rstart >= ? AND rstart < ? GROUP BY resp_type"; //$NON-NLS-1$
            ResultSetHandler<ResponseStatsSummaryBean> handler = new ResponseStatsSummaryHandler();
            return run.query(sql, handler, organizationId, apiId, version, from.getMillis(), to.getMillis());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseStatsSummaryBean();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsPerClient(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsPerClientBean getResponseStatsPerClient(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        try {
            QueryRunner run = new QueryRunner(ds);
            String sql = "SELECT client_id, resp_type, count(*) FROM requests WHERE api_org_id = ? AND api_id = ? AND api_version = ? AND rstart >= ? AND rstart < ? GROUP BY client_id, resp_type"; //$NON-NLS-1$
            ResultSetHandler<ResponseStatsPerClientBean> handler = new ResponseStatsPerClientHandler();
            return run.query(sql, handler, organizationId, apiId, version, from.getMillis(), to.getMillis());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseStatsPerClientBean();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getResponseStatsPerPlan(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ResponseStatsPerPlanBean getResponseStatsPerPlan(String organizationId, String apiId,
            String version, DateTime from, DateTime to) {
        try {
            QueryRunner run = new QueryRunner(ds);
            String sql = "SELECT plan, resp_type, count(*) FROM requests WHERE api_org_id = ? AND api_id = ? AND api_version = ? AND rstart >= ? AND rstart < ? GROUP BY plan, resp_type"; //$NON-NLS-1$
            ResultSetHandler<ResponseStatsPerPlanBean> handler = new ResponseStatsPerPlanHandler();
            return run.query(sql, handler, organizationId, apiId, version, from.getMillis(), to.getMillis());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseStatsPerPlanBean();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IMetricsAccessor#getClientUsagePerApi(java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public ClientUsagePerApiBean getClientUsagePerApi(String organizationId, String clientId, String version,
            DateTime from, DateTime to) {
        try {
            QueryRunner run = new QueryRunner(ds);
            String sql = "SELECT api_id, count(*) FROM requests WHERE client_org_id = ? AND client_id = ? AND client_version = ? AND rstart >= ? AND rstart < ? GROUP BY api_id"; //$NON-NLS-1$
            ResultSetHandler<ClientUsagePerApiBean> handler = new ClientUsagePerApiHandler();
            return run.query(sql, handler, organizationId, clientId, version, from.getMillis(), to.getMillis());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ClientUsagePerApiBean();
        }
    }

    /**
     * Lookup the datasource in JNDI.
     * @param dsJndiLocation
     */
    private static DataSource lookupDS(String dsJndiLocation) {
        DataSource ds;
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(dsJndiLocation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (ds == null) {
            throw new RuntimeException("Datasource not found: " + dsJndiLocation); //$NON-NLS-1$
        }
        return ds;
    }

    /**
     * Returns the group-by column to use for the given interval.
     * @param interval
     */
    private static String groupByColumn(HistogramIntervalType interval) {
        return interval.name();
    }
        
}
