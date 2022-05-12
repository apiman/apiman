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
package io.apiman.gateway.engine.policies.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration object for the IP list policies.
 *
 * @author eric.wittmann@redhat.com
 */
public class IPListConfig {

    private volatile int hashCode = 0;
    private final String httpHeader;
    private final Set<String> ipList;
    private final int responseCode;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public IPListConfig(@JsonProperty("httpHeader") String httpHeader,
                        @JsonProperty("ipList") Set<String> ipList,
                        @JsonProperty("responseCode") int responseCode) {
        this.httpHeader = httpHeader;
        if (ipList != null) {
            this.ipList = Collections.unmodifiableSet(ipList);
        } else {
            this.ipList = Collections.emptySet();
        }
        this.responseCode = responseCode;
        this.hashCode();
    }

    /**
     * @return the ipList
     */
    public Set<String> getIpList() {
        return ipList;
    }

    /**
     * @return the httpHeader
     */
    public String getHttpHeader() {
        return httpHeader;
    }
    /**
     * @return the responseCode
     */
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPListConfig that = (IPListConfig) o;
        return responseCode == that.responseCode && Objects.equals(httpHeader, that.httpHeader) && Objects.equals(ipList, that.ipList);
    }

    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = Objects.hash(httpHeader, ipList, responseCode);
        }
        return this.hashCode;
    }
}
