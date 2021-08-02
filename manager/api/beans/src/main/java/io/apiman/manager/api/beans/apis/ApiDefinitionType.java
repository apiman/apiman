/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.beans.apis;

import io.apiman.common.util.MediaType;

/**
 * The type of definition stored for the API.
 *
 * @author eric.wittmann@redhat.com
 */
public enum ApiDefinitionType {
    None(null, false, false),
    SwaggerJSON(MediaType.APPLICATION_JSON, false, true),
    SwaggerYAML("application/x-yaml", false, true),
    WSDL("text/xml", false, true),
    WADL("application/vnd.sun.wadl+xml", false, true),
    RAML("application/x-yaml", false, true),
    External("", true, true);

    private final String mediaType;
    private final boolean isExternal;
    private final boolean isDefined;

    ApiDefinitionType(String mediaType, boolean isExternal, boolean isDefined) {
        this.mediaType = mediaType;
        this.isExternal = isExternal;
        this.isDefined = isDefined;
    }

    public String getMediaType() {
        return mediaType;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public boolean isDefined() {
        return isDefined;
    }
}
