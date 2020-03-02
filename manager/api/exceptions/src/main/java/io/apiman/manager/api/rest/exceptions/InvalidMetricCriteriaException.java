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
 * Thrown when the metric criteria is not valid.  For example, when asking
 * for an invalid date range, or a date range and interval combination that
 * would result in too many items.
 *
 * @author eric.wittmann@redhat.com
 */
public class InvalidMetricCriteriaException extends AbstractInvalidInputException {

    private static final long serialVersionUID = 1398262976721863828L;

    /**
     * Constructor.
     *
     * @param message the exception message
     */
    public InvalidMetricCriteriaException(String message) {
        super(message);
    }

    /**
     * @see AbstractRestException#getErrorCode()
     */
    @Override
    public int getErrorCode() {
        return ErrorCodes.METRIC_CRITERIA_INVALID;
    }

    /**
     * @see AbstractRestException#getMoreInfoUrl()
     */
    @Override
    public String getMoreInfoUrl() {
        return ErrorCodes.METRIC_CRITERIA_INVALID_INFO;
    }

}
