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
 * Thrown when trying to get a download.
 *
 * @author eric.wittmann@redhat.com
 */
public class DownloadNotFoundException extends AbstractNotFoundException {

    private static final long serialVersionUID = -6071114119976971441L;

    /**
     * Constructor.
     */
    public DownloadNotFoundException() {
    }
    
    /**
     * Constructor.
     * @param message the exception message
     */ 
    public DownloadNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param cause the exception cause
     */
    public DownloadNotFoundException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructor.
     * @param message the exception message
     * @param cause the exception cause
     */
    public DownloadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.DOWNLOAD_NOT_FOUND;
    }
    
    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.DOWNLOAD_NOT_FOUND_INFO;
    }

}
