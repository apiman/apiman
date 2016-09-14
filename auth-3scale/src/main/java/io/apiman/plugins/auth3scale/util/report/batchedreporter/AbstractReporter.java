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
package io.apiman.plugins.auth3scale.util.report.batchedreporter;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.plugins.auth3scale.util.ParameterMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 * @param <T> extends ReportData
 */
public abstract class AbstractReporter<T extends ReportData> {
    private IAsyncHandler<Void> fullHandler;

    protected final Map<Integer, ConcurrentLinkedQueue<T>> reports = new ConcurrentHashMap<>(); // TODO LRU?
    protected static final int DEFAULT_LIST_CAPAC = 800;
    protected static final int FULL_TRIGGER_CAPAC = 500;
    protected static final int MAX_RECORDS = 1000;

    public AbstractReporter() {
    }

    public abstract List<ReportToSend> encode();

    public abstract AbstractReporter<T> addRecord(T record);

    public AbstractReporter<T> setFullHandler(IAsyncHandler<Void> fullHandler) {
        this.fullHandler = fullHandler;
        return this;
    }

    protected void full() {
        fullHandler.handle((Void) null);
    }

    protected <Value> ParameterMap setIfNotNull(ParameterMap in, String k, Value v) {
        if (v == null)
            return in;
        in.add(k, v);
        return in;
    }
}
