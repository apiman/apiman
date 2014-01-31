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

package org.overlord.apiman.dt.api.rest.exceptions;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Base class for all APIMan errors coming out of the REST layer.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class RestException extends RuntimeException {

    private static final long serialVersionUID = -2406210413693314452L;

    /**
     * Constructor.
     */
    public RestException() {
    }
    
    /**
     * Constructor.
     * @param message
     */
    public RestException(String message) {
        super(message);
    }
    
    /**
     * Constructor.
     * @param cause
     */
    public RestException(Throwable cause) {
        super(cause);
    }

}
