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
package io.apiman.gateway.engine.jdbc;

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Extends the {@link JdbcRegistry} to provide multi-node caching.  This caching solution
 * will work in a cluster, although it is a rather naive implementation.  The approach
 * taken is that whenever the data in the DB is modified, a "last modified" record is 
 * inserted/updated.  The registry utilizes a thread to periodically poll the DB to
 * check if this data has been changed.  If the data *has* been changed, then the cache
 * is invalidated.
 *
 * @author eric.wittmann@redhat.com
 */
public class PollCachingJdbcRegistry extends CachingJdbcRegistry {

    private static final int DEFAULT_POLLING_INTERVAL = 10;
    private static final int DEFAULT_STARTUP_DELAY = 30;

    private int pollIntervalMillis;
    private int startupDelayMillis;

    private boolean polling = false;
    private long dataVersion = -1;

    /**
     * Constructor.
     */
    public PollCachingJdbcRegistry(Map<String, String> config) {
        super(config);
        
        String intervalVal = config.get("cache-polling-interval"); //$NON-NLS-1$
        String startupVal = config.get("cache-polling-startup-delay"); //$NON-NLS-1$

        if (intervalVal != null) {
            pollIntervalMillis = new Integer(intervalVal) * 1000;
        } else {
            pollIntervalMillis = DEFAULT_POLLING_INTERVAL * 1000;
        }

        if (startupVal != null) {
            startupDelayMillis = new Integer(startupVal) * 1000;
        } else {
            startupDelayMillis = DEFAULT_STARTUP_DELAY * 1000;
        }

        startCacheInvalidator();
    }

    /**
     * @see io.apiman.gateway.engine.CachingJdbcRegistry.CachingESRegistry#publishApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishApi(Api api, final IAsyncResultHandler<Void> handler) {
        super.publishApi(api, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    updateDataVersion();
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.CachingJdbcRegistry.CachingESRegistry#retireApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireApi(Api api, final IAsyncResultHandler<Void> handler) {
        super.retireApi(api, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    updateDataVersion();
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.CachingJdbcRegistry.CachingESRegistry#registerClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerClient(Client client, final IAsyncResultHandler<Void> handler) {
        super.registerClient(client, new IAsyncResultHandler<Void>() {
            /**
             * @see io.apiman.gateway.engine.async.IAsyncHandler#handle(java.lang.Object)
             */
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    updateDataVersion();
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.CachingJdbcRegistry.CachingESRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterClient(Client client, final IAsyncResultHandler<Void> handler) {
        super.unregisterClient(client, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    updateDataVersion();
                }
                handler.handle(result);
            }
        });
    }

    /**
     * Stores a "dataversion" record in the ES store.  There is only a single one of these.  The
     * return value of the add will include the version number of the entity.  This version
     * number is what we use to determine whether our cache is stale.
     */
    protected void updateDataVersion() {
        Connection conn = null;
        try {
            long newVersion = System.currentTimeMillis();

            conn = ds.getConnection();
            conn.setAutoCommit(false);
            QueryRunner run = new QueryRunner();

            run.update(conn, "DELETE FROM gw_dataversion"); //$NON-NLS-1$
            run.update(conn, "INSERT INTO gw_dataversion (version) VALUES (?)",  //$NON-NLS-1$
                    newVersion);

            DbUtils.commitAndClose(conn);
            dataVersion = newVersion;
        } catch (SQLException e) {
            dataVersion = -1;
        }
    }

    /**
     * Starts up a thread that polls the ES store for updates.
     */
    protected void startCacheInvalidator() {
        polling = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Wait on startup before starting to poll.
                try { Thread.sleep(startupDelayMillis); } catch (InterruptedException e1) { e1.printStackTrace(); }

                while (polling) {
                    try { Thread.sleep(pollIntervalMillis); } catch (Exception e) { e.printStackTrace(); }
                    checkCacheVersion();
                }
            }
        }, "JdbcRegistryCacheInvalidator"); //$NON-NLS-1$
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Stop polling.
     */
    public void stop() {
        polling = false;
    }

    /**
     * Checks the ES store to see if the 'dataVersion' entry has been updated with a newer
     * version #.  If it has, then we need to invalidate our cache.
     */
    protected void checkCacheVersion() {
        // Be very aggressive in invalidating the cache.
        boolean invalidate = true;
        QueryRunner run = new QueryRunner(ds);
        try {
            long latestVersion = run.query("SELECT version FROM gw_dataversion", Handlers.LONG_HANDLER); //$NON-NLS-1$
            if (latestVersion > -1 && dataVersion > -1 && latestVersion == dataVersion) {
                invalidate = false;
            } else {
                dataVersion = latestVersion;
            }
        } catch (SQLException e) {
            // TODO need to use the gateway logger to log this!
            e.printStackTrace();
        }
        if (invalidate) {
            invalidateCache();
        }
    }

    private static final class Handlers {
        public static final ResultSetHandler<Long> LONG_HANDLER = (ResultSet rs) -> {
            if (!rs.next()) {
                return -1L;
            }
            return rs.getLong(1);
        };
    }
    
}
