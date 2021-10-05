package io.apiman.manager.api.beans.developers;

import io.apiman.manager.api.beans.summary.PolicySummaryBean;

/**
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiVersionPolicySummaryDto extends PolicySummaryBean {

    private String policyConfiguration;

    public ApiVersionPolicySummaryDto() {
        super();
    }

    public String getPolicyConfiguration() {
        return policyConfiguration;
    }

    public ApiVersionPolicySummaryDto setPolicyConfiguration(String policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
        return this;
    }
}
