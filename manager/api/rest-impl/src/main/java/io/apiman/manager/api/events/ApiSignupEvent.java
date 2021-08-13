package io.apiman.manager.api.events;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Event issued when a user signs up for an API.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonDeserialize(builder = ApiSignupEvent.Builder.class)
public class ApiSignupEvent extends VersionedApimanEvent {

    private String userId;
    private String clientId;
    private String apiId;
    private String contractId;
    private boolean approvalRequired;

    public ApiSignupEvent(ApimanEventHeaders headers, String userId, String clientId, String apiId,
         String contractId, boolean approvalRequired) {
        super(headers);
        this.userId = userId;
        this.clientId = clientId;
        this.apiId = apiId;
        this.contractId = contractId;
        this.approvalRequired = approvalRequired;
    }

    // TODO do we need this empty constructor anymore for POJO serialization or can we get away with it? Experiment w/ JAX-RS/RESTEasy
    ApiSignupEvent() {
        super(null);
    }

    public String getUserId() {
        return userId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getApiId() {
        return apiId;
    }

    public String getContractId() {
        return contractId;
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
        private String userId;
        private String clientId;
        private String apiId;
        private String contractId;
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

        public Builder setUserId(String userId) {
            this.userId = userId;
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

        public ApiSignupEvent build() {
            Objects.requireNonNull(headers, "Must provider headers");
            Objects.requireNonNull(userId, "Must provide userId");
            Objects.requireNonNull(clientId, "Must provide clientId");
            Objects.requireNonNull(apiId, "Must provide apiId");
            Objects.requireNonNull(contractId, "Must provide contractId");
            Objects.requireNonNull(approvalRequired, "Must explicitly set whether approval is required");
            return new ApiSignupEvent(headers, userId, clientId, apiId, contractId, approvalRequired);
        }
    }
}
