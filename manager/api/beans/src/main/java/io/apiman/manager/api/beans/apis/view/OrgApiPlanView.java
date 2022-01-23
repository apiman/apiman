package io.apiman.manager.api.beans.apis.view;

import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class OrgApiPlanView {
    private String orgId;
    private boolean publicApi;
    private String planId;
    private String planVersion;
    private String apiId;
    private String apiVersion;
    private DiscoverabilityLevel discoverability;
    private boolean requiresApproval;

    public OrgApiPlanView() {
    }

    public OrgApiPlanView(String orgId, boolean publicApi, String planId, String planVersion, String apiId, String apiVersion, DiscoverabilityLevel discoverability, boolean requiresApproval) {
        this.orgId = orgId;
        this.publicApi = publicApi;
        this.planId = planId;
        this.planVersion = planVersion;
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.discoverability = discoverability;
        this.requiresApproval = requiresApproval;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
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

    public DiscoverabilityLevel getDiscoverability() {
        return discoverability;
    }

    public OrgApiPlanView setDiscoverability(DiscoverabilityLevel discoverability) {
        this.discoverability = discoverability;
        return this;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public boolean isPublicApi() {
        return publicApi;
    }

    public void setPublicApi(boolean publicApi) {
        this.publicApi = publicApi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrgApiPlanView that = (OrgApiPlanView) o;
        return publicApi == that.publicApi && requiresApproval == that.requiresApproval && Objects.equals(orgId, that.orgId) && Objects.equals(planId,
                that.planId) && Objects.equals(planVersion, that.planVersion) && Objects.equals(apiId, that.apiId) && Objects.equals(apiVersion,
                that.apiVersion) && discoverability == that.discoverability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId, publicApi, planId, planVersion, apiId, apiVersion, discoverability, requiresApproval);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrgApiPlanView.class.getSimpleName() + "[", "]")
                .add("orgId='" + orgId + "'")
                .add("publicApi=" + publicApi)
                .add("planId='" + planId + "'")
                .add("planVersion='" + planVersion + "'")
                .add("apiId='" + apiId + "'")
                .add("apiVersion='" + apiVersion + "'")
                .add("discoverability=" + discoverability)
                .add("requiresApproval=" + requiresApproval)
                .toString();
    }
}
