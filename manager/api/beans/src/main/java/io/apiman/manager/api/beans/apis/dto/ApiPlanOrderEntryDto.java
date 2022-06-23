/*
 * Copyright 2022 Scheer PAS Schweiz AG
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
package io.apiman.manager.api.beans.apis.dto;

import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiPlanOrderEntryDto {
    @NotNull
    private Long apiVersionId;
    @NotBlank
    private String planId;
    @NotBlank
    private String version;

    public ApiPlanOrderEntryDto() {
    }

    public Long getApiVersionId() {
        return apiVersionId;
    }

    public ApiPlanOrderEntryDto setApiVersionId(Long apiVersionId) {
        this.apiVersionId = apiVersionId;
        return this;
    }

    public String getPlanId() {
        return planId;
    }

    public ApiPlanOrderEntryDto setPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ApiPlanOrderEntryDto setVersion(String version) {
        this.version = version;
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
        ApiPlanOrderEntryDto that = (ApiPlanOrderEntryDto) o;
        return Objects.equals(apiVersionId, that.apiVersionId) && Objects.equals(planId, that.planId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiVersionId, planId, version);
    }
}
