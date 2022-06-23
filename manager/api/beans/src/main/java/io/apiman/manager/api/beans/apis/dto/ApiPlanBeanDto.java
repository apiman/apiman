/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.beans.apis.dto;

import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

/**
 * An API Plan is an API Version plus a Plan Version.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiPlanBeanDto {
    @NotBlank
    private String planId;
    @NotBlank
    private String version;
    private Boolean requiresApproval;
    private DiscoverabilityLevel discoverability;
    public ApiPlanBeanDto() {
    }

    public String getPlanId() {
        return planId;
    }

    public ApiPlanBeanDto setPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ApiPlanBeanDto setVersion(String version) {
        this.version = version;
        return this;
    }

    @Nullable
    public DiscoverabilityLevel getDiscoverability() {
        return discoverability;
    }

    public ApiPlanBeanDto setDiscoverability(@Nullable DiscoverabilityLevel discoverability) {
        this.discoverability = discoverability;
        return this;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public ApiPlanBeanDto setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiPlanBeanDto that = (ApiPlanBeanDto) o;
        return Objects.equals(planId, that.planId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, version);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiPlanBeanDto.class.getSimpleName() + "[", "]")
                .add("planId='" + planId + "'")
                .add("version='" + version + "'")
                .add("requiresApproval=" + requiresApproval)
                .add("discoverability=" + discoverability)
                .toString();
    }
}
