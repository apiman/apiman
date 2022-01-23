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

import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.hibernate.annotations.ColumnDefault;

/**
 * Models a plan+version that is available for use with a particular API.  This
 * makes the Plan available when forming a Contract between an app and an API.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "api_plans",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "api_version_id", "plan_id", "version" }) }
)
@JsonInclude(Include.NON_NULL)
@IdClass(ApiPlanBeanCompositeId.class)
public class ApiPlanBean implements Serializable {

    private static final long serialVersionUID = 7972763768594076697L;

    // @GeneratedValue
    // private Long id;

    @Id
    @ManyToOne
    @JoinColumn(name = "api_version_id", referencedColumnName = "id")
    private ApiVersionBean apiVersion;

    @Id
    @Column(name = "plan_id", nullable = false)
    private String planId;

    @Id
    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "requires_approval", nullable = false)
    @ColumnDefault("false")
    private Boolean requiresApproval = false;

    @Column(name = "discoverability")
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ORG_MEMBERS'")
    private DiscoverabilityLevel discoverability = DiscoverabilityLevel.ORG_MEMBERS;

    /**
     * Constructor.
     */
    public ApiPlanBean() {
    }

    // public Long getId() {
    //     return id;
    // }
    //
    // public void setId(Long id) {
    //     this.id = id;
    // }

    public DiscoverabilityLevel getDiscoverability() {
        return discoverability;
    }

    public void setDiscoverability(DiscoverabilityLevel discoverability) {
        this.discoverability = discoverability;
    }

    public ApiVersionBean getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(ApiVersionBean apiVersion) {
        this.apiVersion = apiVersion;
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

    public ApiPlanBean setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
        return this;
    }

    public Boolean isRequiresApproval() {
        return requiresApproval;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
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
        return Objects.equals(apiVersion, that.apiVersion) && Objects.equals(planId, that.planId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiVersion, planId, version);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiPlanBean.class.getSimpleName() + "[", "]")
                .add("planId='" + planId + "'")
                .add("version='" + version + "'")
                .add("requiresApproval=" + requiresApproval)
                .add("discoverability=" + discoverability)
                .toString();
    }
}
