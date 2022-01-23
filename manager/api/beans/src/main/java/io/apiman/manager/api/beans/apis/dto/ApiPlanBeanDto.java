package io.apiman.manager.api.beans.apis.dto;

import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiPlanBeanDto {
    @NotBlank
    private String planId;
    @NotBlank
    private String version;
    private Boolean requiresApproval;
    private DiscoverabilityLevel discoverability;

    public ApiPlanBeanDto() {
    }

    public String getPlanId() {
        return planId;
    }

    public ApiPlanBeanDto setPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ApiPlanBeanDto setVersion(String version) {
        this.version = version;
        return this;
    }

    @Nullable
    public DiscoverabilityLevel getDiscoverability() {
        return discoverability;
    }

    public ApiPlanBeanDto setDiscoverability(@Nullable DiscoverabilityLevel discoverability) {
        this.discoverability = discoverability;
        return this;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public ApiPlanBeanDto setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiPlanBeanDto that = (ApiPlanBeanDto) o;
        return Objects.equals(planId, that.planId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, version);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiPlanBeanDto.class.getSimpleName() + "[", "]")
                .add("planId='" + planId + "'")
                .add("version='" + version + "'")
                .add("requiresApproval=" + requiresApproval)
                .add("discoverability=" + discoverability)
                .toString();
    }
}
