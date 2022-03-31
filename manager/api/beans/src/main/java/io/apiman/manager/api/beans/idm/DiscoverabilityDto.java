package io.apiman.manager.api.beans.idm;

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
}