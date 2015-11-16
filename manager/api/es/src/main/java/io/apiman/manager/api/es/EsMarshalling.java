/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.es;

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationStatus;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.AuditEntryType;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.download.DownloadType;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.GatewayType;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionTemplateBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.services.EndpointContentType;
import io.apiman.manager.api.beans.services.EndpointType;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceDefinitionType;
import io.apiman.manager.api.beans.services.ServiceGatewayBean;
import io.apiman.manager.api.beans.services.ServicePlanBean;
import io.apiman.manager.api.beans.services.ServiceStatus;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.ApiEntryBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.ApplicationVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyFormType;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceVersionSummaryBean;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.es.beans.PoliciesBean;
import io.apiman.manager.api.es.beans.ServiceDefinitionBean;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

/**
 * Marshalls objects into Maps to be used in ES requests.  Also unmarshalls from
 * maps back into objects.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class EsMarshalling {

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(PoliciesBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("organizationId", bean.getOrganizationId())
                    .field("entityId", bean.getEntityId())
                    .field("entityVersion", bean.getEntityVersion())
                    .field("type", bean.getType());
            List<PolicyBean> policies = bean.getPolicies();
            if (policies != null && !policies.isEmpty()) {
                builder.startArray("policies");
                for (PolicyBean policy : policies) {
                    builder.startObject()
                        .field("id", policy.getId())
                        .field("name", policy.getName())
                        .field("configuration", policy.getConfiguration())
                        .field("createdBy", policy.getCreatedBy())
                        .field("createdOn", policy.getCreatedOn().getTime())
                        .field("modifiedBy", policy.getModifiedBy())
                        .field("modifiedOn", policy.getModifiedOn().getTime())
                        .field("definitionId", policy.getDefinition().getId())
                        .field("orderIndex", policy.getOrderIndex())
                    .endObject();
                }
                builder.endArray();
            }
            builder.endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(GatewayBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("type", bean.getType())
                    .field("configuration", bean.getConfiguration())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("modifiedBy", bean.getModifiedBy())
                    .field("modifiedOn", bean.getModifiedOn().getTime())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(ServiceDefinitionBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("data", bean.getData())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(ContractBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("apiKey", bean.getApikey())
                    .field("appOrganizationId", bean.getApplication().getApplication().getOrganization().getId())
                    .field("appOrganizationName", bean.getApplication().getApplication().getOrganization().getName())
                    .field("appId", bean.getApplication().getApplication().getId())
                    .field("appName", bean.getApplication().getApplication().getName())
                    .field("appVersion", bean.getApplication().getVersion())
                    .field("serviceOrganizationId", bean.getService().getService().getOrganization().getId())
                    .field("serviceOrganizationName", bean.getService().getService().getOrganization().getName())
                    .field("serviceId", bean.getService().getService().getId())
                    .field("serviceName", bean.getService().getService().getName())
                    .field("serviceVersion", bean.getService().getVersion())
                    .field("serviceDescription", bean.getService().getService().getDescription())
                    .field("planName", bean.getPlan().getPlan().getName())
                    .field("planId", bean.getPlan().getPlan().getId())
                    .field("planVersion", bean.getPlan().getVersion())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("createdBy", bean.getCreatedBy())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(PlanBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("organizationId", bean.getOrganization().getId())
                    .field("organizationName", bean.getOrganization().getName())
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(PlanVersionBean bean) throws StorageException {
        try {
            PlanBean plan = bean.getPlan();
            OrganizationBean org = plan.getOrganization();
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("organizationId", org.getId())
                    .field("organizationName", org.getName())
                    .field("planId", plan.getId())
                    .field("planName", plan.getName())
                    .field("planDescription", plan.getDescription())
                    .field("version", bean.getVersion())
                    .field("status", bean.getStatus())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("modifiedBy", bean.getModifiedBy())
                    .field("modifiedOn", bean.getModifiedOn().getTime())
                    .field("lockedOn", bean.getLockedOn() != null ? bean.getLockedOn().getTime() : null)
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(ServiceBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("organizationId", bean.getOrganization().getId())
                    .field("organizationName", bean.getOrganization().getName())
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("numPublished", bean.getNumPublished())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(ServiceVersionBean bean) throws StorageException {
        try {
            ServiceBean service = bean.getService();
            OrganizationBean org = service.getOrganization();
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("organizationId", org.getId())
                    .field("organizationName", org.getName())
                    .field("serviceId", service.getId())
                    .field("serviceName", service.getName())
                    .field("serviceDescription", service.getDescription())
                    .field("version", bean.getVersion())
                    .field("status", bean.getStatus())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("modifiedBy", bean.getModifiedBy())
                    .field("modifiedOn", bean.getModifiedOn().getTime())
                    .field("publishedOn", bean.getPublishedOn() != null ? bean.getPublishedOn().getTime() : null)
                    .field("retiredOn", bean.getRetiredOn() != null ? bean.getRetiredOn().getTime() : null)
                    .field("publicService", bean.isPublicService())
                    .field("endpoint", bean.getEndpoint())
                    .field("endpointType", bean.getEndpointType())
                    .field("endpointContentType", bean.getEndpointContentType())
                    .field("definitionType", bean.getDefinitionType());
            Set<ServiceGatewayBean> gateways = bean.getGateways();
            if (gateways != null) {
                builder.startArray("gateways");
                for (ServiceGatewayBean gateway : gateways) {
                    builder.startObject()
                        .field("gatewayId", gateway.getGatewayId())
                    .endObject();
                }
                builder.endArray();
            }
            Set<ServicePlanBean> plans = bean.getPlans();
            if (plans != null) {
                builder.startArray("plans");
                for (ServicePlanBean plan : plans) {
                    builder.startObject()
                        .field("planId", plan.getPlanId())
                        .field("version", plan.getVersion())
                    .endObject();
                }
                builder.endArray();
            }
            Map<String, String> endpointProperties = bean.getEndpointProperties();
            if (endpointProperties != null) {
                builder.startObject("endpointProperties");
                for (Entry<String, String> property : endpointProperties.entrySet()) {
                    builder.field(property.getKey(), property.getValue());
                }
                builder.endObject();
            }
            builder.endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(ApplicationBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("organizationId", bean.getOrganization().getId())
                    .field("organizationName", bean.getOrganization().getName())
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(ApplicationVersionBean bean) throws StorageException {
        try {
            ApplicationBean app = bean.getApplication();
            OrganizationBean org = app.getOrganization();
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("organizationId", org.getId())
                    .field("organizationName", org.getName())
                    .field("applicationId", app.getId())
                    .field("applicationName", app.getName())
                    .field("applicationDescription", app.getDescription())
                    .field("version", bean.getVersion())
                    .field("status", bean.getStatus())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("modifiedBy", bean.getModifiedBy())
                    .field("modifiedOn", bean.getModifiedOn().getTime())
                    .field("publishedOn", bean.getPublishedOn() != null ? bean.getPublishedOn().getTime() : null)
                    .field("retiredOn", bean.getRetiredOn() != null ? bean.getRetiredOn().getTime() : null)
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(AuditEntryBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("organizationId", bean.getOrganizationId())
                    .field("entityId", bean.getEntityId())
                    .field("entityType", bean.getEntityType())
                    .field("entityVersion", bean.getEntityVersion())
                    .field("data", bean.getData())
                    .field("who", bean.getWho())
                    .field("what", bean.getWhat())
                    .field("createdOn", bean.getCreatedOn().getTime())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(OrganizationBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("modifiedBy", bean.getModifiedBy())
                    .field("modifiedOn", bean.getModifiedOn().getTime())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(RoleMembershipBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("organizationId", bean.getOrganizationId())
                    .field("roleId", bean.getRoleId())
                    .field("userId", bean.getUserId())
                    .field("createdOn", bean.getCreatedOn().getTime())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(UserBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("username", bean.getUsername())
                    .field("email", bean.getEmail())
                    .field("fullName", bean.getFullName())
                    .field("joinedOn", bean.getJoinedOn() == null ? null : bean.getJoinedOn().getTime())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(RoleBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("autoGrant", bean.getAutoGrant());
            Set<PermissionType> permissions = bean.getPermissions();
            if (permissions != null && !permissions.isEmpty()) {
                builder.array("permissions", permissions.toArray());
            }
            builder.endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(PolicyDefinitionBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("form", bean.getForm())
                    .field("formType", bean.getFormType())
                    .field("icon", bean.getIcon())
                    .field("pluginId", bean.getPluginId())
                    .field("policyImpl", bean.getPolicyImpl())
                    .field("deleted", bean.isDeleted());

            Set<PolicyDefinitionTemplateBean> templates = bean.getTemplates();
            if (templates != null) {
                builder.field("templates").startArray();
                for (PolicyDefinitionTemplateBean template : templates) {
                    builder.startObject();
                    builder.field("language", template.getLanguage());
                    builder.field("template", template.getTemplate());
                    builder.endObject();
                }
                builder.endArray();
            }

            builder.endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(PluginBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("groupId", bean.getGroupId())
                    .field("artifactId", bean.getArtifactId())
                    .field("version", bean.getVersion())
                    .field("classifier", bean.getClassifier())
                    .field("type", bean.getType())
                    .field("deleted", bean.isDeleted())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the bean
     * @return the content builder
     * @throws StorageException when a storage problem occurs while storing a bean
     */
    public static XContentBuilder marshall(DownloadBean bean) throws StorageException {
        try {
            preMarshall(bean);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("type", bean.getType().name())
                    .field("path", bean.getPath())
                    .field("expires", bean.getExpires().getTime())
                .endObject();
            postMarshall(bean);
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the policy beans
     */
    @SuppressWarnings("unchecked")
    public static PoliciesBean unmarshallPolicies(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        PoliciesBean bean = new PoliciesBean();
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setEntityId(asString(source.get("entityId")));
        bean.setEntityVersion(asString(source.get("entityVersion")));
        bean.setType(asEnum(source.get("type"), PolicyType.class));
        List<Map<String, Object>> policies = (List<Map<String, Object>>) source.get("policies");
        if (policies != null) {
            for (Map<String, Object> policyMap : policies) {
                PolicyBean policy = new PolicyBean();
                policy.setOrganizationId(bean.getOrganizationId());
                policy.setEntityId(bean.getEntityId());
                policy.setEntityVersion(bean.getEntityVersion());
                policy.setType(bean.getType());
                policy.setConfiguration(asString(policyMap.get("configuration")));
                policy.setCreatedBy(asString(policyMap.get("createdBy")));
                policy.setCreatedOn(asDate(policyMap.get("createdOn")));
                PolicyDefinitionBean def = new PolicyDefinitionBean();
                def.setId(asString(policyMap.get("definitionId")));
                // Note: this is a placeholder that needs to be resolved later.
                policy.setDefinition(def);
                policy.setId(asLong(policyMap.get("id")));
                policy.setModifiedBy(asString(policyMap.get("modifiedBy")));
                policy.setModifiedOn(asDate(policyMap.get("modifiedOn")));
                policy.setName(asString(policyMap.get("name")));
                policy.setOrderIndex(asInt(policyMap.get("orderIndex")));
                bean.getPolicies().add(policy);
            }
        }
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the gateway bean
     */
    public static GatewayBean unmarshallGateway(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        GatewayBean bean = new GatewayBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setType(asEnum(source.get("type"), GatewayType.class));
        bean.setConfiguration(asString(source.get("configuration")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setModifiedBy(asString(source.get("modifiedBy")));
        bean.setModifiedOn(asDate(source.get("modifiedOn")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the gateway bean
     */
    public static DownloadBean unmarshallDownload(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        DownloadBean bean = new DownloadBean();
        bean.setId(asString(source.get("id")));
        bean.setPath(asString(source.get("path")));
        bean.setType(asEnum(source.get("type"), DownloadType.class));
        bean.setExpires(asDate(source.get("expires")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the service definition
     */
    public static ServiceDefinitionBean unmarshallServiceDefinition(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ServiceDefinitionBean bean = new ServiceDefinitionBean();
        bean.setData(asString(source.get("data")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the contract
     */
    public static ContractBean unmarshallContract(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ContractBean bean = new ContractBean();
        bean.setId(asLong(source.get("id")));
        bean.setApikey(asString(source.get("apiKey")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the contract summary
     */
    public static ContractSummaryBean unmarshallContractSummary(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ContractSummaryBean bean = new ContractSummaryBean();
        bean.setContractId(asLong(source.get("id")));
        bean.setApikey(asString(source.get("apiKey")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setAppOrganizationId(asString(source.get("appOrganizationId")));
        bean.setAppOrganizationName(asString(source.get("appOrganizationName")));
        bean.setAppId(asString(source.get("appId")));
        bean.setAppName(asString(source.get("appName")));
        bean.setAppVersion(asString(source.get("appVersion")));
        bean.setServiceOrganizationId(asString(source.get("serviceOrganizationId")));
        bean.setServiceOrganizationName(asString(source.get("serviceOrganizationName")));
        bean.setServiceId(asString(source.get("serviceId")));
        bean.setServiceName(asString(source.get("serviceName")));
        bean.setServiceVersion(asString(source.get("serviceVersion")));
        bean.setServiceDescription(asString(source.get("serviceDescription")));
        bean.setPlanName(asString(source.get("planName")));
        bean.setPlanId(asString(source.get("planId")));
        bean.setPlanVersion(asString(source.get("planVersion")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the api entry
     */
    public static ApiEntryBean unmarshallApiEntry(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ApiEntryBean bean = new ApiEntryBean();
        bean.setApiKey(asString(source.get("apiKey")));
        bean.setServiceOrgId(asString(source.get("serviceOrganizationId")));
        bean.setServiceOrgName(asString(source.get("serviceOrganizationName")));
        bean.setServiceId(asString(source.get("serviceId")));
        bean.setServiceName(asString(source.get("serviceName")));
        bean.setServiceVersion(asString(source.get("serviceVersion")));
        bean.setPlanName(asString(source.get("planName")));
        bean.setPlanId(asString(source.get("planId")));
        bean.setPlanVersion(asString(source.get("planVersion")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the plan
     */
    public static PlanBean unmarshallPlan(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        PlanBean bean = new PlanBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the plan summary
     */
    public static PlanSummaryBean unmarshallPlanSummary(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        PlanSummaryBean bean = new PlanSummaryBean();
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setOrganizationName(asString(source.get("organizationName")));
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the plan version
     */
    public static PlanVersionBean unmarshallPlanVersion(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        PlanVersionBean bean = new PlanVersionBean();
        bean.setVersion(asString(source.get("version")));
        bean.setStatus(asEnum(source.get("status"), PlanStatus.class));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setModifiedBy(asString(source.get("modifiedBy")));
        bean.setModifiedOn(asDate(source.get("modifiedOn")));
        bean.setLockedOn(asDate(source.get("lockedOn")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the plan version summary
     */
    public static PlanVersionSummaryBean unmarshallPlanVersionSummary(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        PlanVersionSummaryBean bean = new PlanVersionSummaryBean();
        bean.setDescription(asString(source.get("planDescription")));
        bean.setId(asString(source.get("planId")));
        bean.setName(asString(source.get("planName")));
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setOrganizationName(asString(source.get("organizationName")));
        bean.setStatus(asEnum(source.get("status"), PlanStatus.class));
        bean.setVersion(asString(source.get("version")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the service
     */
    public static ServiceBean unmarshallService(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ServiceBean bean = new ServiceBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setNumPublished(asInt(source.get("numPublished")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the service summary
     */
    public static ServiceSummaryBean unmarshallServiceSummary(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ServiceSummaryBean bean = new ServiceSummaryBean();
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setOrganizationName(asString(source.get("organizationName")));
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the service version
     */
    @SuppressWarnings("unchecked")
    public static ServiceVersionBean unmarshallServiceVersion(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ServiceVersionBean bean = new ServiceVersionBean();
        bean.setVersion(asString(source.get("version")));
        bean.setStatus(asEnum(source.get("status"), ServiceStatus.class));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setModifiedBy(asString(source.get("modifiedBy")));
        bean.setModifiedOn(asDate(source.get("modifiedOn")));
        bean.setPublishedOn(asDate(source.get("publishedOn")));
        bean.setRetiredOn(asDate(source.get("retiredOn")));
        bean.setEndpoint(asString(source.get("endpoint")));
        bean.setEndpointType(asEnum(source.get("endpointType"), EndpointType.class));
        bean.setEndpointContentType(asEnum(source.get("endpointContentType"), EndpointContentType.class));
        bean.setPublicService(asBoolean(source.get("publicService")));
        bean.setDefinitionType(asEnum(source.get("definitionType"), ServiceDefinitionType.class));
        bean.setGateways(new HashSet<ServiceGatewayBean>());
        List<Map<String, Object>> gateways = (List<Map<String, Object>>) source.get("gateways");
        if (gateways != null) {
            for (Map<String, Object> gatewayMap : gateways) {
                ServiceGatewayBean gatewayBean = new ServiceGatewayBean();
                gatewayBean.setGatewayId(asString(gatewayMap.get("gatewayId")));
                bean.getGateways().add(gatewayBean);
            }
        }
        bean.setPlans(new HashSet<ServicePlanBean>());
        List<Map<String, Object>> plans = (List<Map<String, Object>>) source.get("plans");
        if (plans != null) {
            for (Map<String, Object> planMap : plans) {
                ServicePlanBean planBean = new ServicePlanBean();
                planBean.setPlanId(asString(planMap.get("planId")));
                planBean.setVersion(asString(planMap.get("version")));
                bean.getPlans().add(planBean);
            }
        }
        Map<String, Object> endpointProperties = (Map<String, Object>) source.get("endpointProperties");
        if (endpointProperties != null) {
            bean.setEndpointProperties(new HashMap<String, String>());
            for (Entry<String, Object> entry : endpointProperties.entrySet()) {
                bean.getEndpointProperties().put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the service veresion summary
     */
    public static ServiceVersionSummaryBean unmarshallServiceVersionSummary(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ServiceVersionSummaryBean bean = new ServiceVersionSummaryBean();
        bean.setDescription(asString(source.get("serviceDescription")));
        bean.setId(asString(source.get("serviceId")));
        bean.setName(asString(source.get("serviceName")));
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setOrganizationName(asString(source.get("organizationName")));
        bean.setStatus(asEnum(source.get("status"), ServiceStatus.class));
        bean.setVersion(asString(source.get("version")));
        bean.setPublicService(asBoolean(source.get("publicService")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the application
     */
    public static ApplicationBean unmarshallApplication(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ApplicationBean bean = new ApplicationBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the application summary
     */
    public static ApplicationSummaryBean unmarshallApplicationSummary(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ApplicationSummaryBean bean = new ApplicationSummaryBean();
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setOrganizationName(asString(source.get("organizationName")));
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the application version
     */
    public static ApplicationVersionBean unmarshallApplicationVersion(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ApplicationVersionBean bean = new ApplicationVersionBean();
        bean.setVersion(asString(source.get("version")));
        bean.setStatus(asEnum(source.get("status"), ApplicationStatus.class));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setModifiedBy(asString(source.get("modifiedBy")));
        bean.setModifiedOn(asDate(source.get("modifiedOn")));
        bean.setPublishedOn(asDate(source.get("publishedOn")));
        bean.setRetiredOn(asDate(source.get("retiredOn")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the application version summary
     */
    public static ApplicationVersionSummaryBean unmarshallApplicationVersionSummary(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ApplicationVersionSummaryBean bean = new ApplicationVersionSummaryBean();
        bean.setDescription(asString(source.get("applicationDescription")));
        bean.setId(asString(source.get("applicationId")));
        bean.setName(asString(source.get("applicationName")));
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setOrganizationName(asString(source.get("organizationName")));
        bean.setStatus(asEnum(source.get("status"), ApplicationStatus.class));
        bean.setVersion(asString(source.get("version")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the role
     */
    public static RoleBean unmarshallRole(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        RoleBean bean = new RoleBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setAutoGrant(asBoolean(source.get("autoGrant")));
        @SuppressWarnings("unchecked")
        List<Object> permissions = (List<Object>) source.get("permissions");
        if (permissions != null && !permissions.isEmpty()) {
            bean.setPermissions(new HashSet<PermissionType>());
            for (Object permission : permissions) {
                bean.getPermissions().add(asEnum(permission, PermissionType.class));
            }
        }
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the user
     */
    public static UserBean unmarshallUser(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        UserBean bean = new UserBean();
        bean.setUsername(asString(source.get("username")));
        bean.setEmail(asString(source.get("email")));
        bean.setFullName(asString(source.get("fullName")));
        bean.setJoinedOn(asDate(source.get("joinedOn")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the organization
     */
    public static OrganizationBean unmarshallOrganization(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        OrganizationBean bean = new OrganizationBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setModifiedOn(asDate(source.get("modifiedOn")));
        bean.setModifiedBy(asString(source.get("modifiedBy")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the organization summary
     */
    public static OrganizationSummaryBean unmarshallOrganizationSummary(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        OrganizationSummaryBean bean = new OrganizationSummaryBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the role membership
     */
    public static RoleMembershipBean unmarshallRoleMembership(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        RoleMembershipBean bean = new RoleMembershipBean();
        bean.setId(asLong(source.get("id")));
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setRoleId(asString(source.get("roleId")));
        bean.setUserId(asString(source.get("userId")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the audit entry
     */
    public static AuditEntryBean unmarshallAuditEntry(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        AuditEntryBean bean = new AuditEntryBean();
        bean.setId(asLong(source.get("id")));
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setData(asString(source.get("data")));
        bean.setEntityId(asString(source.get("entityId")));
        bean.setEntityType(asEnum(source.get("entityType"), AuditEntityType.class));
        bean.setEntityVersion(asString(source.get("entityVersion")));
        bean.setWhat(asEnum(source.get("what"), AuditEntryType.class));
        bean.setWho(asString(source.get("who")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param map the search hit map
     * @return the gateway summary
     */
    public static GatewaySummaryBean unmarshallGatewaySummary(Map<String, Object> map) {
        GatewaySummaryBean bean = new GatewaySummaryBean();
        bean.setId(asString(map.get("id")));
        bean.setName(asString(map.get("name")));
        if (map.containsKey("description")) {
            bean.setDescription(asString(map.get("description")));
        }
        bean.setType(asEnum(map.get("type"), GatewayType.class));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the policy definition
     */
    public static PolicyDefinitionBean unmarshallPolicyDefinition(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        PolicyDefinitionBean bean = new PolicyDefinitionBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setForm(asString(source.get("form")));
        bean.setFormType(asEnum(source.get("formType"), PolicyFormType.class));
        bean.setIcon(asString(source.get("icon")));
        bean.setPluginId(asLong(source.get("pluginId")));
        bean.setPolicyImpl(asString(source.get("policyImpl")));
        bean.setDeleted(asBoolean(source.get("deleted")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> templates = (List<Map<String, Object>>) source.get("templates");
        if (templates != null && !templates.isEmpty()) {
            bean.setTemplates(new HashSet<PolicyDefinitionTemplateBean>());
            for (Map<String, Object> templateMap : templates) {
                PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
                template.setLanguage(asString(templateMap.get("language")));
                template.setTemplate(asString(templateMap.get("template")));
                bean.getTemplates().add(template);
            }
        }
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param map the search map
     * @return the policy definition summary
     */
    public static PolicyDefinitionSummaryBean unmarshallPolicyDefinitionSummary(Map<String, Object> map) {
        PolicyDefinitionSummaryBean bean = new PolicyDefinitionSummaryBean();
        bean.setId(asString(map.get("id")));
        bean.setName(asString(map.get("name")));
        if (map.containsKey("description")) {
            bean.setDescription(asString(map.get("description")));
        }
        bean.setPolicyImpl(asString(map.get("policyImpl")));
        if (map.containsKey("icon")) {
            bean.setIcon(asString(map.get("icon")));
        }
        if (map.containsKey("pluginId")) {
            bean.setPluginId(asLong(map.get("pluginId")));
        }
        if (map.containsKey("formType")) {
            bean.setFormType(asEnum(map.get("formType"), PolicyFormType.class));
        }

        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source
     * @return the plugin
     */
    public static PluginBean unmarshallPlugin(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        PluginBean bean = new PluginBean();
        bean.setId(asLong(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setGroupId(asString(source.get("groupId")));
        bean.setArtifactId(asString(source.get("artifactId")));
        bean.setVersion(asString(source.get("version")));
        bean.setType(asString(source.get("type")));
        bean.setClassifier(asString(source.get("classifier")));
        bean.setDeleted(asBoolean(source.get("deleted")));
        postMarshall(bean);
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param map the search hit map
     * @return the plugin summary
     */
    public static PluginSummaryBean unmarshallPluginSummary(Map<String, Object> map) {
        PluginSummaryBean bean = new PluginSummaryBean();
        bean.setId(asLong(map.get("id")));
        bean.setName(asString(map.get("name")));
        if (map.containsKey("description")) {
            bean.setDescription(asString(map.get("description")));
        }
        bean.setGroupId(asString(map.get("groupId")));
        bean.setArtifactId(asString(map.get("artifactId")));
        bean.setVersion(asString(map.get("version")));
        if (map.containsKey("type")) {
            bean.setType(asString(map.get("type")));
        }
        if (map.containsKey("classifier")) {
            bean.setClassifier(asString(map.get("classifier")));
        }
        bean.setCreatedBy(asString(map.get("createdBy")));
        bean.setCreatedOn(asDate(map.get("createdOn")));

        postMarshall(bean);
        return bean;
    }

    /**
     * @param object
     */
    private static String asString(Object object) {
        if (object == null) {
            return null;
        }
        return String.valueOf(object);
    }

    /**
     * @param object
     */
    private static Long asLong(Object object) {
        if (object == null) {
            return null;
        }
        Number n = (Number) object;
        return n.longValue();
    }

    /**
     * @param object
     */
    private static Integer asInt(Object object) {
        if (object == null) {
            return null;
        }
        Number n = (Number) object;
        return n.intValue();
    }

    /**
     * @param object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> T asEnum(Object object, Class<T> type) {
        if (object == null) {
            return null;
        }
        return (T) Enum.valueOf((Class) type, String.valueOf(object));
    }

    /**
     * @param object
     */
    private static Date asDate(Object object) {
        if (object == null) {
            return null;
        }
        Number n = (Number) object;
        return new Date(n.longValue());
    }

    /**
     * @param object
     */
    private static Boolean asBoolean(Object object) {
        if (object == null) {
            return null;
        }
        return (Boolean) object;
    }

    /**
     * Called before marshalling the bean to a form that will be used
     * for storage in the DB.
     * @param bean
     */
    private static void preMarshall(Object bean) {
        try {
            Method method = bean.getClass().getDeclaredMethod("encryptData");
            if (method != null) {
                method.invoke(bean);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
        }
    }

    /**
     * Called after marshalling (or unmarshalling).
     * @param bean
     */
    private static void postMarshall(Object bean) {
        try {
            Method method = bean.getClass().getDeclaredMethod("decryptData");
            if (method != null) {
                method.invoke(bean);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
        }
    }

}
