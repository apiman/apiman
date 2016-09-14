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
package io.apiman.plugins.auth3scale.authrep;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans.Content;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public interface IdentityFromContext {
    default String getIdentityElement(Content config, ApiRequest request, String canonicalName)  {
        // Manual for now as there's no mapping in the config.
        if (config.getProxy().getCredentialsLocation().equalsIgnoreCase("query")) {
            return request.getQueryParams().get(getElemFromConfig(config, canonicalName));
        } else { // Else let's assume header
            return request.getHeaders().get(getElemFromConfig(config, canonicalName));
        }
    }

    default String getElemFromConfig(Content config, String canonicalName) {
        switch (config.getAuthType()) {
        case API_KEY:
            return config.getProxy().getAuthUserKey();
        case APP_ID:
            if (AuthRepConstants.APP_ID.equalsIgnoreCase(canonicalName))
                return config.getProxy().getAuthAppId();
            if (AuthRepConstants.APP_KEY.equalsIgnoreCase(canonicalName))
                return config.getProxy().getAuthAppKey();
        case OAUTH: // TODO
            return null;
        }
        throw new IllegalStateException(String.format("Unrecognised auth identifier elements for %s with %s", config.getAuthType(), canonicalName));
    }

}
