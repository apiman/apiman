package io.apiman.manager.api.beans.events;

import io.apiman.manager.api.beans.idm.UserDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Event issued when a user signs up for an API.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonDeserialize(builder = ContractCreatedEvent.Builder.class)
@ApimanEvent(version = 1)
public class ContractCreatedEvent implements IVersionedApimanEvent {

    private ApimanEventHeaders headers;
    private UserDto user;
    private String clientOrgId;
    private String clientId;
    private String clientVersion;
    private String apiOrgId;
    private String apiId;
    private String apiVersion;
    private String contractId;
    private String planId;
    private String planVersion;
    private boolean approvalRequired;

    ContractCreatedEvent(ApimanEventHeaders headers, UserDto user, String clientOrgId, String clientId,
         String clientVersion, String apiOrgId, String apiId, String apiVersion, String contractId, String planId,
         String planVersion,
         boolean approvalRequired) {
        this.headers = headers;
        this.user = user;
        this.clientOrgId = clientOrgId;
        this.clientId = clientId;
        this.clientVersion = clientVersion;
        this.apiOrgId = apiOrgId;
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.contractId = contractId;
        this.planId = planId;
        this.planVersion = planVersion;
        this.approvalRequired = approvalRequired;
    }

    // TODO(msavy): do we need this empty constructor anymore for POJO serialization or can we get away with it? Experiment w/ JAX-RS/RESTEasy
    public ContractCreatedEvent() {
    }

    public static Builder builder() {
        return new Builder();
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

    public String getClientOrgId() {
        return clientOrgId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getApiOrgId() {
        return apiOrgId;
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

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder implements ApimanEventBuilderMixin {
        @NotNull
        private ApimanEventHeaders headers;
        @NotNull
        private UserDto user;
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

        public Builder setApprovalRequired(Boolean approvalRequired) {
            this.approvalRequired = approvalRequired;
            return this;
        }

        public ContractCreatedEvent build() {
            beanValidate(this);
            // Objects.requireNonNull(headers, "Must provider headers");
            // Objects.requireNonNull(user, "Must provide user info");
            // Objects.requireNonNull(orgId, "Must provide org");
            // Objects.requireNonNull(contractId, "Must provide contractId");
            // Objects.requireNonNull(clientId, "Must provide clientId");
            // Objects.requireNonNull(clientVersion, "Must provide clientVersion");
            // Objects.requireNonNull(apiId, "Must provide apiId");
            // Objects.requireNonNull(apiVersion, "Must provide apiVersion");
            // Objects.requireNonNull(planId, "Must provide planId");
            // Objects.requireNonNull(planVersion, "Must provide planVersion");
            // Objects.requireNonNull(approvalRequired, "Must explicitly set whether approval is required");
            return new ContractCreatedEvent(headers, user, clientOrgId, clientId, clientVersion, apiOrgId, apiId,
                 apiVersion, contractId, planId, planVersion, approvalRequired);
        }
    }
}
