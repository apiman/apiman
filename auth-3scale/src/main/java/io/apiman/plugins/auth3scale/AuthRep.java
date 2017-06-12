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
package io.apiman.plugins.auth3scale;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPeriodicComponent;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.AuthTypeEnum;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.RateLimitingStrategy;
import io.apiman.plugins.auth3scale.authrep.AbstractAuth;
import io.apiman.plugins.auth3scale.authrep.AbstractRep;
import io.apiman.plugins.auth3scale.authrep.AuthRepFactory;
import io.apiman.plugins.auth3scale.authrep.IAuthStrategyFactory;
import io.apiman.plugins.auth3scale.authrep.StandardStrategyFactory;
import io.apiman.plugins.auth3scale.authrep.apikey.ApiKeyAuthRepFactory;
import io.apiman.plugins.auth3scale.authrep.appid.AppIdAuthRepFactory;
import io.apiman.plugins.auth3scale.ratelimit.IAuth;
import io.apiman.plugins.auth3scale.ratelimit.IRep;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.BatchedReporter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class AuthRep {
    private Map<AuthTypeEnum, AuthRepFactory> authTypeFactory = new HashMap<>();
    private Map<RateLimitingStrategy, IAuthStrategyFactory> strategyFactoryMap = new HashMap<>();

    private BatchedReporter batchedReporter; // TODO simplifly this by making public static as everyone wants to share it anyway?
    private volatile boolean reporterInitialised = false;


    public AuthRep(BatchedReporter batchedReporter) {
        this.batchedReporter = batchedReporter;

        // API Key
        ApiKeyAuthRepFactory apiKeyFactory = new ApiKeyAuthRepFactory();
        authTypeFactory.put(AuthTypeEnum.API_KEY, apiKeyFactory);
//        batchedReporter.addReporter(apiKeyFactory.getReporter());

        // App Id
        AppIdAuthRepFactory appIdFactory = new AppIdAuthRepFactory();
        authTypeFactory.put(AuthTypeEnum.APP_ID, appIdFactory);
//        batchedReporter.addReporter(appIdFactory.getReporter());

        // Add different RL strategies (bad name... TODO better name!).
        // Standard naive rate limiting.
        strategyFactoryMap.put(RateLimitingStrategy.STANDARD, new StandardStrategyFactory());

//                .addReporter(appIdFactory.getReporter())
//                .addReporter(oauthFactory.getReporter());
    }

    public IAuth getAuth(Content config, ApiRequest request, IPolicyContext context) {
        AbstractAuth<?> authStrategy = getAuthStrategy(config, request, context);
        return authTypeFactory.get(config.getAuthType())
                .createAuth(config, request, context, authStrategy);
    }

    private AbstractAuth<?> getAuthStrategy(Content config, ApiRequest request, IPolicyContext context) {
        return strategyFactoryMap.get(RateLimitingStrategy.STANDARD)
                .getAuthStrategy(config, request, context); // TODO
    }

    public IRep getRep(Content config, ApiResponse response, ApiRequest request, IPolicyContext context) {
        safeInitialise(context);
        AbstractRep<?> repStrategy = getRepStrategy(config, request, response, context);
        return authTypeFactory.get(config.getAuthType())
                .createRep(config, response, request, context, repStrategy);
    }

    private AbstractRep<?> getRepStrategy(Content config, ApiRequest request, ApiResponse response, IPolicyContext context) {
        return strategyFactoryMap.get(RateLimitingStrategy.STANDARD)
                .getRepStrategy(config, request, response, context); // TODO
    }

    // TODO Could convert to component to avoid DCL.
    private void safeInitialise(IPolicyContext context) {
        if (!reporterInitialised) {
            synchronized (this) {
                if (!reporterInitialised) {
                    batchedReporter.start(context.getComponent(IPeriodicComponent.class), context.getComponent(IHttpClientComponent.class));
                    reporterInitialised = true;
                }
            }
        }
    }
}
