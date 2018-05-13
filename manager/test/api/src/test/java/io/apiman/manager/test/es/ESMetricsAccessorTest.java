/*
 * Copyright 2013 JBoss Inc
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

package io.apiman.manager.test.es;

import io.apiman.gateway.engine.es.DefaultESClientFactory;
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
import io.apiman.manager.api.core.metrics.AbstractMetricsAccessor;
import io.apiman.manager.api.es.ESMetricsAccessor;
import io.searchbox.client.JestClient;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.Flush;
import io.searchbox.indices.Refresh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the elasticsearch metrics accessor.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class ESMetricsAccessorTest {

    //private static Node node;
    private static JestClient client;
    private static Locale locale;

    @BeforeClass @Ignore
    public static void setup() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        // Delete, refresh and create new client
        client = createJestClient();
        client.execute(new DeleteIndex.Builder("apiman_metrics").build());
        client.execute(new Flush.Builder().force().build());
        DefaultESClientFactory.clearClientCache();
        // Because of the delete above, the metrics fields need reinitialising with the index
        // mappings otherwise everything will screw up. See apiman_metrics-settings.json
        client = createJestClient();

        // Load test
        loadTestData();

        client.execute(new Flush.Builder().force().build());
        DefaultESClientFactory.clearClientCache();
    }

    private static JestClient createJestClient() {
        Map<String, String> config = new HashMap<>();
        config.put("client.protocol", "http");
        config.put("client.host", "localhost");
        config.put("client.port", "19250");
        config.put("client.initialize", "true");
        return new DefaultESClientFactory().createClient(config, "apiman_metrics");
    }

    private static void loadTestData() throws Exception {
        String url = "http://localhost:19250/_bulk";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        InputStream is = ESMetricsAccessorTest.class.getResourceAsStream("bulk-metrics-data.txt");
        IOUtils.copy(is, os);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);
        if (conn.getResponseCode() > 299) {
            IOUtils.copy(conn.getInputStream(), System.err);
            throw new IOException("Bulk load of data failed with: " + conn.getResponseMessage());
        }
    }

    @AfterClass
    public static void teardown() throws Exception {
        System.out.println("----------- All done.");
        Locale.setDefault(locale);
    }

    @Before
    public void before() throws IOException {
        client.execute(new Refresh.Builder().addIndex("apiman_metrics").build());
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.ESMetricsAccessor#getUsage(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.metrics.HistogramIntervalType, java.util.Date, java.util.Date)}.
     */
    @Test
    public void testGetUsage() throws Exception {
        ESMetricsAccessor metrics = new ESMetricsAccessor();
        metrics.setEsClient(client);

        UsageHistogramBean usage = metrics.getUsage("JBossOverlord", "s-ramp-api", "1.0", HistogramIntervalType.day,
                parseDate("2015-01-01"), new DateTime().withZone(DateTimeZone.UTC));
        List<UsageDataPoint> data = usage.getData();
        Assert.assertTrue(!data.isEmpty());
        Assert.assertTrue(data.size() > 1);
        Assert.assertEquals("2015-06-19T00:00:00.000Z", usage.getData().get(169).getLabel());
        Assert.assertEquals(46L, usage.getData().get(169).getCount());


        usage = metrics.getUsage("JBossOverlord", "s-ramp-api", "1.0", HistogramIntervalType.hour,
                parseDate("2015-06-15"), parseDate("2015-06-22"));
        data = usage.getData();
        Assert.assertEquals(168, data.size());
        Assert.assertEquals("2015-06-19T15:00:00.000Z", usage.getData().get(111).getLabel());
        Assert.assertEquals(46L, usage.getData().get(111).getCount());


        usage = metrics.getUsage("JBossOverlord", "s-ramp-api", "1.0", HistogramIntervalType.minute,
                parseDate("2015-06-19"), parseDate("2015-06-20"));
        data = usage.getData();
        Assert.assertEquals(1440, data.size());
        Assert.assertEquals("2015-06-19T15:13:00.000Z", usage.getData().get(913).getLabel());
        Assert.assertEquals("2015-06-19T15:14:00.000Z", usage.getData().get(914).getLabel());
        Assert.assertEquals("2015-06-19T15:15:00.000Z", usage.getData().get(915).getLabel());
        Assert.assertEquals(14L, usage.getData().get(913).getCount());
        Assert.assertEquals(15L, usage.getData().get(914).getCount());
        Assert.assertEquals(17L, usage.getData().get(915).getCount());
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.ESMetricsAccessor#getUsagePerClient(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)}.
     */
    @Test
    public void testGetUsagePerClient() throws Exception {
        ESMetricsAccessor metrics = new ESMetricsAccessor();
        metrics.setEsClient(client);

        // data exists - all data for JBossOverlord/s-ramp-api:1.0
        UsagePerClientBean usagePerClient = metrics.getUsagePerClient("JBossOverlord", "s-ramp-api", "1.0",
                parseDate("2015-01-01"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertNotNull(usagePerClient);
        Map<String, Long> expectedData = new HashMap<>();
        expectedData.put("dtgov", 29L);
        expectedData.put("rtgov", 14L);
        Assert.assertEquals(expectedData, usagePerClient.getData());

        // data exists - all data for Test/echo:1.0
        usagePerClient = metrics.getUsagePerClient("Test", "echo", "1.0",
                parseDate("2015-01-01"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertNotNull(usagePerClient);
        expectedData.clear();
        expectedData.put("my-client", 136L);
        expectedData.put("client1", 78L);
        Assert.assertEquals(expectedData, usagePerClient.getData());

        // Test/echo:1.0 bounded by a different date range
        usagePerClient = metrics.getUsagePerClient("Test", "echo", "1.0",
                parseDate("2015-06-18"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertNotNull(usagePerClient);
        expectedData.clear();
        Assert.assertEquals(expectedData, usagePerClient.getData());

        // data exists - all data for Test/echo:1.0
        usagePerClient = metrics.getUsagePerClient("Test", "echo", "1.0",
                parseDate("2015-06-01"),
                parseDate("2015-06-17"));
        Assert.assertNotNull(usagePerClient);
        expectedData.clear();
        expectedData.put("my-client", 136L);
        expectedData.put("client1", 78L);
        Assert.assertEquals(expectedData, usagePerClient.getData());

        // No data for API
        usagePerClient = metrics.getUsagePerClient("NA", "NA", "NA", parseDate("2015-01-01"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertNotNull(usagePerClient);
        expectedData.clear();
        Assert.assertEquals(expectedData, usagePerClient.getData());
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.ESMetricsAccessor#getUsagePerPlan(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)}.
     */
    @Test
    public void testGetUsagePerPlan() throws Exception {
        ESMetricsAccessor metrics = new ESMetricsAccessor();
        metrics.setEsClient(client);

        // data exists - all data for JBossOverlord/s-ramp-api:1.0
        UsagePerPlanBean usagePerPlan = metrics.getUsagePerPlan("JBossOverlord", "s-ramp-api", "1.0",
                parseDate("2015-01-01"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertNotNull(usagePerPlan);
        Map<String, Long> expectedData = new HashMap<>();
        expectedData.put("Platinum", 17L);
        expectedData.put("Silver", 16L);
        expectedData.put("Gold", 12L);
        Assert.assertEquals(expectedData, usagePerPlan.getData());

        // data exists - all data for Test/echo:1.0
        usagePerPlan = metrics.getUsagePerPlan("Test", "echo", "1.0",
                parseDate("2015-01-01"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertNotNull(usagePerPlan);
        expectedData.clear();
        expectedData.put("Platinum", 31L);
        expectedData.put("Silver", 73L);
        expectedData.put("Gold", 67L);
        expectedData.put("Bronze", 43L);
        Assert.assertEquals(expectedData, usagePerPlan.getData());

        // Test/echo:1.0 bounded by a different date range
        usagePerPlan = metrics.getUsagePerPlan("Test", "echo", "1.0",
                parseDate("2015-06-18"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertNotNull(usagePerPlan);
        expectedData.clear();
        Assert.assertEquals(expectedData, usagePerPlan.getData());

        // data exists - all data for Test/echo:1.0 (alt timeframe)
        usagePerPlan = metrics.getUsagePerPlan("Test", "echo", "1.0",
                parseDate("2015-06-01"),
                parseDate("2015-06-17"));
        Assert.assertNotNull(usagePerPlan);
        expectedData.clear();
        expectedData.put("Platinum", 31L);
        expectedData.put("Silver", 73L);
        expectedData.put("Gold", 67L);
        expectedData.put("Bronze", 43L);
        Assert.assertEquals(expectedData, usagePerPlan.getData());

    }

    /**
     * Test method for {@link io.apiman.manager.api.es.ESMetricsAccessor#generateHistogramSkeleton(io.apiman.manager.api.beans.metrics.UsageHistogramBean, java.util.Date, java.util.Date, io.apiman.manager.api.beans.metrics.HistogramIntervalType)}.
     */
    @Test
    public void testGenerateHistogramSkeleton() throws Exception {
        DateTime from = parseDate("2015-01-01T00:00:00Z");
        DateTime to = parseDate("2015-01-10T00:00:00Z");
        UsageHistogramBean histogram = new UsageHistogramBean();
        Map<String, UsageDataPoint> index = AbstractMetricsAccessor.generateHistogramSkeleton(histogram, from, to,
                HistogramIntervalType.day, UsageDataPoint.class);
        Assert.assertEquals(9, index.size());
        Assert.assertEquals(9, histogram.getData().size());
        Assert.assertEquals("2015-01-01T00:00:00.000Z", histogram.getData().get(0).getLabel());
        Assert.assertEquals("2015-01-03T00:00:00.000Z", histogram.getData().get(2).getLabel());
        Assert.assertEquals("2015-01-07T00:00:00.000Z", histogram.getData().get(6).getLabel());


        from = parseDate("2015-01-01T00:00:00Z");
        to = parseDate("2015-01-03T00:00:00Z");
        histogram = new UsageHistogramBean();
        index = AbstractMetricsAccessor.generateHistogramSkeleton(histogram, from, to, HistogramIntervalType.hour,
                UsageDataPoint.class);
        Assert.assertEquals(48, index.size());
        Assert.assertEquals(48, histogram.getData().size());
        Assert.assertEquals("2015-01-01T00:00:00.000Z", histogram.getData().get(0).getLabel());
        Assert.assertEquals("2015-01-01T02:00:00.000Z", histogram.getData().get(2).getLabel());
        Assert.assertEquals("2015-01-01T06:00:00.000Z", histogram.getData().get(6).getLabel());
        Assert.assertEquals("2015-01-02T18:00:00.000Z", histogram.getData().get(42).getLabel());


        from = parseDate("2015-01-01");
        to = parseDate("2015-01-03");
        histogram = new UsageHistogramBean();
        index = AbstractMetricsAccessor.generateHistogramSkeleton(histogram, from, to, HistogramIntervalType.hour,
                UsageDataPoint.class);
        Assert.assertEquals(48, index.size());
        Assert.assertEquals(48, histogram.getData().size());
        Assert.assertEquals("2015-01-01T00:00:00.000Z", histogram.getData().get(0).getLabel());
        Assert.assertEquals("2015-01-01T02:00:00.000Z", histogram.getData().get(2).getLabel());
        Assert.assertEquals("2015-01-01T06:00:00.000Z", histogram.getData().get(6).getLabel());
        Assert.assertEquals("2015-01-02T18:00:00.000Z", histogram.getData().get(42).getLabel());


        from = parseDate("2015-01-01T00:00:00Z");
        to = parseDate("2015-01-02T00:00:00Z");
        histogram = new UsageHistogramBean();
        index = AbstractMetricsAccessor.generateHistogramSkeleton(histogram, from, to,
                HistogramIntervalType.minute, UsageDataPoint.class);
        Assert.assertEquals(1440, index.size());
        Assert.assertEquals(1440, histogram.getData().size());
        Assert.assertEquals("2015-01-01T00:00:00.000Z", histogram.getData().get(0).getLabel());
        Assert.assertEquals("2015-01-01T00:20:00.000Z", histogram.getData().get(20).getLabel());
        Assert.assertEquals("2015-01-01T00:30:00.000Z", histogram.getData().get(30).getLabel());


        from = parseDate("2015-01-01");
        to = parseDate("2015-12-31");
        histogram = new UsageHistogramBean();
        index = AbstractMetricsAccessor.generateHistogramSkeleton(histogram, from, to, HistogramIntervalType.month,
                UsageDataPoint.class);
        Assert.assertEquals(12, index.size());
        Assert.assertEquals(12, histogram.getData().size());
        Assert.assertEquals("2015-01-01T00:00:00.000Z", histogram.getData().get(0).getLabel());
        Assert.assertEquals("2015-06-01T00:00:00.000Z", histogram.getData().get(5).getLabel());

        System.out.println("--------------------------------");

        from = parseDate("2015-01-01");
        to = parseDate("2015-12-30");
        histogram = new UsageHistogramBean();
        index = AbstractMetricsAccessor.generateHistogramSkeleton(histogram, from, to, HistogramIntervalType.week,
                UsageDataPoint.class);

        Assert.assertEquals(53, index.size());
        Assert.assertEquals(53, histogram.getData().size());
        Assert.assertEquals("2014-12-28T00:00:00.000Z", histogram.getData().get(0).getLabel());
        Assert.assertEquals("2015-02-01T00:00:00.000Z", histogram.getData().get(5).getLabel());
        Assert.assertEquals("2015-04-12T00:00:00.000Z", histogram.getData().get(15).getLabel());
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.ESMetricsAccessor#getResponseStats(String, String, String, HistogramIntervalType, DateTime, DateTime)
     */
    @Test
    public void testGetResponseStats() throws Exception {
        ESMetricsAccessor metrics = new ESMetricsAccessor();
        metrics.setEsClient(client);

        // s-ramp-api data
        ResponseStatsHistogramBean stats = metrics.getResponseStats("JBossOverlord", "s-ramp-api", "1.0", HistogramIntervalType.day,
                parseDate("2015-06-01"), new DateTime().withZone(DateTimeZone.UTC));
        List<ResponseStatsDataPoint> data = stats.getData();
        Assert.assertTrue(!data.isEmpty());
        Assert.assertTrue(data.size() > 1);
        Assert.assertEquals("2015-06-19T00:00:00.000Z", stats.getData().get(18).getLabel());
        Assert.assertEquals(46L, stats.getData().get(18).getTotal());
        Assert.assertEquals(0L, stats.getData().get(18).getFailures());
        Assert.assertEquals(3L, stats.getData().get(18).getErrors());

        // test/echo data
        stats = metrics.getResponseStats("Test", "echo", "1.0", HistogramIntervalType.day,
                parseDate("2015-06-01"), new DateTime().withZone(DateTimeZone.UTC));
        data = stats.getData();
        Assert.assertTrue(!data.isEmpty());
        Assert.assertTrue(data.size() > 1);
        Assert.assertEquals("2015-06-16T00:00:00.000Z", stats.getData().get(15).getLabel());
        Assert.assertEquals(214L, stats.getData().get(15).getTotal());
        Assert.assertEquals(41L, stats.getData().get(15).getFailures());
        Assert.assertEquals(2L, stats.getData().get(15).getErrors());
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.ESMetricsAccessor#getResponseStatsSummary(String, String, String, DateTime, DateTime)
     */
    @Test
    public void testGetResponseStatsSummary() throws Exception {
        ESMetricsAccessor metrics = new ESMetricsAccessor();
        metrics.setEsClient(client);

        // s-ramp-api data
        ResponseStatsSummaryBean stats = metrics.getResponseStatsSummary("JBossOverlord", "s-ramp-api", "1.0",
                parseDate("2015-06-01"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertEquals(46L, stats.getTotal());
        Assert.assertEquals(3L, stats.getErrors());
        Assert.assertEquals(0L, stats.getFailures());

        // test/echo data
        stats = metrics.getResponseStatsSummary("Test", "echo", "1.0",
                parseDate("2015-06-01"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertEquals(214L, stats.getTotal());
        Assert.assertEquals(2L, stats.getErrors());
        Assert.assertEquals(41L, stats.getFailures());

    }

    /**
     * Test method for {@link io.apiman.manager.api.es.ESMetricsAccessor#getResponseStatsPerClient(String, String, String, DateTime, DateTime)
     */
    @Test
    public void testGetResponseStatsPerClient() throws Exception {
        ESMetricsAccessor metrics = new ESMetricsAccessor();
        metrics.setEsClient(client);

        // s-ramp-api data
        ResponseStatsPerClientBean stats = metrics.getResponseStatsPerClient("JBossOverlord", "s-ramp-api", "1.0",
                parseDate("2015-06-01"), new DateTime().withZone(DateTimeZone.UTC));
        Map<String, ResponseStatsDataPoint> data = stats.getData();
        Assert.assertEquals(2, data.size());
        ResponseStatsDataPoint point = data.get("dtgov");
        Assert.assertNotNull(point);
        Assert.assertEquals(29L, point.getTotal());
        Assert.assertEquals(0L, point.getFailures());
        Assert.assertEquals(0L, point.getErrors());
        point = data.get("rtgov");
        Assert.assertNotNull(point);
        Assert.assertEquals(14L, point.getTotal());
        Assert.assertEquals(0L, point.getFailures());
        Assert.assertEquals(3L, point.getErrors());

        // test/echo data
        stats = metrics.getResponseStatsPerClient("Test", "echo", "1.0",
                parseDate("2015-06-01"), new DateTime().withZone(DateTimeZone.UTC));
        data = stats.getData();
        Assert.assertEquals(2, data.size());
        point = data.get("client1");
        Assert.assertNotNull(point);
        Assert.assertEquals(78L, point.getTotal());
        Assert.assertEquals(19L, point.getFailures());
        Assert.assertEquals(1L, point.getErrors());
        point = data.get("my-client");
        Assert.assertNotNull(point);
        Assert.assertEquals(136L, point.getTotal());
        Assert.assertEquals(22L, point.getFailures());
        Assert.assertEquals(1L, point.getErrors());
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.ESMetricsAccessor#getResponseStatsPerPlan(String, String, String, DateTime, DateTime)
     */
    @Test
    public void testGetResponseStatsPerPlan() throws Exception {
        ESMetricsAccessor metrics = new ESMetricsAccessor();
        metrics.setEsClient(client);

        // s-ramp-api data
        ResponseStatsPerPlanBean stats = metrics.getResponseStatsPerPlan("JBossOverlord", "s-ramp-api", "1.0",
                parseDate("2015-06-01"), new DateTime().withZone(DateTimeZone.UTC));
        Map<String, ResponseStatsDataPoint> data = stats.getData();
        Assert.assertTrue(!data.isEmpty());
        ResponseStatsDataPoint point = data.get("Gold");
        Assert.assertNotNull(point);
        Assert.assertEquals(12L, point.getTotal());
        Assert.assertEquals(0L, point.getFailures());
        Assert.assertEquals(0L, point.getErrors());
        point = data.get("Silver");
        Assert.assertNotNull(point);
        Assert.assertEquals(16L, point.getTotal());
        Assert.assertEquals(0L, point.getFailures());
        Assert.assertEquals(3L, point.getErrors());
        point = data.get("Platinum");
        Assert.assertNotNull(point);
        Assert.assertEquals(17L, point.getTotal());
        Assert.assertEquals(0L, point.getFailures());
        Assert.assertEquals(0L, point.getErrors());

        // test/echo data
        stats = metrics.getResponseStatsPerPlan("Test", "echo", "1.0",
                parseDate("2015-06-01"), new DateTime().withZone(DateTimeZone.UTC));
        data = stats.getData();
        Assert.assertTrue(!data.isEmpty());
        point = data.get("Gold");
        Assert.assertNotNull(point);
        Assert.assertEquals(67L, point.getTotal());
        Assert.assertEquals(15L, point.getFailures());
        Assert.assertEquals(0L, point.getErrors());
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.ESMetricsAccessor#getClientUsagePerApi(String, String, String, DateTime, DateTime)
     */
    @Test
    public void testGetClientUsagePerApi() throws Exception {
        ESMetricsAccessor metrics = new ESMetricsAccessor();
        metrics.setEsClient(client);

        // data exists - all data for JBossOverlord/s-ramp-api:1.0
        ClientUsagePerApiBean usagePerApi = metrics.getClientUsagePerApi("JBossOverlord", "dtgov", "1.0",
                parseDate("2015-01-01"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertNotNull(usagePerApi);
        Map<String, Long> expectedData = new HashMap<>();
        expectedData.put("s-ramp-api", 29L);
        Assert.assertEquals(expectedData, usagePerApi.getData());

        usagePerApi = metrics.getClientUsagePerApi("JBossOverlord", "rtgov", "1.0",
                parseDate("2015-01-01"), new DateTime().withZone(DateTimeZone.UTC));
        Assert.assertNotNull(usagePerApi);
        expectedData = new HashMap<>();
        expectedData.put("s-ramp-api", 14L);
        Assert.assertEquals(expectedData, usagePerApi.getData());
    }

    /**
     * @param date
     * @throws ParseException
     */
    private DateTime parseDate(String date) throws ParseException {
        if (date.length() == 10) {
            return ISODateTimeFormat.date().withZone(DateTimeZone.UTC).parseDateTime(date);
        }
        return ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).parseDateTime(date);
    }

}
