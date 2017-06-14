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

package io.apiman.plugins.auth3scale.authrep.strategies;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.AuthRepConstants;
import io.apiman.plugins.auth3scale.authrep.IAuthStrategyFactory;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.BatchedReportData;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.BatchedReporter;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.Reporter;

public class BatchedStrategyFactory implements IAuthStrategyFactory {
    // move stuff here
    // is this safe for mixing multiple different types? probably not.
    // Maybe caller should sort that out (seems better)
    private Reporter<BatchedReportData> reporter = new Reporter<>(AuthRepConstants.REPORT_URI);
    private StandardAuthCache standardCache = new StandardAuthCache();
    private BatchedAuthCache heuristicCache = new BatchedAuthCache();

    public BatchedStrategyFactory(BatchedReporter batchedReporter) {
        reporter.flushHandler(result -> {
            BatchedReportData entry = result.getResult().get(0);
            // Invalidate standard cache to ensure next request goes through blocking authrep
            standardCache.invalidate(entry.getConfig(), entry.getRequest(), entry.getKeyElems());
            // Make cache entry in heuristic cache to force subsequent N entries to be blocking authrep
            // with the hope that the backend has caught up and we will catch the updated rate limiting status.
            heuristicCache.cache(entry.getConfig(), entry.getRequest(), entry.getKeyElems());
        });

        batchedReporter.addReporter(reporter);
    }

    @Override
    public BatchedAuth getAuthStrategy(Content config,
            ApiRequest request,
            IPolicyContext context) {
        return new BatchedAuth(config, request, context, standardCache, heuristicCache);
    }

    @Override
    public BatchedRep getRepStrategy(Content config,
            ApiRequest request,
            ApiResponse response,
            IPolicyContext context) {
        return new BatchedRep(config, request, response, context, reporter);
    }

}
