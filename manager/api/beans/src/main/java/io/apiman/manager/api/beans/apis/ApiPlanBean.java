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

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((planId == null) ? 0 : planId.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApiPlanBean other = (ApiPlanBean) obj;
        if (planId == null) {
            if (other.planId != null)
                return false;
        } else if (!planId.equals(other.planId))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

}
