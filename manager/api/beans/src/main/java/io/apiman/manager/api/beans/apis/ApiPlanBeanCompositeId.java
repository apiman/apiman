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
package io.apiman.manager.api.beans.apis;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import org.hibernate.Hibernate;

/**
 * Composite ID for {@link ApiPlanBean}
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiPlanBeanCompositeId implements Serializable {

    private static final long serialVersionUID = -8712802849367905765L;

    private ApiVersionBean apiVersion;
    private String planId;
    private String version;

    public ApiPlanBeanCompositeId(ApiVersionBean apiVersion, String planId, String version) {
        this.apiVersion = apiVersion;
        this.planId = planId;
        this.version = version;
    }

    public ApiPlanBeanCompositeId() {
    }

    public ApiVersionBean getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(ApiVersionBean apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ApiPlanBeanCompositeId that = (ApiPlanBeanCompositeId) o;
        return apiVersion != null && Objects.equals(apiVersion, that.apiVersion)
                       && version != null && Objects.equals(version, that.version)
                       && planId != null && Objects.equals(planId, that.planId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiVersion, version, planId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiPlanBeanCompositeId.class.getSimpleName() + "[", "]")
                .add("apiVersion=??")
                .add("planId='" + planId + "'")
                .add("version='" + version + "'")
                .toString();
    }
}

