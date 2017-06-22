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

package io.apiman.plugins.auth3scale.authrep.strategies.impl;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.threescale.beans.Auth3ScaleBean;
import io.apiman.plugins.auth3scale.authrep.PrincipalStrategyFactory;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class StandardStrategyFactory implements PrincipalStrategyFactory {

    private final StandardAuthCache authCache = new StandardAuthCache();

    @Override
    public StandardAuth getAuthPrincipal(Auth3ScaleBean bean,
            ApiRequest request,
            IPolicyContext context) {
        return new StandardAuth(bean, request, context, authCache);
    }

    @Override
    public StandardRep getRepPrincipal(Auth3ScaleBean bean,
            ApiRequest request,
            ApiResponse response,
            IPolicyContext context) {
        return new StandardRep(bean, request, response, context, authCache);
    }

}
