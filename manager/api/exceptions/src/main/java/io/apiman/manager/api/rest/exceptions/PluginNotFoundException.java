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


/**
 * Thrown when trying to get, update, or delete a plugin that does not exist.
 *
 * @author eric.wittmann@redhat.com
 */
public class PluginNotFoundException extends AbstractNotFoundException {

    private static final long serialVersionUID = 4877430226993586299L;

    /**
     * Constructor.
     */
    public PluginNotFoundException() {
    }
    
    /**
     * Constructor.
     * @param message the exception message
     */ 
    public PluginNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param cause the exception cause
     */
    public PluginNotFoundException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructor.
     * @param message the exception message
     * @param cause the exception cause
     */
    public PluginNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.PLUGIN_NOT_FOUND;
    }
    
    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.PLUGIN_NOT_FOUND_INFO;
    }

}
