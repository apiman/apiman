package io.apiman.manager.api.beans.idm;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Entity
// @Table(name = "discoverability", uniqueConstraints = {
//         @UniqueConstraint(columnNames = {
//                 "org_id", "api_id", "api_version", "plan_id", "plan_version"
//         })
// })
@Table(name = "discoverability")
public class DiscoverabilityEntity implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "org_id")
    @NotBlank
    private String orgId;

    @Column(name = "api_id")
    @NotBlank
    private String apiId;

    @Column(name = "api_version")
    @NotBlank
    private String apiVersion;

    @Column(name = "plan_id")
    @Nullable
    private String planId;

    @Column(name = "plan_version")
    @Nullable
    private String planVersion;

    @Column(name = "discoverability")
    @NotNull
    @Enumerated(EnumType.STRING)
    private DiscoverabilityLevel discoverability;

    public DiscoverabilityEntity() {
    }

    public String getId() {
        return id;
    }

    public DiscoverabilityEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getOrgId() {
        return orgId;
    }

    public DiscoverabilityEntity setOrgId(String orgId) {
        this.orgId = orgId;
        return this;
    }

    public String getApiId() {
        return apiId;
    }

    public DiscoverabilityEntity setApiId(String apiId) {
        this.apiId = apiId;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public DiscoverabilityEntity setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Nullable
    public String getPlanId() {
        return planId;
    }

    public DiscoverabilityEntity setPlanId(@Nullable String planId) {
        this.planId = planId;
        return this;
    }

    @Nullable
    public String getPlanVersion() {
        return planVersion;
    }

    public DiscoverabilityEntity setPlanVersion(@Nullable String planVersion) {
        this.planVersion = planVersion;
        return this;
    }

    public DiscoverabilityLevel getDiscoverability() {
        return discoverability;
    }

    public DiscoverabilityEntity setDiscoverability(DiscoverabilityLevel discoverability) {
        this.discoverability = discoverability;
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
        DiscoverabilityEntity that = (DiscoverabilityEntity) o;
        return Objects.equals(id, that.id) && discoverability == that.discoverability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, discoverability);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DiscoverabilityEntity.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("orgId='" + orgId + "'")
                .add("apiId='" + apiId + "'")
                .add("apiVersion='" + apiVersion + "'")
                .add("planId='" + planId + "'")
                .add("planVersion='" + planVersion + "'")
                .add("discoverability=" + discoverability)
                .toString();
    }
}
