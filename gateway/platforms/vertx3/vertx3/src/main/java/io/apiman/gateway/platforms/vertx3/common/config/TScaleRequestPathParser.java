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

import io.apiman.common.util.ApimanPathUtils.ApiRequestPathInfo;
import io.apiman.gateway.engine.IApiRequestPathParser;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.vertx.core.Vertx;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("nls")
public class TScaleRequestPathParser implements IApiRequestPathParser {

    public TScaleRequestPathParser(Vertx vertx, Map<String, String> config) {
    }

    @Override
    public ApiRequestPathInfo parseEndpoint(String path, HeaderMap headers) {
        String[] split = StringUtils.split(path, "/", 3);

        if (split == null || split.length < 2 || !"services".equalsIgnoreCase(split[0])) {
            throw new IllegalArgumentException("Invalid path format, expected /service/serviceName");
        }

        ApiRequestPathInfo parsed = new ApiRequestPathInfo();
        parsed.orgId="DEFAULT";
        parsed.apiVersion="DEFAULT";
        parsed.apiId=split[1];
        parsed.resource=split[2];
        return parsed;
    }

}
