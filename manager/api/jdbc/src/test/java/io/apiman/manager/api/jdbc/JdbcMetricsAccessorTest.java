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

import io.apiman.common.util.ddl.DdlParser;
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
import io.apiman.test.common.util.TestUtil;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;

import org.apache.commons.dbcp.BasicDataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class JdbcMetricsAccessorTest {

    private static final String DB_JNDI_LOC = "java:/comp/env/jdbc/ApiGatewayDS";
    private static BasicDataSource ds = null;

    @BeforeClass
    public static void setup() {
        try {
            InitialContext ctx = TestUtil.initialContext();
            TestUtil.ensureCtx(ctx, "java:/comp/env");
            TestUtil.ensureCtx(ctx, "java:/comp/env/jdbc");
            ds = createInMemoryDatasource();
            ctx.bind(DB_JNDI_LOC, ds);
            System.out.println("DataSource created and bound to JNDI: " + DB_JNDI_LOC);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Test method for {@link io.apiman.manager.api.jdbc.JdbcMetricsAccessor#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, org.joda.time.DateTime, org.joda.time.DateTime)}.
     */
    @Test
    public void testGetUsage() {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetricsAccessor accessor = new JdbcMetricsAccessor(config);
        DateTime from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-01T00:00:00Z");
        DateTime to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-03-01T00:00:00Z");
        
        UsageHistogramBean usage = accessor.getUsage("TestOrg", "TestApi", "1.0", HistogramIntervalType.day, from, to);
        Assert.assertNotNull(usage);
        UsageDataPoint dataPoint = usage.getData().get(9);
        Assert.assertNotNull(dataPoint);
        Assert.assertEquals(2, dataPoint.getCount());
        Assert.assertEquals("2016-02-10T00:00:00.000Z", dataPoint.getLabel());
        Assert.assertEquals(9, usage.getData().get(22).getCount());
        Assert.assertEquals(6, usage.getData().get(23).getCount());
        Assert.assertEquals(0, usage.getData().get(24).getCount());

        // No data for "UnknownApi"
        usage = accessor.getUsage("TestOrg", "UnknownApi", "1.0", HistogramIntervalType.day, from, to);
        Assert.assertNotNull(usage);
        for (UsageDataPoint dp : usage.getData()) {
            Assert.assertEquals(0, dp.getCount());
        }

        from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-01-01T00:00:00Z");
        to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-12-31T00:00:00Z");
        usage = accessor.getUsage("TestOrg", "TestApi", "1.0", HistogramIntervalType.month, from, to);
        Assert.assertNotNull(usage);
        Assert.assertEquals(18, usage.getData().get(1).getCount());
        Assert.assertEquals(0, usage.getData().get(2).getCount());

        from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-10T00:00:00Z");
        to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-12T00:00:00Z");
        usage = accessor.getUsage("TestOrg", "TestApi", "1.0", HistogramIntervalType.hour, from, to);
        Assert.assertNotNull(usage);
        Assert.assertEquals(2, usage.getData().get(8).getCount());
        Assert.assertEquals(1, usage.getData().get(36).getCount());

        from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-10T07:00:00Z");
        to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-11T00:00:00Z");
        usage = accessor.getUsage("TestOrg", "TestApi", "1.0", HistogramIntervalType.minute, from, to);
        Assert.assertNotNull(usage);
        for (UsageDataPoint dp : usage.getData()) {
            if ("2016-02-10T08:56:00.000Z".equals(dp.getLabel())) {
                Assert.assertEquals(2, dp.getCount());
            } else {
                Assert.assertEquals(0, dp.getCount());
            }
        }
    }

    /**
     * Test for {@link JdbcMetricsAccessor#getUsagePerClient(String, String, String, DateTime, DateTime)}
     */
    @Test
    public void testGetUsagePerClient() {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetricsAccessor accessor = new JdbcMetricsAccessor(config);

        DateTime from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-01T00:00:00Z");
        DateTime to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-03-01T00:00:00Z");
        
        UsagePerClientBean usage = accessor.getUsagePerClient("TestOrg", "TestApi", "1.0", from, to);
        Map<String, Long> expectedData = new HashMap<>();
        expectedData.put("TestClient", 16L);
        expectedData.put("OtherClient", 2L);
        Assert.assertEquals(expectedData, usage.getData());
    }

    /**
     * Test for {@link JdbcMetricsAccessor#getUsagePerPlan(String, String, String, DateTime, DateTime)}
     */
    @Test
    public void testGetUsagePerPlan() {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetricsAccessor accessor = new JdbcMetricsAccessor(config);

        DateTime from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-01T00:00:00Z");
        DateTime to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-03-01T00:00:00Z");
        
        UsagePerPlanBean usage = accessor.getUsagePerPlan("TestOrg", "TestApi", "1.0", from, to);
        Map<String, Long> expectedData = new HashMap<>();
        expectedData.put("Silver", 10L);
        expectedData.put("Gold", 8L);
        Assert.assertEquals(expectedData, usage.getData());
    }

    /**
     * Test method for {@link io.apiman.manager.api.jdbc.JdbcMetricsAccessor#getResponseStats(String, String, String, HistogramIntervalType, DateTime, DateTime)}.
     */
    @Test
    public void testGetResponseStats() {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetricsAccessor accessor = new JdbcMetricsAccessor(config);
        
        DateTime from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-01-01T00:00:00Z");
        DateTime to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-12-31T00:00:00Z");
        ResponseStatsHistogramBean stats = accessor.getResponseStats("TestOrg", "TestApi", "1.0", HistogramIntervalType.month, from, to);
        Assert.assertNotNull(stats);
        for (ResponseStatsDataPoint dataPoint : stats.getData()) {
            if (dataPoint.getLabel().equals("2016-02-01T00:00:00.000Z")) {
                Assert.assertEquals(18, dataPoint.getTotal());
                Assert.assertEquals(1, dataPoint.getErrors());
                Assert.assertEquals(3, dataPoint.getFailures());
            } else {
                Assert.assertEquals(0, dataPoint.getTotal());
                Assert.assertEquals(0, dataPoint.getErrors());
                Assert.assertEquals(0, dataPoint.getFailures());
            }
        }
        
        from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-01T00:00:00Z");
        to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-03-01T00:00:00Z");
        stats = accessor.getResponseStats("TestOrg", "TestApi", "1.0", HistogramIntervalType.day, from, to);
        Assert.assertNotNull(stats);
        ResponseStatsDataPoint dataPoint = stats.getData().get(9);
        Assert.assertEquals(2, dataPoint.getTotal());
        Assert.assertEquals(0, dataPoint.getErrors());
        Assert.assertEquals(0, dataPoint.getFailures());
        dataPoint = stats.getData().get(10);
        Assert.assertEquals(1, dataPoint.getTotal());
        Assert.assertEquals(0, dataPoint.getErrors());
        Assert.assertEquals(0, dataPoint.getFailures());
        dataPoint = stats.getData().get(22);
        Assert.assertEquals(9, dataPoint.getTotal());
        Assert.assertEquals(1, dataPoint.getErrors());
        Assert.assertEquals(1, dataPoint.getFailures());
        dataPoint = stats.getData().get(23);
        Assert.assertEquals(6, dataPoint.getTotal());
        Assert.assertEquals(0, dataPoint.getErrors());
        Assert.assertEquals(2, dataPoint.getFailures());

        dataPoint = stats.getData().get(5);
        Assert.assertEquals(0, dataPoint.getTotal());
        Assert.assertEquals(0, dataPoint.getErrors());
        Assert.assertEquals(0, dataPoint.getFailures());
        dataPoint = stats.getData().get(19);
        Assert.assertEquals(0, dataPoint.getTotal());
        Assert.assertEquals(0, dataPoint.getErrors());
        Assert.assertEquals(0, dataPoint.getFailures());
    }

    /**
     * Test for {@link JdbcMetricsAccessor#getResponseStatsSummary(String, String, String, DateTime, DateTime)}
     */
    @Test
    public void testGetResponseStatsSummary() {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetricsAccessor accessor = new JdbcMetricsAccessor(config);

        DateTime from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-01T00:00:00Z");
        DateTime to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-03-01T00:00:00Z");
        ResponseStatsSummaryBean stats = accessor.getResponseStatsSummary("TestOrg", "TestApi", "1.0", from, to);
        Assert.assertEquals(18, stats.getTotal());
        Assert.assertEquals(3, stats.getFailures());
        Assert.assertEquals(1, stats.getErrors());
    }

    /**
     * Test for {@link JdbcMetricsAccessor#getResponseStatsPerClient(String, String, String, DateTime, DateTime)}
     */
    @Test
    public void testGetResponseStatsPerClient() {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetricsAccessor accessor = new JdbcMetricsAccessor(config);

        DateTime from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-01T00:00:00Z");
        DateTime to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-03-01T00:00:00Z");
        ResponseStatsPerClientBean stats = accessor.getResponseStatsPerClient("TestOrg", "TestApi", "1.0", from, to);
        ResponseStatsDataPoint dataPoint = stats.getData().get("TestClient");
        Assert.assertNotNull(dataPoint);
        Assert.assertEquals(16, dataPoint.getTotal());
        Assert.assertEquals(3, dataPoint.getFailures());
        Assert.assertEquals(1, dataPoint.getErrors());

        dataPoint = stats.getData().get("OtherClient");
        Assert.assertNotNull(dataPoint);
        Assert.assertEquals(2, dataPoint.getTotal());
        Assert.assertEquals(0, dataPoint.getFailures());
        Assert.assertEquals(0, dataPoint.getErrors());
    }

    /**
     * Test for {@link JdbcMetricsAccessor#getResponseStatsPerPlan(String, String, String, DateTime, DateTime)}
     */
    @Test
    public void testGetResponseStatsPerPlan() {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetricsAccessor accessor = new JdbcMetricsAccessor(config);

        DateTime from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-01T00:00:00Z");
        DateTime to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-03-01T00:00:00Z");
        ResponseStatsPerPlanBean stats = accessor.getResponseStatsPerPlan("TestOrg", "TestApi", "1.0", from, to);
        ResponseStatsDataPoint dataPoint = stats.getData().get("Gold");
        Assert.assertNotNull(dataPoint);
        Assert.assertEquals(8, dataPoint.getTotal());
        Assert.assertEquals(1, dataPoint.getFailures());
        Assert.assertEquals(1, dataPoint.getErrors());

        dataPoint = stats.getData().get("Silver");
        Assert.assertNotNull(dataPoint);
        Assert.assertEquals(10, dataPoint.getTotal());
        Assert.assertEquals(2, dataPoint.getFailures());
        Assert.assertEquals(0, dataPoint.getErrors());
    }

    /**
     * Test for {@link JdbcMetricsAccessor#getClientUsagePerApi(String, String, String, DateTime, DateTime)}
     */
    @Test
    public void testGetClientUsagePerApi() {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetricsAccessor accessor = new JdbcMetricsAccessor(config);

        DateTime from = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-02-01T00:00:00Z");
        DateTime to = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime("2016-03-01T00:00:00Z");
        
        ClientUsagePerApiBean usage = accessor.getClientUsagePerApi("TestOrg", "TestClient", "1.0", from, to);
        Map<String, Long> expectedData = new HashMap<>();
        expectedData.put("TestApi", 15L);
        expectedData.put("OtherApi", 4L);
        Assert.assertEquals(expectedData, usage.getData());
    }

    /**
     * Creates an in-memory datasource.
     * @throws SQLException
     */
    private static BasicDataSource createInMemoryDatasource() throws Exception {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Connection connection = ds.getConnection();
        connection.setAutoCommit(true);
        initDB(connection);
        connection.close();
        return ds;
    }

    /**
     * Initialize the DB with the apiman gateway DDL.
     * @param connection
     */
    private static void initDB(Connection connection) throws Exception {
        ClassLoader cl = JdbcMetricsAccessorTest.class.getClassLoader();
        URL resource = cl.getResource("ddls/apiman-gateway_h2.ddl");
        try (InputStream is = resource.openStream()) {
            System.out.println("=======================================");
            System.out.println("Initializing database.");
            DdlParser ddlParser = new DdlParser();
            List<String> statements = ddlParser.parse(is);
            for (String sql : statements){
                System.out.println(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
            }
            System.out.println("=======================================");
        }
        
        System.out.println("--------------------------------------");
        System.out.println("Adding test data to the database.");
        resource = cl.getResource("JdbcMetricsAccessorTest/bulk-data.ddl");
        try (InputStream is = resource.openStream()) {
            DdlParser ddlParser = new DdlParser();
            List<String> statements = ddlParser.parse(is);
            for (String sql : statements){
                System.out.println(sql);
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
            }
        }
        System.out.println("--------------------------------------");
    }
    
}
