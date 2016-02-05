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
package io.apiman.gateway.engine.metrics;

import io.apiman.gateway.engine.IMetrics;

import java.io.Serializable;
import java.util.Date;

/**
 * Metric information about a single request processed by the API Gateway.
 * This information is then reported to the {@link IMetrics} subsystem.
 *
 * @author eric.wittmann@redhat.com
 */
public class RequestMetric implements Serializable {

    private static final long serialVersionUID = 7085676761317470403L;

    private Date requestStart;
    private Date requestEnd;
    private long requestDuration = -1;
    private Date apiStart;
    private Date apiEnd;
    private long apiDuration = 1;
    private String url;
    private String resource;
    private String method;
    private String apiOrgId;
    private String apiId;
    private String apiVersion;
    private String planId;
    private String clientOrgId;
    private String clientId;
    private String clientVersion;
    private String contractId;
    private String user;
    private int responseCode;
    private String responseMessage;
    private boolean failure;
    private int failureCode;
    private String failureReason;
    private boolean error;
    private String errorMessage;
    private long bytesUploaded;
    private long bytesDownloaded;

    /**
     * Constructor.
     */
    public RequestMetric() {
    }

    /**
     * @return the apiDuration
     */
    public long getApiDuration() {
        return apiDuration;
    }

    /**
     * @param apiDuration the apiDuration to set
     */
    public void setApiDuration(long apiDuration) {
        this.apiDuration = apiDuration;
    }

    /**
     * @return the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the apiOrgId
     */
    public String getApiOrgId() {
        return apiOrgId;
    }

    /**
     * @param apiOrgId the apiOrgId to set
     */
    public void setApiOrgId(String apiOrgId) {
        this.apiOrgId = apiOrgId;
    }

    /**
     * @return the apiId
     */
    public String getApiId() {
        return apiId;
    }

    /**
     * @param apiId the apiId to set
     */
    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    /**
     * @return the apiVersion
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * @param apiVersion the apiVersion to set
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * @return the clientOrgId
     */
    public String getClientOrgId() {
        return clientOrgId;
    }

    /**
     * @param clientOrgId the clientOrgId to set
     */
    public void setClientOrgId(String clientOrgId) {
        this.clientOrgId = clientOrgId;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the clientVersion
     */
    public String getClientVersion() {
        return clientVersion;
    }

    /**
     * @param clientVersion the clientVersion to set
     */
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    /**
     * @return the contractId
     */
    public String getContractId() {
        return contractId;
    }

    /**
     * @param contractId the contractId to set
     */
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    /**
     * @return the responseCode
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * @param responseCode the responseCode to set
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * @return the responseMessage
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * @param responseMessage the responseMessage to set
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * @return the failureCode
     */
    public int getFailureCode() {
        return failureCode;
    }

    /**
     * @param failureCode the failureCode to set
     */
    public void setFailureCode(int failureCode) {
        this.failureCode = failureCode;
    }

    /**
     * @return the failureReason
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * @param failureReason the failureReason to set
     */
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    /**
     * @return the requestStart
     */
    public Date getRequestStart() {
        return requestStart;
    }

    /**
     * @param requestStart the requestStart to set
     */
    public void setRequestStart(Date requestStart) {
        this.requestStart = requestStart;
    }

    /**
     * @return the requestEnd
     */
    public Date getRequestEnd() {
        return requestEnd;
    }

    /**
     * @param requestEnd the requestEnd to set
     */
    public void setRequestEnd(Date requestEnd) {
        this.requestEnd = requestEnd;
        this.requestDuration = requestEnd.getTime() - requestStart.getTime();
    }

    /**
     * @return the requestDuration
     */
    public long getRequestDuration() {
        return requestDuration;
    }

    /**
     * @param requestDuration the requestDuration to set
     */
    public void setRequestDuration(long requestDuration) {
        this.requestDuration = requestDuration;
    }

    /**
     * @return the apiStart
     */
    public Date getApiStart() {
        return apiStart;
    }

    /**
     * @param apiStart the apiStart to set
     */
    public void setApiStart(Date apiStart) {
        this.apiStart = apiStart;
    }

    /**
     * @return the apiEnd
     */
    public Date getApiEnd() {
        return apiEnd;
    }

    /**
     * @param apiEnd the apiEnd to set
     */
    public void setApiEnd(Date apiEnd) {
        this.apiEnd = apiEnd;
        this.apiDuration = apiEnd.getTime() - apiStart.getTime();
    }

    /**
     * @return the error
     */
    public boolean isError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the failure
     */
    public boolean isFailure() {
        return failure;
    }

    /**
     * @param failure the failure to set
     */
    public void setFailure(boolean failure) {
        this.failure = failure;
    }

    /**
     * @return the planId
     */
    public String getPlanId() {
        return planId;
    }

    /**
     * @param planId the planId to set
     */
    public void setPlanId(String planId) {
        this.planId = planId;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the bytesUploaded
     */
    public long getBytesUploaded() {
        return bytesUploaded;
    }

    /**
     * @param bytesUploaded the bytesUploaded to set
     */
    public void setBytesUploaded(long bytesUploaded) {
        this.bytesUploaded = bytesUploaded;
    }

    /**
     * @return the bytesDownloaded
     */
    public long getBytesDownloaded() {
        return bytesDownloaded;
    }

    /**
     * @param bytesDownloaded the bytesDownloaded to set
     */
    public void setBytesDownloaded(long bytesDownloaded) {
        this.bytesDownloaded = bytesDownloaded;
    }

}
