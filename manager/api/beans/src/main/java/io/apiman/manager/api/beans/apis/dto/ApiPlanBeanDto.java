package io.apiman.manager.api.beans.apis.dto;

import java.util.Objects;
import java.util.StringJoiner;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiPlanBeanDto {
    private String planId;
    private String version;
    private Boolean exposeInPortal;
    private Boolean requiresApproval;

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

    public Boolean getExposeInPortal() {
        return exposeInPortal;
    }

    public ApiPlanBeanDto setExposeInPortal(Boolean exposeInPortal) {
        this.exposeInPortal = exposeInPortal;
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
                .add("exposeInPortal=" + exposeInPortal)
                .add("requiresApproval=" + requiresApproval)
                .toString();
    }
}
