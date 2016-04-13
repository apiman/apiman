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

package io.apiman.gateway.engine.jdbc;

import io.apiman.common.util.ddl.DdlParser;
import io.apiman.gateway.engine.metrics.RequestMetric;
import io.apiman.test.common.util.TestUtil;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class JdbcMetricsTest {

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
    
    @Before
    public void reset() throws SQLException {
        QueryRunner run = new QueryRunner(ds);
        run.update("DELETE FROM gw_requests");
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.jdbc.JdbcMetrics#record(io.apiman.gateway.engine.metrics.RequestMetric)}.
     */
    @Test
    public void testRecord() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetrics metrics = new JdbcMetrics(config);
        metrics.record(request(
                "2016-02-10T09:30:00Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        
        Thread.sleep(200);
        assertRowCount(1, "SELECT * FROM gw_requests WHERE api_org_id = ?", "TestOrg");
        metrics.stop();
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.jdbc.JdbcMetrics#record(io.apiman.gateway.engine.metrics.RequestMetric)}.
     */
    @Test
    public void testRecords() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetrics metrics = new JdbcMetrics(config);
        metrics.record(request(
                "2016-02-10T09:30:00Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        metrics.record(request(
                "2016-02-10T09:31:00Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        metrics.record(request(
                "2016-02-10T09:32:00Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        
        Thread.sleep(200);
        assertRowCount(3, "SELECT * FROM gw_requests WHERE api_org_id = ?", "TestOrg");
        metrics.stop();
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.jdbc.JdbcMetrics#record(io.apiman.gateway.engine.metrics.RequestMetric)}.
     */
    @Test
    public void testAggregationByTime() throws Exception {
        Map<String, String> config = new HashMap<>();
        config.put("datasource.jndi-location", DB_JNDI_LOC);
        JdbcMetrics metrics = new JdbcMetrics(config);
        metrics.record(request(
                "2016-02-10T09:30:10Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        metrics.record(request(
                "2016-02-10T09:30:15Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        metrics.record(request(
                "2016-02-10T09:30:21Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        metrics.record(request(
                "2016-02-10T09:17:21Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        metrics.record(request(
                "2016-02-10T09:17:22Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        metrics.record(request(
                "2016-01-17T12:31:00Z", 300, "http://localhost:8080/test/1", "/test/1", 
                "GET", "TestOrg", "TestApi", "1.0", "Gold", 
                "TestOrg", "TestClient", "1.0", "12345", "user1",
                200, "OK", false, 0, null, false, null, 0, 1024));
        
        Thread.sleep(200);
        // aggregate per minute
        assertRowCount(3, "SELECT count(*) FROM gw_requests WHERE api_org_id = ? GROUP BY minute", "TestOrg");
        // aggregate per hour
        assertRowCount(2, "SELECT count(*) FROM gw_requests WHERE api_org_id = ? GROUP BY hour", "TestOrg");
        metrics.stop();
    }
    
    /**
     * Asserts the row count of the given query.
     * @param count
     * @param query
     * @param params
     * @throws SQLException 
     */
    private void assertRowCount(int count, String query, Object ... params) throws SQLException {
        QueryRunner run = new QueryRunner(ds);
        int actualCount = run.query(query, COUNT_HANDLER, params);
        Assert.assertEquals(count, actualCount);
    }

    /**
     * @throws ParseException 
     */
    private RequestMetric request(String requestStart, long requestDuration, String url, String resource,
            String method, String apiOrgId, String apiId, String apiVersion, String planId,
            String clientOrgId, String clientId, String clientVersion, String contractId, String user,
            int responseCode, String responseMessage, boolean failure, int failureCode, String failureReason,
            boolean error, String errorMessage, long bytesUploaded, long bytesDownloaded) throws ParseException {
        Date start = ISO8601Utils.parse(requestStart, new ParsePosition(0));
        RequestMetric rval = new RequestMetric();
        rval.setRequestStart(start);
        rval.setRequestEnd(new Date(start.getTime() + requestDuration));
        rval.setApiStart(start);
        rval.setApiEnd(rval.getRequestEnd());
        rval.setApiDuration(requestDuration);
        rval.setUrl(url);
        rval.setResource(resource);
        rval.setMethod(method);
        rval.setApiOrgId(apiOrgId);
        rval.setApiId(apiId);
        rval.setApiVersion(apiVersion);
        rval.setPlanId(planId);
        rval.setClientOrgId(clientOrgId);
        rval.setClientId(clientId);
        rval.setClientVersion(clientVersion);
        rval.setContractId(contractId);
        rval.setUser(user);
        rval.setResponseCode(responseCode);
        rval.setResponseMessage(responseMessage);
        rval.setFailure(failure);
        rval.setFailureCode(failureCode);
        rval.setFailureReason(failureReason);
        rval.setError(error);
        rval.setErrorMessage(errorMessage);
        rval.setBytesUploaded(bytesUploaded);
        rval.setBytesDownloaded(bytesDownloaded);
        return rval;
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
        ClassLoader cl = JdbcMetricsTest.class.getClassLoader();
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
    }
    
    private static ResultSetHandler<Integer> COUNT_HANDLER = new ResultSetHandler<Integer>() {

        @Override
        public Integer handle(ResultSet rs) throws SQLException {
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
            }
            return rowCount;
        }
        
    };

}
