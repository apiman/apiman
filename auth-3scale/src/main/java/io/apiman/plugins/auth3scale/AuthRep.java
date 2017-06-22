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
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.threescale.beans.Auth3ScaleBean;
import io.apiman.gateway.engine.threescale.beans.AuthTypeEnum;
import io.apiman.gateway.engine.threescale.beans.BackendConfiguration;
import io.apiman.gateway.engine.threescale.beans.RateLimitingStrategy;
import io.apiman.plugins.auth3scale.authrep.AuthPrincipal;
import io.apiman.plugins.auth3scale.authrep.AuthRepFactory;
import io.apiman.plugins.auth3scale.authrep.PrincipalStrategyFactory;
import io.apiman.plugins.auth3scale.authrep.RepPrincipal;
import io.apiman.plugins.auth3scale.authrep.apikey.ApiKeyAuthRepFactory;
import io.apiman.plugins.auth3scale.authrep.appid.AppIdAuthRepFactory;
import io.apiman.plugins.auth3scale.authrep.strategies.AuthStrategy;
import io.apiman.plugins.auth3scale.authrep.strategies.RepStrategy;
import io.apiman.plugins.auth3scale.authrep.strategies.impl.BatchedStrategyFactory;
import io.apiman.plugins.auth3scale.authrep.strategies.impl.StandardStrategyFactory;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.BatchedReporter;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.BatchedReporterOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class AuthRep {

    private Map<AuthTypeEnum, AuthRepFactory> authTypeFactory = new HashMap<>();
    private Map<RateLimitingStrategy, PrincipalStrategyFactory> principalFactoryMap = new HashMap<>();
    private BatchedReporter batchedReporter;

    public AuthRep(IPolicyContext context) {
        batchedReporter = new BatchedReporter()
                .start(context, new BatchedReporterOptions());

        // API Key
        ApiKeyAuthRepFactory apiKeyFactory = new ApiKeyAuthRepFactory();
        authTypeFactory.put(AuthTypeEnum.API_KEY, apiKeyFactory);

        // App Id
        AppIdAuthRepFactory appIdFactory = new AppIdAuthRepFactory();
        authTypeFactory.put(AuthTypeEnum.APP_ID, appIdFactory);

        // Add different RL strategies (bad name... TODO better name!).
        // Standard naive rate limiting.
        principalFactoryMap.put(RateLimitingStrategy.STANDARD, new StandardStrategyFactory());
        principalFactoryMap.put(RateLimitingStrategy.BATCHED_HYBRID, new BatchedStrategyFactory(batchedReporter));
    }

    public AuthPrincipal getAuth(Auth3ScaleBean config, ApiRequest request, IPolicyContext context) {
        BackendConfiguration backendConfig = getBackendConfig(config);
        AuthStrategy authStrategy = getAuthStrategy(config, request, context);
        return authTypeFactory.get(backendConfig.getAuthType())
                .createAuth(config, request, context, authStrategy);
    }

    public RepPrincipal getRep(Auth3ScaleBean config, ApiResponse response, ApiRequest request, IPolicyContext context) {
        BackendConfiguration contentConfig = config.getThreescaleConfig().getProxyConfig().getBackendConfig();
        RepStrategy repStrategy = getRepStrategy(config, request, response, context);
        return authTypeFactory.get(contentConfig.getAuthType())
                .createRep(config, response, request, context, repStrategy);
    }

    private BackendConfiguration getBackendConfig(Auth3ScaleBean config) {
        return config.getThreescaleConfig().getProxyConfig().getBackendConfig();
    }

    private AuthStrategy getAuthStrategy(Auth3ScaleBean config, ApiRequest request, IPolicyContext context) {
        return principalFactoryMap.get(config.getRateLimitingStrategy())
                .getAuthPrincipal(config, request, context);
    }

    private RepStrategy getRepStrategy(Auth3ScaleBean config, ApiRequest request, ApiResponse response, IPolicyContext context) {
        return principalFactoryMap.get(config.getRateLimitingStrategy())
                .getRepPrincipal(config, request, response, context);
    }
}
