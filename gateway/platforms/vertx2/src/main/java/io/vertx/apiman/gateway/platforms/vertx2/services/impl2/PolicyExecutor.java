package io.vertx.apiman.gateway.platforms.vertx2.services.impl2;

import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class PolicyExecutor {

    private IngestorToPolicyImpl requestService;
    private PolicyToIngestorService replyProxy;
    private Handler<AsyncResult<IngestorToPolicyService>> resultHandler;

    public PolicyExecutor(IngestorToPolicyImpl requestService,
            PolicyToIngestorService replyProxy,
            Handler<AsyncResult<IngestorToPolicyService>> resultHandler) {

        this.requestService = requestService;
        this.replyProxy = replyProxy;
        this.resultHandler = resultHandler;
    }
}
