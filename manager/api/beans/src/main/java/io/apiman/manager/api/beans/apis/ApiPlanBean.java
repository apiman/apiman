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
package io.apiman.manager.api.beans.apis;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Models a plan+version that is available for use with a particular API.  This
 * makes the Plan available when forming a Contract between an app and an API.
 *
 * @author eric.wittmann@redhat.com
 */
@Embeddable
public class ApiPlanBean implements Serializable {

    private static final long serialVersionUID = 7972763768594076697L;

    @Column(name = "plan_id", nullable = false)
    private String planId;
    @Column(name = "version", nullable = false)
    private String version;
    @Column(name = "expose_in_portal", nullable = false)
    private Boolean exposeInPortal = false;
    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = false;

    /**
     * Constructor.
     */
    public ApiPlanBean() {
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
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }


    public Boolean isExposeInPortal() {
        return exposeInPortal;
    }

    public ApiPlanBean setExposeInPortal(Boolean exposeInPortal) {
        this.exposeInPortal = exposeInPortal;
        return this;
    }

    public Boolean isRequiresApproval() {
        return requiresApproval;
    }

    public ApiPlanBean setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiPlanBean.class.getSimpleName() + "[", "]")
             .add("planId='" + planId + "'")
             .add("version='" + version + "'")
             .add("exposeInPortal=" + exposeInPortal)
             .add("requiresApproval=" + requiresApproval)
             .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, version, exposeInPortal, requiresApproval);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiPlanBean that = (ApiPlanBean) o;
        return Objects.equals(planId, that.planId)
                       && Objects.equals(version, that.version)
                       && Objects.equals(exposeInPortal, that.exposeInPortal)
                       && Objects.equals(requiresApproval, that.requiresApproval);
    }
}
