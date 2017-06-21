/*
 * Copyright 2017 JBoss Inc
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

import io.apiman.gateway.engine.vertx.polling.ThreeScaleURILoadingRegistry;
import io.apiman.plugins.auth3scale.authrep.AuthRepConstants;

import java.net.URI;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ReporterOptions {

    public static final int DEFAULT_LIST_CAPAC = 800;
    public static final int DEFAULT_FULL_TRIGGER = 500;
    public static final int DEFAULT_BUCKET_MAX_SIZE = 1000;
    public static final URI DEFAULT_REPORT_ENDPOINT = URI.create(ThreeScaleURILoadingRegistry.DEFAULT_BACKEND + AuthRepConstants.REPORT_PATH);

    private int initialBucketCapacity = DEFAULT_LIST_CAPAC;
    private int bucketMaxSize = DEFAULT_BUCKET_MAX_SIZE;
    private int bucketFullTriggerSize = DEFAULT_FULL_TRIGGER;
    private URI reportEndpoint = DEFAULT_REPORT_ENDPOINT;

    public int getInitialBucketCapacity() {
        return initialBucketCapacity;
    }

    public ReporterOptions setInitialBucketCapacity(int initialBucketCapacity) {
        this.initialBucketCapacity = initialBucketCapacity;
        return this;
    }

    public int getBucketMaxSize() {
        return bucketMaxSize;
    }

    public ReporterOptions setBucketMaxSize(int bucketMaxSize) {
        this.bucketMaxSize = bucketMaxSize;
        return this;
    }

    public int getBucketFullTriggerSize() {
        return bucketFullTriggerSize;
    }

    public ReporterOptions setBucketFullTriggerSize(int bucketFullTriggerSize) {
        this.bucketFullTriggerSize = bucketFullTriggerSize;
        return this;
    }

    public URI getReportEndpoint() {
        return reportEndpoint;
    }

    public ReporterOptions setReportEndpoint(URI reportEndpoint) {
        this.reportEndpoint = reportEndpoint;
        return this;
    }

}
