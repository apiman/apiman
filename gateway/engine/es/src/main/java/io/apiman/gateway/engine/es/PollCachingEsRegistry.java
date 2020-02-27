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

import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.logging.DefaultDelegateFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Extends the {@link EsRegistry} to provide multi-node caching.  This caching solution
 * will work in a cluster, although it is a rather naive implementation.  The approach
 * taken is that whenever the ES index is modified, a "last modified" record is set in
 * elasticsearch.  The registry utilizes a thread to periodically poll the ES store to
 * check if the data has been changed.  If the data *has* been changed, then the cache
 * is invalidated.
 *
 * @author eric.wittmann@redhat.com
 */
public class PollCachingEsRegistry extends CachingEsRegistry {

    private static final int DEFAULT_POLLING_INTERVAL = 10;
    private static final int DEFAULT_STARTUP_DELAY = 30;

    private int pollIntervalMillis;
    private int startupDelayMillis;

    private boolean polling = false;
    private String dataVersion = null;

    private IApimanLogger logger = new DefaultDelegateFactory().createLogger(PollCachingEsRegistry.class);

    /**
     * Constructor.
     * @param config
     */
    public PollCachingEsRegistry(Map<String, String> config) {
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
     * @see CachingEsRegistry#publishApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
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
     * @see CachingEsRegistry#retireApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
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
     * @see CachingEsRegistry#registerClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
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
     * @see CachingEsRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
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
        ActionListener listener = new ActionListener() {
            @Override
            public void onResponse(Object o) {
                dataVersion = null;
            }

            @Override
            public void onFailure(Exception e) {
                dataVersion = null;
            }
        };

        IndexRequest request = new IndexRequest(getIndexPrefix() + EsConstants.INDEX_DATA_VERSION);
        request.id("instance"); //$NON-NLS-1$
        request.source("updatedOn", System.currentTimeMillis()); //$NON-NLS-1$
        getClient().indexAsync(request, RequestOptions.DEFAULT, listener);
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
                    try {
                        Thread.sleep(pollIntervalMillis);
                        checkCacheVersion();
                    } catch (Exception e) {
                        logger.warn(e.getMessage());
                    }

                }
            }
        }, "EsRegistryCacheInvalidator"); //$NON-NLS-1$
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
    protected void checkCacheVersion() throws IOException {
        // Be very aggressive in invalidating the cache.
        boolean invalidate = true;
        GetResponse result = getClient().get(new GetRequest(getIndexPrefix() + EsConstants.INDEX_DATA_VERSION, "instance"), RequestOptions.DEFAULT);
        if (result.isExists()) {
            String latestDV = Long.toString(result.getVersion()) ; //$NON-NLS-1$
            if (latestDV != null && dataVersion != null && latestDV.equals(dataVersion)) {
                invalidate = false;
            } else {
                dataVersion = latestDV;
            }
        }
        if (invalidate) {
            invalidateCache();
        }
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.GATEWAY_INDEX_NAME;
    }

    /**
     * @see AbstractEsComponent#getDefaultIndices()
     * @return default indices
     */
    @Override
    protected List<String> getDefaultIndices() {
        String[] indices = {EsConstants.INDEX_DATA_VERSION};
        return Arrays.asList(indices);
    }
}
