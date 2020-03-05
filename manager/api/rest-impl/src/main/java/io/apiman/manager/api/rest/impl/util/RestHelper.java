package io.apiman.manager.api.rest.impl.util;

import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides static helper methods the hide sensitive data from certain beans (eg. createdOn, createdBy).
 * The fields are set explicit so if new fields are added in future there is no chance to forget to set a field to null.
 */
public final class RestHelper {
    /**
     * Constructor
     */
    public RestHelper() {
    }

    /**
     * This method will hide sensitive data, such as created by, from the result
     *
     * @param securityContext the security context
     * @param roleBean        the role
     * @return the role without sensitive data
     */
    public static RoleBean hideSensitiveDataFromRoleBean(ISecurityContext securityContext, RoleBean roleBean) {
        if (securityContext.isAdmin()) {
            return roleBean;
        } else {
            RoleBean role = new RoleBean();
            role.setId(roleBean.getId());
            role.setName(roleBean.getName());
            role.setPermissions(roleBean.getPermissions());
            role.setAutoGrant(roleBean.getAutoGrant());
            role.setDescription(roleBean.getDescription());
            // check if the role was created by the current user
            if (securityContext.getCurrentUser().equals(roleBean.getCreatedBy())) {
                role.setCreatedBy(roleBean.getCreatedBy());
                role.setCreatedOn(roleBean.getCreatedOn());
            }
            return role;
        }
    }

    /**
     * This method will hide sensitive data, such as created by, from the result
     *
     * @param apiSummaryBeans the summary beans
     * @return A list with beans without sensitive data
     */
    public static List<ApiSummaryBean> hideSensitiveDataFromApiSummaryBeanList(List<ApiSummaryBean> apiSummaryBeans) {
        List<ApiSummaryBean> apis = new ArrayList<>();
        for (ApiSummaryBean apiSummaryBean : apiSummaryBeans) {
            ApiSummaryBean apiSummary = new ApiSummaryBean();
            apiSummary.setOrganizationId(apiSummaryBean.getOrganizationId());
            apiSummary.setOrganizationName(apiSummaryBean.getOrganizationName());
            apiSummary.setId(apiSummaryBean.getId());
            apiSummary.setName(apiSummaryBean.getName());
            apiSummary.setDescription(apiSummaryBean.getDescription());
            apis.add(apiSummary);
        }
        return apis;
    }

    /**
     * This method will hide sensitive data, such as created by, from the result
     *
     * @param securityContext    the security context
     * @param policySummaryBeans the summary beans
     * @return A list with beans without sensitive data
     */
    public static List<PolicySummaryBean> hideSensitiveDataFromPolicySummaryBeanList(ISecurityContext securityContext, List<PolicySummaryBean> policySummaryBeans) {
        List<PolicySummaryBean> policies = new ArrayList<>();
        for (PolicySummaryBean policySummaryBean : policySummaryBeans) {
            PolicySummaryBean policySumarry = new PolicySummaryBean();
            policySumarry.setId(policySummaryBean.getId());
            policySumarry.setPolicyDefinitionId(policySummaryBean.getPolicyDefinitionId());
            policySumarry.setName(policySummaryBean.getName());
            policySumarry.setDescription(policySummaryBean.getDescription());
            policySumarry.setIcon(policySummaryBean.getIcon());
            // check if the role was created by the current user
            if (securityContext.getCurrentUser().equals(policySummaryBean.getCreatedBy())) {
                policySumarry.setCreatedBy(policySummaryBean.getCreatedBy());
                policySumarry.setCreatedOn(policySummaryBean.getCreatedOn());
            }
            policies.add(policySumarry);
        }
        return policies;
    }

    /**
     * This method will hide sensitive data, such as created by, from the result
     *
     * @param apiVersionBean the apiVersionBean
     * @return the apiVersionBean without sensitive data
     */
    public static ApiVersionBean hideSensitiveDataFromApiVersionBean(ApiVersionBean apiVersionBean) {
        ApiBean api = new ApiBean();
        api.setId(apiVersionBean.getApi().getId());
        api.setName(apiVersionBean.getApi().getName());
        api.setDescription(apiVersionBean.getApi().getDescription());

        OrganizationBean org = new OrganizationBean();
        org.setId(apiVersionBean.getApi().getOrganization().getId());
        org.setName(apiVersionBean.getApi().getOrganization().getName());
        org.setDescription(apiVersionBean.getApi().getOrganization().getDescription());

        api.setOrganization(org);

        ApiVersionBean apiVersion = new ApiVersionBean();
        apiVersion.setApi(api);
        apiVersion.setStatus(apiVersionBean.getStatus());
        apiVersion.setEndpointType(apiVersionBean.getEndpointType());
        apiVersion.setEndpointContentType(apiVersionBean.getEndpointContentType());
        apiVersion.setGateways(apiVersionBean.getGateways());
        apiVersion.setPublicAPI(apiVersionBean.isPublicAPI());
        apiVersion.setPlans(apiVersionBean.getPlans());
        apiVersion.setVersion(apiVersionBean.getVersion());
        apiVersion.setDefinitionType(apiVersionBean.getDefinitionType());
        return apiVersion;
    }

    /**
     * This method will hide sensitive data, such as created by, from the result
     *
     * @param organizationBean the organizationBean
     * @return the organization without sensitive data
     */
    public static OrganizationBean hideSensitiveDataFromOrganizationBean(OrganizationBean organizationBean) {
        OrganizationBean org = new OrganizationBean();
        org.setId(organizationBean.getId());
        org.setName(organizationBean.getName());
        org.setDescription(organizationBean.getDescription());
        return org;
    }
}
