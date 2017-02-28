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

package io.apiman.gateway.platforms.vertx3.common.config;

import io.apiman.common.util.ApimanPathUtils;
import io.apiman.common.util.ApimanPathUtils.ApiRequestPathInfo;
import io.apiman.gateway.engine.IApiRequestPathParser;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.vertx.core.Vertx;

import java.util.Map;

public class DefaultApiRequestFactoryImpl implements IApiRequestPathParser {

    public DefaultApiRequestFactoryImpl(Vertx vertx, Map<String, String> config) {
    }

    @Override
    public ApiRequestPathInfo parseEndpoint(String path, HeaderMap headers) {
        ApiRequestPathInfo parsedPath = ApimanPathUtils.parseApiRequestPath(
                headers.get(ApimanPathUtils.X_API_VERSION_HEADER),
                headers.get(ApimanPathUtils.ACCEPT_HEADER),
                path);

        if (parsedPath.orgId == null)
            throw new IllegalArgumentException(String.format("Invalid endpoint provided: %s", path)); //$NON-NLS-1$

        return parsedPath;
    }

}
