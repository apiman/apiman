package io.apiman.manager.api.beans.idm;

import java.util.Objects;
import java.util.StringJoiner;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public final class DiscoverabilityDto {
    private String id;
    private String orgId;
    private String apiId;
    private String apiVersion;
    private String planId;
    private String planVersion;
    private DiscoverabilityLevel discoverability;

    public DiscoverabilityDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanVersion() {
        return planVersion;
    }

    public void setPlanVersion(String planVersion) {
        this.planVersion = planVersion;
    }

    public DiscoverabilityLevel getDiscoverability() {
        return discoverability;
    }

    public void setDiscoverability(DiscoverabilityLevel discoverability) {
        this.discoverability = discoverability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiscoverabilityDto that = (DiscoverabilityDto) o;
        return Objects.equals(id, that.id) && Objects.equals(orgId, that.orgId) && Objects.equals(apiId, that.apiId)
                       && Objects.equals(apiVersion, that.apiVersion) && Objects.equals(planId, that.planId) && Objects.equals(planVersion,
                that.planVersion) && discoverability == that.discoverability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orgId, apiId, apiVersion, planId, planVersion, discoverability);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DiscoverabilityDto.class.getSimpleName() + "[", "]")
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