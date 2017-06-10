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
package io.apiman.plugins.auth3scale.authrep.apikey;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;
import io.apiman.plugins.auth3scale.authrep.ICachingAuthenticator;

import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ApiKeyAuthCache implements ICachingAuthenticator {
    @Override
    public boolean isAuthCached(Content config, ApiRequest serviceRequest, Object... elems) {
        try {
            return lruCache.get(getCacheKey(serviceRequest.getApiId(), elems[0],
                    hashArray(config, serviceRequest)), () -> false); // TODO cache routematcher result into request?
        } catch (ExecutionException e) {
            throw new UncheckedExecutionException(e);
        }
    }

    @Override
    public ApiKeyAuthCache cache(Content config, ApiRequest serviceRequest, Object... elems) {
        lruCache.put(getCacheKey(serviceRequest.getApiId(),  elems[0],
                hashArray(config, serviceRequest)), true);
        return this;
    }

    @Override
    public ApiKeyAuthCache invalidate(Content config, ApiRequest serviceRequest, Object... elems) {
        if (elems.length == 0 || elems[0] == null || elems[0].equals("")) {
            System.out.println("Bad"); //$NON-NLS-1$
        }
        lruCache.invalidate(getCacheKey(serviceRequest.getApiId(),  elems[0],
                hashArray(config, serviceRequest))); // TODO optimise
        return this;
    }
}
