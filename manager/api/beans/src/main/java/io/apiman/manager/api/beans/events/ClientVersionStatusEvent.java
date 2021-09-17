package io.apiman.manager.api.beans.events;

import io.apiman.manager.api.beans.clients.ClientStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonDeserialize(builder = ClientVersionStatusEvent.Builder.class)
@ApimanEvent(version = 1)
public class ClientVersionStatusEvent implements IVersionedApimanEvent {

    private ApimanEventHeaders headers;
    private String clientOrgId;
    private String clientId;
    private String clientVersion;
    private ClientStatus previousStatus;
    private ClientStatus newStatus;

    ClientVersionStatusEvent(ApimanEventHeaders headers, String clientOrgId, String clientId,
         String clientVersion, ClientStatus previousStatus, ClientStatus newStatus) {
        this.headers = headers;
        this.clientOrgId = clientOrgId;
        this.clientId = clientId;
        this.clientVersion = clientVersion;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public ClientVersionStatusEvent() {
    }

    public static ClientVersionStatusEvent.Builder builder() {
        return new ClientVersionStatusEvent.Builder();
    }

    @Override
    public ApimanEventHeaders getHeaders() {
        return headers;
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

    public ClientStatus getPreviousStatus() {
        return previousStatus;
    }

    public ClientStatus getNewStatus() {
        return newStatus;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder implements ApimanEventBuilderMixin {
        @NotNull
        private ApimanEventHeaders headers;
        @NotBlank
        private String clientOrgId;
        @NotBlank
        private String clientId;
        @NotBlank
        private String clientVersion;
        @NotNull
        private ClientStatus previousStatus;
        @NotNull
        private ClientStatus newStatus;

        public Builder setHeaders(ApimanEventHeaders headers) {
            this.headers = headers;
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

        public Builder setPreviousStatus(ClientStatus previousStatus) {
            this.previousStatus = previousStatus;
            return this;
        }

        public Builder setNewStatus(ClientStatus newStatus) {
            this.newStatus = newStatus;
            return this;
        }

        public ClientVersionStatusEvent build() {
            beanValidate(this);
            return new ClientVersionStatusEvent(headers, clientOrgId, clientId, clientVersion, previousStatus, newStatus);
        }
    }
}
