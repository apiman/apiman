package io.apiman.manager.api.beans.events;

import io.apiman.manager.api.beans.idm.UserDto;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Was the contract accepted or rejected.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonDeserialize(builder = ContractApprovalEvent.Builder.class)
@ApimanEvent(version = 1)
public class ContractApprovalEvent implements IVersionedApimanEvent {

    private final ApimanEventHeaders headers;
    private final UserDto approver;
    private final String clientOrgId;
    private final String clientId;
    private final String clientVersion;
    private final String apiOrgId;
    private final String apiId;
    private final String apiVersion;
    private final String contractId;
    private final String planId;
    private final String planVersion;
    private final boolean approved;
    private final String rejectionReason;

    ContractApprovalEvent(ApimanEventHeaders headers, UserDto approver,
         String clientOrgId, String clientId, String clientVersion,
         String apiOrgId, String apiId, String apiVersion, String contractId, String planId, String planVersion,
         boolean approved, String rejectionReason) {
        this.headers = headers;
        this.approver = approver;
        this.clientOrgId = clientOrgId;
        this.clientId = clientId;
        this.clientVersion = clientVersion;
        this.apiOrgId = apiOrgId;
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.contractId = contractId;
        this.planId = planId;
        this.planVersion = planVersion;
        this.approved = approved;
        this.rejectionReason = rejectionReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ApimanEventHeaders getHeaders() {
        return headers;
    }

    public UserDto getApprover() {
        return approver;
    }

    public String getClientOrgId() {
        return clientOrgId;
    }

    public String getApiOrgId() {
        return apiOrgId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getApiId() {
        return apiId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getContractId() {
        return contractId;
    }

    public String getPlanId() {
        return planId;
    }

    public String getPlanVersion() {
        return planVersion;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder implements ApimanBuilderMixin {

        @NotNull
        private ApimanEventHeaders headers;
        @NotNull
        private UserDto approver;
        @NotBlank
        private String clientOrgId;
        @NotBlank
        private String clientId;
        @NotBlank
        private String clientVersion;
        @NotBlank
        private String apiOrgId;
        @NotBlank
        private String apiId;
        @NotBlank
        private String apiVersion;
        @NotBlank
        private String contractId;
        @NotBlank
        private String planId;
        @NotBlank
        private String planVersion;
        @NotNull
        private Boolean approved;
        @Nullable
        private String rejectionReason;

        public Builder() {
        }

        public Builder setHeaders(ApimanEventHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder setApprover(UserDto approver) {
            this.approver = approver;
            return this;
        }

        public Builder setClientOrgId(String clientOrgId) {
            this.clientOrgId = clientOrgId;
            return this;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setClientVersion(String clientVersion) {
            this.clientVersion = clientVersion;
            return this;
        }

        public Builder setApiOrgId(String apiOrgId) {
            this.apiOrgId = apiOrgId;
            return this;
        }

        public Builder setApiId(String apiId) {
            this.apiId = apiId;
            return this;
        }

        public Builder setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public Builder setContractId(String contractId) {
            this.contractId = contractId;
            return this;
        }

        public Builder setPlanId(String planId) {
            this.planId = planId;
            return this;
        }

        public Builder setPlanVersion(String planVersion) {
            this.planVersion = planVersion;
            return this;
        }

        public Builder setApproved(Boolean approved) {
            this.approved = approved;
            return this;
        }

        public Builder setRejectionReason(@Nullable String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        public ContractApprovalEvent build() {
            beanValidate(this);
            return new ContractApprovalEvent(headers, approver, clientOrgId, clientId, clientVersion, apiOrgId, apiId,
                 apiVersion, contractId, planId, planVersion, approved, rejectionReason);
        }
    }
}
