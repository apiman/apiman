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
package io.apiman.plugins.auth3scale.authrep.apikey;

import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.LOG;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.REFERRER;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.SERVICE_ID;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.SERVICE_TOKEN;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USAGE;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USER_ID;
import static io.apiman.plugins.auth3scale.authrep.AuthRepConstants.USER_KEY;
import static io.apiman.plugins.auth3scale.util.IMetricsBuilder.setIfNotNull;

import io.apiman.plugins.auth3scale.util.ParameterMap;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.ReportData;

import java.net.URI;
import java.util.Objects;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ApiKeyReportData implements ReportData {
    private URI endpoint;
    private String serviceToken;
    private String userKey;
    private String serviceId;
    private String timestamp;
    private String userId;
    private ParameterMap usage;
    private ParameterMap log;
    private String referrer;

    public URI getEndpoint() {
        return endpoint;
    }

    public ApiKeyReportData setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public ApiKeyReportData setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
        return this;
    }

    public String getUserKey() {
        return userKey;
    }

    public ApiKeyReportData setUserKey(String userKey) {
        this.userKey = userKey;
        return this;
    }

    public String getServiceId() {
        return serviceId;
    }

    public ApiKeyReportData setServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ApiKeyReportData setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public ApiKeyReportData setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public ParameterMap getUsage() {
        return usage;
    }

    public ApiKeyReportData setUsage(ParameterMap usage) {
        this.usage = usage;
        return this;
    }

    @Override
    public ParameterMap getLog() {
        return log;
    }

    public ApiKeyReportData setLog(ParameterMap log) {
        this.log = log;
        return this;
    }

    @Override
    public int groupId() {
        return hashCode();
    }

    private String getReferrer() {
        return referrer;
    }

    public ApiKeyReportData setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    @Override
    public int hashCode() { // TODO
        return Objects.hash(endpoint, serviceToken, serviceId);
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "ApiKeyReportData [endpoint=" + endpoint + ", serviceToken=" + serviceToken + ", userKey="
                + userKey + ", serviceId=" + serviceId + ", timestamp=" + timestamp + ", userId=" + userId
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
        ApiKeyReportData other = (ApiKeyReportData) obj;
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
    public String encode() {
      ParameterMap paramMap = new ParameterMap();
      paramMap.add(USER_KEY, getUserKey());
      paramMap.add(SERVICE_TOKEN, getServiceToken());// maybe use endpoint properties or something. or new properties field.
      paramMap.add(SERVICE_ID, getServiceId());
      paramMap.add(USAGE, getUsage());
      paramMap.add(LOG, getLog());

      setIfNotNull(paramMap, REFERRER, getReferrer());
      setIfNotNull(paramMap, USER_ID, getUserId());
      return paramMap.encode();
    }
}
