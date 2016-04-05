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

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.metrics.RequestMetric;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.dbutils.QueryRunner;

/**
 * A JDBC implementation of the gateway registry.  Only suitable for a 
 * synchronous environment - should not be used when running an async 
 * Gateway (e.g. vert.x).
 * 
 * Must be configured with the JNDI location of the datasource to use.
 * Example:
 * 
 *     apiman-gateway.registry=io.apiman.gateway.engine.jdbc.JdbcRegistry
 *     apiman-gateway.registry.datasource.jndi-location=java:jboss/datasources/apiman-gateway
 * 
 * @author ewittman
 */
public class JdbcMetrics extends AbstractJdbcComponent implements IMetrics {

    private static final int DEFAULT_QUEUE_SIZE = 10000;

    protected IComponentRegistry componentRegistry;
    protected final BlockingQueue<RequestMetric> queue;
    
    private boolean stopped;
    private Thread thread;

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public JdbcMetrics(Map<String, String> config) {
        super(config);

        int queueSize = DEFAULT_QUEUE_SIZE;
        String queueSizeConfig = config.get("queue.size"); //$NON-NLS-1$
        if (queueSizeConfig != null) {
            queueSize = new Integer(queueSizeConfig);
        }
        queue = new LinkedBlockingDeque<>(queueSize);
        startConsumerThread();
    }

    /**
     * Starts a thread which will serially pull information off the blocking
     * queue and submit that information to hawkular metrics.
     */
    private void startConsumerThread() {
        stopped = false;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopped) {
                    processQueue();
                }
            }
        }, "JdbcMetricsConsumer"); //$NON-NLS-1$
        thread.setDaemon(true);
        thread.start();        
    }

    /**
     * Process the next item in the queue.
     */
    @SuppressWarnings("nls")
    protected void processQueue() {
        try {
            RequestMetric metric = queue.take();
            QueryRunner run = new QueryRunner(ds);

            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            cal.setTime(metric.getRequestStart());
            
            long rstart = cal.getTimeInMillis();
            long rend = metric.getRequestEnd().getTime();
            long duration = metric.getRequestDuration();
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            long minute = cal.getTimeInMillis();
            cal.set(Calendar.MINUTE, 0);
            long hour = cal.getTimeInMillis();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            long day = cal.getTimeInMillis();
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            long week = cal.getTimeInMillis();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            long month = cal.getTimeInMillis();
            String api_org_id = metric.getApiOrgId();
            String api_id = metric.getApiId();
            String api_version = metric.getApiVersion();
            String client_org_id = metric.getClientOrgId();
            String client_id = metric.getClientId();
            String client_version = metric.getClientVersion();
            String plan = metric.getPlanId();
            String user_id = metric.getUser();
            String rtype = null;
            if (metric.isFailure()) {
                rtype = "failure";
            } else if (metric.isError()) {
                rtype = "error";
            }
            long bytes_up = metric.getBytesUploaded();
            long bytes_down = metric.getBytesDownloaded();

            // Now insert a row for the metric.
            run.update("INSERT INTO requests ("
                    + "rstart, rend, duration, month, week, day, hour, minute, "
                    + "api_org_id, api_id, api_version, "
                    + "client_org_id, client_id, client_version, plan, "
                    + "user_id, resp_type, bytes_up, bytes_down) VALUES ("
                    + "?, ?, ?, ?, ?, ?, ?, ?,"
                    + "?, ?, ?,"
                    + "?, ?, ?, ?,"
                    + "?, ?, ?, ?)",
                    rstart, rend, duration, month, week, day, hour, minute,
                    api_org_id, api_id, api_version,
                    client_org_id, client_id, client_version, plan,
                    user_id, rtype, bytes_up, bytes_down
                    );
        } catch (InterruptedException ie) {
            // This means that the thread was stopped.
        } catch (Exception e) {
            // TODO better logging of this unlikely error
            System.err.println("Error adding metric to database:"); //$NON-NLS-1$
            e.printStackTrace();
            return;
        }
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#record(io.apiman.gateway.engine.metrics.RequestMetric)
     */
    @Override
    public void record(RequestMetric metric) {
        try {
            queue.put(metric);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see io.apiman.gateway.engine.IMetrics#setComponentRegistry(io.apiman.gateway.engine.IComponentRegistry)
     */
    @Override
    public void setComponentRegistry(IComponentRegistry registry) {
        this.componentRegistry = registry;
    }
    
    /**
     * Called to stop the consumer thread (used for testing only).
     */
    protected void stop() {
        stopped = true;
        thread.interrupt();
    }

}
