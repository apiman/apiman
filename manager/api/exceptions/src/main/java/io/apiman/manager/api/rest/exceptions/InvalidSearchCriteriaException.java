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
 * Thrown when the search criteria is not valid (when invoking any of the 
 * various search methods).
 *
 * @author eric.wittmann@redhat.com
 */
public class InvalidSearchCriteriaException extends AbstractInvalidInputException {

    private static final long serialVersionUID = -166126446625739289L;
    
    /**
     * Constructor.
     * 
     * @param message the exception message
     */
    public InvalidSearchCriteriaException(String message) {
        super(message);
    }
    
    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.SEARCH_CRITERIA_INVALID;
    }
    
    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.SEARCH_CRITERIA_INVALID_INFO;
    }

}
