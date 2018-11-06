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
package io.apiman.gateway.engine.es;

import io.apiman.common.logging.DefaultDelegateFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.es.beans.DataVersionBean;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Get;
import io.searchbox.core.Index;

import java.io.IOException;
import java.util.Map;

/**
 * Extends the {@link ESRegistry} to provide multi-node caching.  This caching solution
 * will work in a cluster, although it is a rather naive implementation.  The approach
 * taken is that whenever the ES index is modified, a "last modified" record is set in
 * elasticsearch.  The registry utilizes a thread to periodically poll the ES store to
 * check if the data has been changed.  If the data *has* been changed, then the cache
 * is invalidated.
 *
 * @author eric.wittmann@redhat.com
 */
public class PollCachingESRegistry extends CachingESRegistry {

    private static final int DEFAULT_POLLING_INTERVAL = 10;
    private static final int DEFAULT_STARTUP_DELAY = 30;

    private int pollIntervalMillis;
    private int startupDelayMillis;

    private boolean polling = false;
    private String dataVersion = null;

    private IApimanLogger logger = new DefaultDelegateFactory().createLogger(PollCachingESRegistry.class);

    /**
     * Constructor.
     * @param config
     */
    public PollCachingESRegistry(Map<String, String> config) {
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
     * @see io.apiman.gateway.engine.es.CachingESRegistry#publishApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
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
     * @see io.apiman.gateway.engine.es.CachingESRegistry#retireApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
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
     * @see io.apiman.gateway.engine.es.CachingESRegistry#registerClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
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
     * @see io.apiman.gateway.engine.es.CachingESRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
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
        DataVersionBean dv = new DataVersionBean();
        dv.setUpdatedOn(System.currentTimeMillis());
        Index index = new Index.Builder(dv).refresh(false)
                .index(getDefaultIndexName())
                .type("dataVersion").id("instance").build(); //$NON-NLS-1$ //$NON-NLS-2$
        getClient().executeAsync(index, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                dataVersion = null;
            }
            @Override
            public void failed(Exception e) {
                dataVersion = null;
            }
        });
    }

    /**
     * Starts up a thread that polls the ES store for updates.
     */
    protected void startCacheInvalidator() {
        polling = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Wait for 30s on startup before starting to poll.
                try { Thread.sleep(startupDelayMillis); } catch (InterruptedException e1) { e1.printStackTrace(); }

                while (polling) {
                    try { Thread.sleep(pollIntervalMillis); } catch (Exception e) { e.printStackTrace(); }
                    checkCacheVersion();
                }
            }
        }, "ESRegistryCacheInvalidator"); //$NON-NLS-1$
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
        try {
            Get get = new Get.Builder(getDefaultIndexName(), "instance").type("dataVersion").build(); //$NON-NLS-1$ //$NON-NLS-2$
            JestResult result = getClient().execute(get);
            if (result.isSucceeded()) {
                String latestDV = result.getJsonObject().get("_version").getAsString(); //$NON-NLS-1$
                if (latestDV != null && dataVersion != null && latestDV.equals(dataVersion)) {
                    invalidate = false;
                } else {
                    dataVersion = latestDV;
                }
            }
        } catch (IOException e) {
            logger.warn("Elasticsearch is not available, using cache");
            invalidate = false;
        }
        if (invalidate) {
            invalidateCache();
        }
    }

}
