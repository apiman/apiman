/*
 * Copyright 2014 JBoss Inc
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

package io.apiman.manager.api.rest.exceptions;


import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Thrown when trying to get a resource from a plugin.
 *
 * @author eric.wittmann@redhat.com
 */
@ApiResponse(responseCode = "404", description = "Plugin resource not found", useReturnTypeSchema = true)
public class PluginResourceNotFoundException extends AbstractNotFoundException {

    private static final long serialVersionUID = 2620584248905679316L;

    /**
     * Constructor.
     */
    public PluginResourceNotFoundException() {
    }

    /**
     * Constructor.
     * @param message the exception message
     */
    public PluginResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.PLUGIN_RESOURCE_NOT_FOUND;
    }

    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.PLUGIN_RESOURCE_NOT_FOUND_INFO;
    }

}
