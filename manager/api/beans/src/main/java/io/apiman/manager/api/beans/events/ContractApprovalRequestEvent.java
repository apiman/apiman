package io.apiman.manager.api.beans.events;

import io.apiman.manager.api.beans.idm.UserDto;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Event issued when a user signs up for an API.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonDeserialize(builder = ContractApprovalRequestEvent.Builder.class)
@ApimanEvent(version = 1)
public class ContractApprovalRequestEvent implements IVersionedApimanEvent {

    private ApimanEventHeaders headers;
    private UserDto user;
    private String orgId;
    private String clientId;
    private String clientVersion;
    private String apiId;
    private String apiVersion;
    private String contractId;
    private String planId;
    private String planVersion;
    private boolean approvalRequired;

    public ContractApprovalRequestEvent(ApimanEventHeaders headers, UserDto user, String orgId, String clientId,
         String clientVersion, String apiId, String apiVersion, String contractId, String planId, String planVersion,
         boolean approvalRequired) {
        this.headers = headers;
        this.user = user;
        this.orgId = orgId;
        this.clientId = clientId;
        this.clientVersion = clientVersion;
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.contractId = contractId;
        this.planId = planId;
        this.planVersion = planVersion;
        this.approvalRequired = approvalRequired;
    }

    // TODO do we need this empty constructor anymore for POJO serialization or can we get away with it? Experiment w/ JAX-RS/RESTEasy
    ContractApprovalRequestEvent() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApimanEventHeaders getHeaders() {
        return headers;
    }

    public UserDto getUser() {
        return user;
    }

    public String getOrgId() {
        return orgId;
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

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private ApimanEventHeaders headers;
        private UserDto user;
        private String orgId;
        private String clientId;
        private String clientVersion;
        private String apiId;
        private String apiVersion;
        private String contractId;
        private String planId;
        private String planVersion;
        private Boolean approvalRequired;

        public Builder() {
        }

        public ApimanEventHeaders.Builder headers() {
            return ApimanEventHeaders.builder();
        }

        public Builder setHeaders(ApimanEventHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder setUser(UserDto user) {
            this.user = user;
            return this;
        }

        public Builder setOrgId(String orgId) {
            this.orgId = orgId;
            return this;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setApiId(String apiId) {
            this.apiId = apiId;
            return this;
        }

        public Builder setContractId(String contractId) {
            this.contractId = contractId;
            return this;
        }

        public Builder setApprovalRequired(Boolean approvalRequired) {
            this.approvalRequired = approvalRequired;
            return this;
        }

        public Builder setClientVersion(String clientVersion) {
            this.clientVersion = clientVersion;
            return this;
        }

        public Builder setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
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

        public ContractApprovalRequestEvent build() {
            Objects.requireNonNull(headers, "Must provider headers");
            Objects.requireNonNull(user, "Must provide user info");
            Objects.requireNonNull(orgId, "Must provide org");
            Objects.requireNonNull(contractId, "Must provide contractId");
            Objects.requireNonNull(clientId, "Must provide clientId");
            Objects.requireNonNull(clientVersion, "Must provide clientVersion");
            Objects.requireNonNull(apiId, "Must provide apiId");
            Objects.requireNonNull(apiVersion, "Must provide apiVersion");
            Objects.requireNonNull(planId, "Must provide planId");
            Objects.requireNonNull(planVersion, "Must provide planVersion");
            Objects.requireNonNull(approvalRequired, "Must explicitly set whether approval is required");
            return new ContractApprovalRequestEvent(headers, user, orgId, clientId, clientVersion, apiId, apiVersion,
                 contractId, planId, planVersion, approvalRequired);
        }
    }
}
