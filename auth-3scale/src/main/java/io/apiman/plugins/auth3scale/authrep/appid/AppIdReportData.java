/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.plugins.auth3scale.authrep.appid;

import static io.apiman.plugins.auth3scale.Auth3ScaleConstants.APP_ID;
import static io.apiman.plugins.auth3scale.Auth3ScaleConstants.APP_KEY;
import static io.apiman.plugins.auth3scale.Auth3ScaleConstants.LOG;
import static io.apiman.plugins.auth3scale.Auth3ScaleConstants.REFERRER;
import static io.apiman.plugins.auth3scale.Auth3ScaleConstants.SERVICE_ID;
import static io.apiman.plugins.auth3scale.Auth3ScaleConstants.SERVICE_TOKEN;
import static io.apiman.plugins.auth3scale.Auth3ScaleConstants.TIMESTAMP;
import static io.apiman.plugins.auth3scale.Auth3ScaleConstants.USAGE;
import static io.apiman.plugins.auth3scale.Auth3ScaleConstants.USER_ID;
import static io.apiman.plugins.auth3scale.util.Auth3ScaleUtils.setIfNotNull;

import io.apiman.plugins.auth3scale.util.ParameterMap;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.ReportData;

import java.net.URI;
import java.util.Objects;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class AppIdReportData implements ReportData {

    private URI endpoint;
    private String serviceToken;
    private String serviceId;
    private String timestamp;
    private String appId;
    private String userId;
    private ParameterMap usage;
    private ParameterMap log;
    private String referrer;
    private String appKey;

    public URI getEndpoint() {
        return endpoint;
    }

    public AppIdReportData setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    @Override
    public String getServiceToken() {
        return serviceToken;
    }

    public AppIdReportData setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
        return this;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    public AppIdReportData setServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public AppIdReportData setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public AppIdReportData setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public ParameterMap getUsage() {
        return usage;
    }

    public AppIdReportData setUsage(ParameterMap usage) {
        this.usage = usage;
        return this;
    }

    @Override
    public ParameterMap getLog() {
        return log;
    }

    public AppIdReportData setLog(ParameterMap log) {
        this.log = log;
        return this;
    }

    @Override
    public int bucketId() {
        return hashCode();
    }

    private String getReferrer() {
        return referrer;
    }

    public AppIdReportData setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public AppIdReportData setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    @Override
    public int hashCode() { //TODO
        return Objects.hash(endpoint, serviceToken, serviceId, appId);
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "AppIdReportData [endpoint=" + endpoint + ", serviceToken=" + serviceToken + ", serviceId="
                + serviceId + ", appId=" + appId + ", userId=" + userId + ", timestamp=" + timestamp
                + ", usage=" + usage + ", log=" + log + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AppIdReportData other = (AppIdReportData) obj;
        if (endpoint == null) {
            if (other.endpoint != null)
                return false;
        } else if (!endpoint.equals(other.endpoint))
            return false;
        if (serviceId == null) {
            if (other.serviceId != null)
                return false;
        } else if (!serviceId.equals(other.serviceId))
            return false;
        if (serviceToken == null) {
            if (other.serviceToken != null)
                return false;
        } else if (!serviceToken.equals(other.serviceToken))
            return false;
        return true;
    }

    @Override
    public ParameterMap toParameterMap() {
      ParameterMap paramMap = new ParameterMap();
      paramMap.add(APP_ID, getAppId());
      paramMap.add(APP_KEY, getAppKey());
      paramMap.add(SERVICE_TOKEN, getServiceToken());
      paramMap.add(SERVICE_ID, getServiceId());
      paramMap.add(USAGE, getUsage());

      setIfNotNull(paramMap, TIMESTAMP, getTimestamp());
      setIfNotNull(paramMap, LOG, getLog());
      setIfNotNull(paramMap, REFERRER, getReferrer());
      setIfNotNull(paramMap, USER_ID, getUserId());
      return paramMap;
    }

    public String getAppKey() {
        return appKey;
    }

    public AppIdReportData setAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    @Override
    public String encode() {
        return toParameterMap().encode();
    }

}
