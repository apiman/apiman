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

package io.apiman.gateway.engine;

import io.apiman.common.util.ApimanPathUtils.ApiRequestPathInfo;
import io.apiman.gateway.engine.beans.util.HeaderMap;

/**
 * <p>
 * Parse an inbound request's path and headers, and return {@link ApiRequestPathInfo} for use
 * by the engine to determine how to route the request.
 * </p>
 *
 * <p>
 * In essence, this is the identification of information that the gateway needs so that it can
 * be distinguished.
 * </p>
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public interface IApiRequestPathParser {

    /**
     * Parse an inbound API Request's endpoint.
     *
     * @param path the inbound path
     * @param headers the request's headers
     * @return parsed request path info
     */
    ApiRequestPathInfo parseEndpoint(String path, HeaderMap headers);
}
