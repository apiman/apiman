package io.apiman.manager.api.beans.developers;

import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.EndpointContentType;
import io.apiman.manager.api.beans.apis.EndpointType;
import io.apiman.manager.api.beans.apis.dto.ApiPlanBeanDto;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Temporary DTO to make old developer endpoint work properly until we delete it.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Deprecated
public class DeveloperApiVersionBeanDto {
        private Long id;
        private ApiBean api;
        private ApiStatus status;
        private String endpoint;
        private EndpointType endpointType;
        private EndpointContentType endpointContentType;
        private Map<String, String> endpointProperties = new HashMap<>();
        private Set<ApiGatewayBean> gateways;
        private boolean publicAPI;
        private DiscoverabilityLevel publicDiscoverability;
        private Set<ApiPlanBeanDto> plans;
        private String version;
        private String createdBy;
        private Date createdOn;
        private String modifiedBy;
        private Date modifiedOn;
        private Date publishedOn;
        private Date retiredOn;
        private ApiDefinitionType definitionType;
        private boolean parsePayload;
        private boolean disableKeysStrip;
        private String definitionUrl;
        private String extendedDescription;

    public DeveloperApiVersionBeanDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApiBean getApi() {
        return api;
    }

    public void setApi(ApiBean api) {
        this.api = api;
    }

    public ApiStatus getStatus() {
        return status;
    }

    public void setStatus(ApiStatus status) {
        this.status = status;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public EndpointType getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }

    public EndpointContentType getEndpointContentType() {
        return endpointContentType;
    }

    public void setEndpointContentType(EndpointContentType endpointContentType) {
        this.endpointContentType = endpointContentType;
    }

    public Map<String, String> getEndpointProperties() {
        return endpointProperties;
    }

    public void setEndpointProperties(Map<String, String> endpointProperties) {
        this.endpointProperties = endpointProperties;
    }

    public Set<ApiGatewayBean> getGateways() {
        return gateways;
    }

    public void setGateways(Set<ApiGatewayBean> gateways) {
        this.gateways = gateways;
    }

    public boolean isPublicAPI() {
        return publicAPI;
    }

    public void setPublicAPI(boolean publicAPI) {
        this.publicAPI = publicAPI;
    }

    public DiscoverabilityLevel getPublicDiscoverability() {
        return publicDiscoverability;
    }

    public DeveloperApiVersionBeanDto setPublicDiscoverability(DiscoverabilityLevel publicDiscoverability) {
        this.publicDiscoverability = publicDiscoverability;
        return this;
    }

    public Set<ApiPlanBeanDto> getPlans() {
        return plans;
    }

    public void setPlans(Set<ApiPlanBeanDto> plans) {
        this.plans = plans;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Date getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(Date publishedOn) {
        this.publishedOn = publishedOn;
    }

    public Date getRetiredOn() {
        return retiredOn;
    }

    public void setRetiredOn(Date retiredOn) {
        this.retiredOn = retiredOn;
    }

    public ApiDefinitionType getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(ApiDefinitionType definitionType) {
        this.definitionType = definitionType;
    }

    public boolean isParsePayload() {
        return parsePayload;
    }

    public void setParsePayload(boolean parsePayload) {
        this.parsePayload = parsePayload;
    }

    public boolean isDisableKeysStrip() {
        return disableKeysStrip;
    }

    public void setDisableKeysStrip(boolean disableKeysStrip) {
        this.disableKeysStrip = disableKeysStrip;
    }

    public String getDefinitionUrl() {
        return definitionUrl;
    }

    public void setDefinitionUrl(String definitionUrl) {
        this.definitionUrl = definitionUrl;
    }



    public String getExtendedDescription() {
        return extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeveloperApiVersionBeanDto that = (DeveloperApiVersionBeanDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DeveloperApiVersionBeanDto.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("api=" + api)
                .add("status=" + status)
                .add("endpoint='" + endpoint + "'")
                .add("endpointType=" + endpointType)
                .add("endpointContentType=" + endpointContentType)
                .add("endpointProperties=" + endpointProperties)
                .add("gateways=" + gateways)
                .add("publicAPI=" + publicAPI)
                .add("publicDiscoverability=" + publicDiscoverability)
                .add("plans=" + plans)
                .add("version='" + version + "'")
                .add("createdBy='" + createdBy + "'")
                .add("createdOn=" + createdOn)
                .add("modifiedBy='" + modifiedBy + "'")
                .add("modifiedOn=" + modifiedOn)
                .add("publishedOn=" + publishedOn)
                .add("retiredOn=" + retiredOn)
                .add("definitionType=" + definitionType)
                .add("parsePayload=" + parsePayload)
                .add("disableKeysStrip=" + disableKeysStrip)
                .add("definitionUrl='" + definitionUrl + "'")
                .add("extendedDescription='" + extendedDescription + "'")
                .toString();
    }
}
