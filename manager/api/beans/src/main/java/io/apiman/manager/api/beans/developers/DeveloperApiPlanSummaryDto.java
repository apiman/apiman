package io.apiman.manager.api.beans.developers;

import io.apiman.manager.api.beans.policies.PolicyBean;

import java.util.List;
import java.util.StringJoiner;

/**
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class DeveloperApiPlanSummaryDto {

    private String planId;
    private String planName;
    private String planDescription;
    private String version;
    private Boolean requiresApproval;
    private List<PolicyBean> planPolicies;

    public DeveloperApiPlanSummaryDto() {
    }

    public String getPlanId() {
        return planId;
    }

    public DeveloperApiPlanSummaryDto setPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    public String getPlanName() {
        return planName;
    }

    public DeveloperApiPlanSummaryDto setPlanName(String planName) {
        this.planName = planName;
        return this;
    }

    public String getPlanDescription() {
        return planDescription;
    }

    public DeveloperApiPlanSummaryDto setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public DeveloperApiPlanSummaryDto setVersion(String version) {
        this.version = version;
        return this;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public DeveloperApiPlanSummaryDto setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
        return this;
    }

    public List<PolicyBean> getPlanPolicies() {
        return planPolicies;
    }

    public DeveloperApiPlanSummaryDto setPlanPolicies(List<PolicyBean> planPolicies) {
        this.planPolicies = planPolicies;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DeveloperApiPlanSummaryDto.class.getSimpleName() + "[", "]")
                .add("planId='" + planId + "'")
                .add("planName='" + planName + "'")
                .add("planDescription='" + planDescription + "'")
                .add("version='" + version + "'")
                .add("requiresApproval=" + requiresApproval)
                .add("planPolicies=" + planPolicies)
                .toString();
    }
}