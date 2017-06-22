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

package io.apiman.plugins.auth3scale.authrep.appid;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.threescale.beans.BackendConfiguration;
import io.apiman.plugins.auth3scale.util.Auth3ScaleUtils;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class AppIdUtils {

    public static String getAppKey(BackendConfiguration config, ApiRequest request) {
        return Auth3ScaleUtils.getCredentialFromQueryOrHeader(config, request, config.getProxy().getAuthAppKey());
    }

    public static String getAppId(BackendConfiguration config, ApiRequest request) {
        return Auth3ScaleUtils.getCredentialFromQueryOrHeader(config, request, config.getProxy().getAuthAppId());
    }

}
