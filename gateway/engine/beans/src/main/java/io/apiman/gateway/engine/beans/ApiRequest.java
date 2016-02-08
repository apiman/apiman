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
package io.apiman.gateway.engine.beans;

import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.beans.util.QueryMap;

import java.io.Serializable;

/**
 * An inbound request for a managed API.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiRequest implements IApiObject, Serializable {

    private static final long serialVersionUID = 8024669261165845962L;

    private String apiKey;
    private transient ApiContract contract;
    private transient Api api;
    private String type;
    private String url;
    private String destination;
    private QueryMap queryParams = new QueryMap();
    private HeaderMap headers = new HeaderMap();
    private String remoteAddr;
    private Object rawRequest;
    private boolean transportSecurity = false;

    /*
     * Optional fields - set these if you want the apiman engine to
     * validate that the apikey is valid for the given API coords.
     */
    private String apiOrgId;
    private String apiId;
    private String apiVersion;

    /**
     * Constructor.
     */
    public ApiRequest() {
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the rawRequest
     */
    public Object getRawRequest() {
        return rawRequest;
    }

    /**
     * @param rawRequest the rawRequest to set
     */
    public void setRawRequest(Object rawRequest) {
        this.rawRequest = rawRequest;
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
     * @see io.apiman.gateway.engine.beans.IApiObject#getHeaders()
     */
    @Override
    public HeaderMap getHeaders() {
        return headers;
    }

    /**
     * @see io.apiman.gateway.engine.beans.IApiObject#setHeaders(HeaderMap)
     */
    @Override
    public void setHeaders(HeaderMap headers) {
        this.headers = headers;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @return the remoteAddr
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * @param remoteAddr the remoteAddr to set
     */
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    /**
     * @return the contract
     */
    public ApiContract getContract() {
        return contract;
    }

    /**
     * @param contract the contract to set
     */
    public void setContract(ApiContract contract) {
        this.contract = contract;
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
     * @return the queryParams
     */
    public QueryMap getQueryParams() {
        return queryParams;
    }

    /**
     * @param queryParams the queryParams to set
     */
    public void setQueryParams(QueryMap queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * Indicates whether api request or response was made with transport security.
     *
     * @return true if transport is secure; else false.
     */
    public boolean isTransportSecure() {
        return transportSecurity;
    }

    /**
     * Set whether api request/response was made with transport security.
     *
     * @param isSecure transport security status
     */
    public void setTransportSecure(boolean isSecure) {
        this.transportSecurity = isSecure;
    }

    /**
     * @return the api
     */
    public Api getApi() {
        return api;
    }

    /**
     * @param api the api to set
     */
    public void setApi(Api api) {
        this.api = api;
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
}
