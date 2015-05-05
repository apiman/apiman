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

package io.apiman.manager.api.beans.exceptions;


/**
 * Simple error bean used to serialize error information to JSON when
 * responding to a REST call with an error.
 *
 * @author eric.wittmann@redhat.com
 */
public class ErrorBean {

    private String type;
    private int errorCode;
    private String message;
    private String moreInfoUrl;
    private String stacktrace;

    /**
     * Constructor.
     */
    public ErrorBean() {
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return the moreInfo
     */
    public String getMoreInfoUrl() {
        return moreInfoUrl;
    }

    /**
     * @param moreInfoUrl the moreInfo to set
     */
    public void setMoreInfoUrl(String moreInfoUrl) {
        this.moreInfoUrl = moreInfoUrl;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the stacktrace
     */
    public String getStacktrace() {
        return stacktrace;
    }

    /**
     * @param stacktrace the stacktrace to set
     */
    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "ErrorBean [type=" + type + ", errorCode=" + errorCode + ", message=" + message
                + ", moreInfoUrl=" + moreInfoUrl + ", stacktrace=" + stacktrace + "]";
    }

}
