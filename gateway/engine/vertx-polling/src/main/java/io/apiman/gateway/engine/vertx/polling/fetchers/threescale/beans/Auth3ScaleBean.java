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
package io.apiman.gateway.engine.vertx.polling.fetchers.threescale.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Does nothing (yet).
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Auth3ScaleBean {
    @JsonProperty("3scaleConfig")
    private ProxyConfigRoot threescaleConfig;
    private String defaultOrg;
    private String defaultVersion;

    public ProxyConfigRoot getThreescaleConfig() {
        return threescaleConfig;
    }

    public Auth3ScaleBean setThreescaleConfig(ProxyConfigRoot threescaleConfig) {
        this.threescaleConfig = threescaleConfig;
        return this;
    }

    public String getDefaultOrg() {
        return defaultOrg;
    }

    public Auth3ScaleBean setDefaultOrg(String defaultOrgName) {
        this.defaultOrg = defaultOrgName;
        return this;
    }

    public String getDefaultVersion() {
        return defaultVersion;
    }

    public Auth3ScaleBean setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;
        return this;
    }
}
