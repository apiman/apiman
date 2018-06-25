/*
 * Copyright 2018 JBoss Inc
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

package io.apiman.plugins.demo.custompathparser;

import io.apiman.common.util.ApimanPathUtils.ApiRequestPathInfo;
import io.apiman.gateway.engine.IApiRequestPathParser;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.impl.DefaultRequestPathParser;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
/**
 * Example {@link IApiRequestPathParser} implementation.
 *
 * See {@link DefaultRequestPathParser} for the standard implementation.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class CustomPathParserImpl implements IApiRequestPathParser {
    private final String defaultOrgName;

    public CustomPathParserImpl(Map<String, String> config) {
       // Pass in the defaultOrgName from static config or just use "DefaultOrg".
       this.defaultOrgName = config.getOrDefault("defaultOrgName", "DefaultOrg");
    }

    @Override
    public ApiRequestPathInfo parseEndpoint(String path, HeaderMap headers) {
       String[] split = StringUtils.split(path, "/", 3);

       if (split == null || split.length < 2) {
          throw new IllegalArgumentException("Invalid path format, expected /apiId/apiVersion/<resource path>");
       }

       ApiRequestPathInfo parsed = new ApiRequestPathInfo();
       // Let's set the org name manually as our configured `defaultOrgName`
       parsed.orgId = defaultOrgName;
       parsed.apiId = split[0];
       parsed.apiVersion = split[1];
       if (split.length > 2) {
          parsed.resource = "/" + split[2];
       } else {
          parsed.resource = "/";
       }
       return parsed;
    }
}
