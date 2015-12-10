/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.manager.api.rest.impl.audit;

import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.AuditEntryType;
import io.apiman.manager.api.beans.audit.data.ContractData;
import io.apiman.manager.api.beans.audit.data.EntityUpdatedData;
import io.apiman.manager.api.beans.audit.data.MembershipData;
import io.apiman.manager.api.beans.audit.data.PolicyData;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Contains a number of methods useful to create and manage audit entries for
 * actions taken by users in the management layer REST API.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Returns true only if the value changed.
     * @param before the value before change
     * @param after the value after change
     * @return true if value changed, else false
     */
    public static boolean valueChanged(String before, String after) {
        if ((before == null && after == null) || after == null) {
            return false;
        }

        if (before == null) {
            return true;
        }

        return !before.trim().equals(after.trim());
    }

    /**
     * Returns true only if the value changed.
     * @param before the value before change
     * @param after the value after change
     * @return true if value changed, else false
     */
    public static boolean valueChanged(Boolean before, Boolean after) {
        if ((before == null && after == null) || after == null) {
            return false;
        }

        if (before == null) {
            return true;
        }

        return !before.equals(after);
    }

    /**
     * Returns true only if the set has changed.
     *
     * @param before the value before change
     * @param after the value after change
     * @return true if value changed, else false
     */
    public static boolean valueChanged(Set<?> before, Set<?> after) {
        if ((before == null && after == null) || after == null) {
            return false;
        }
        if (before == null) {
            if (after.isEmpty()) {
                return false;
            } else {
                return true;
            }
        } else {
            if (before.size() != after.size()) {
                return true;
            }
            for (Object bean : after) {
                if (!before.contains(bean)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true only if the map has changed.
     *
     * @param before the value before change
     * @param after the value after change
     * @return true if value changed, else false
     */
    public static boolean valueChanged(Map<String, String> before, Map<String, String> after) {
        if ((before == null && after == null) || after == null) {
            return false;
        }
        if (before == null) {
            if (after.isEmpty()) {
                return false;
            } else {
                return true;
            }
        } else {
            if (before.size() != after.size()) {
                return true;
            }
            for (Entry<String, String> entry : after.entrySet()) {
                String key =  entry.getKey();
                String afterValue = entry.getValue();
                if (!before.containsKey(key)) {
                    return true;
                }
                String beforeValue = before.get(key);
                if (valueChanged(beforeValue, afterValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true only if the value changed.
     * @param before the value before change
     * @param after the value after change
     * @return true if value changed, else false
     */
    public static boolean valueChanged(Enum<?> before, Enum<?> after) {
        if (before == null && after == null) {
            return false;
        }
        if (after == null) {
            return false;
        }
        if (before == null && after != null) {
            return true;
        }
        return !(before == after);
    }

    /**
     * Creates an {@link AuditEntryBean} for the 'organization created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean organizationCreated(OrganizationBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getId(), AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'organization updated' event.
     * @param bean the bean
     * @param data the update
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean organizationUpdated(OrganizationBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getId(), AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'membership granted' even.
     * @param organizationId the organization id
     * @param data the membership data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean membershipGranted(String organizationId, MembershipData data,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(organizationId, AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Grant);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'membership revoked' even.
     * @param organizationId the organization id
     * @param data the membership data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean membershipRevoked(String organizationId, MembershipData data,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(organizationId, AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Revoke);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'API created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean apiCreated(ApiBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setData(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'API updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean apiUpdated(ApiBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'API version created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean apiVersionCreated(ApiVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApi().getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setEntityId(bean.getApi().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'API version updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean apiVersionUpdated(ApiVersionBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getApi().getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setEntityId(bean.getApi().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry when a API definition is updated.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean apiDefinitionUpdated(ApiVersionBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApi().getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setEntityId(bean.getApi().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.UpdateDefinition);
        return entry;
    }

    /**
     * Creates an audit entry when a API definition is deleted.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean apiDefinitionDeleted(ApiVersionBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApi().getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setEntityId(bean.getApi().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.DeleteDefinition);
        return entry;
    }

    /**
     * Creates an audit entry for the 'client created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean clientCreated(ClientBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Client, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setData(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'client updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean clientUpdated(ClientBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Client, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'client version created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean clientVersionCreated(ClientVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getClient().getOrganization().getId(), AuditEntityType.Client, securityContext);
        entry.setEntityId(bean.getClient().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'client version updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean clientVersionUpdated(ClientVersionBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getClient().getOrganization().getId(), AuditEntityType.Client, securityContext);
        entry.setEntityId(bean.getClient().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean contractCreatedFromClient(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getClient().getClient().getOrganization().getId(), AuditEntityType.Client, securityContext);
        entry.setWhat(AuditEntryType.CreateContract);
        entry.setEntityId(bean.getClient().getClient().getId());
        entry.setEntityVersion(bean.getClient().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean contractCreatedToApi(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApi().getApi().getOrganization().getId(), AuditEntityType.Api, securityContext);
        // Ensure the order of contract-created events are deterministic by adding 1 ms to this one
        entry.setCreatedOn(new Date(entry.getCreatedOn().getTime() + 1));
        entry.setWhat(AuditEntryType.CreateContract);
        entry.setEntityId(bean.getApi().getApi().getId());
        entry.setEntityVersion(bean.getApi().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract broken' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean contractBrokenFromClient(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getClient().getClient().getOrganization().getId(), AuditEntityType.Client, securityContext);
        entry.setWhat(AuditEntryType.BreakContract);
        entry.setEntityId(bean.getClient().getClient().getId());
        entry.setEntityVersion(bean.getClient().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract broken' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean contractBrokenToApi(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApi().getApi().getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setWhat(AuditEntryType.BreakContract);
        entry.setEntityId(bean.getApi().getApi().getId());
        entry.setEntityVersion(bean.getApi().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'policy added' event.  Works for all
     * three kinds of policies.
     * @param bean the bean
     * @param type the policy type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policyAdded(PolicyBean bean, PolicyType type,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), null, securityContext);
        entry.setWhat(AuditEntryType.AddPolicy);
        entry.setEntityId(bean.getEntityId());
        entry.setEntityVersion(bean.getEntityVersion());
        switch (type) {
        case Client:
            entry.setEntityType(AuditEntityType.Client);
            break;
        case Plan:
            entry.setEntityType(AuditEntityType.Plan);
            break;
        case Api:
            entry.setEntityType(AuditEntityType.Api);
            break;
        }
        PolicyData data = new PolicyData();
        data.setPolicyDefId(bean.getDefinition().getId());
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'policy removed' event.  Works for all
     * three kinds of policies.
     * @param bean the bean
     * @param type the policy type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policyRemoved(PolicyBean bean, PolicyType type,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), null, securityContext);
        entry.setWhat(AuditEntryType.RemovePolicy);
        entry.setEntityId(bean.getEntityId());
        entry.setEntityVersion(bean.getEntityVersion());
        switch (type) {
        case Client:
            entry.setEntityType(AuditEntityType.Client);
            break;
        case Plan:
            entry.setEntityType(AuditEntityType.Plan);
            break;
        case Api:
            entry.setEntityType(AuditEntityType.Api);
            break;
        }
        PolicyData data = new PolicyData();
        data.setPolicyDefId(bean.getDefinition().getId());
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'policy updated' event.  Works for all
     * three kinds of policies.
     * @param bean the bean
     * @param type the policy type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policyUpdated(PolicyBean bean, PolicyType type,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), null, securityContext);
        entry.setWhat(AuditEntryType.UpdatePolicy);
        entry.setEntityId(bean.getEntityId());
        entry.setEntityVersion(bean.getEntityVersion());
        switch (type) {
        case Client:
            entry.setEntityType(AuditEntityType.Client);
            break;
        case Plan:
            entry.setEntityType(AuditEntityType.Plan);
            break;
        case Api:
            entry.setEntityType(AuditEntityType.Api);
            break;
        }
        PolicyData data = new PolicyData();
        data.setPolicyDefId(bean.getDefinition().getId());
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Writes the data object as a JSON string.
     * @param data
     */
    private static String toJSON(Object data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an audit entry for the 'plan created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planCreated(PlanBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setData(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planUpdated(PlanBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan version created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planVersionCreated(PlanVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getPlan().getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getPlan().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan version updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planVersionUpdated(PlanVersionBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getPlan().getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getPlan().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'API published' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean apiPublished(ApiVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApi().getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setEntityId(bean.getApi().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Publish);
        return entry;
    }

    /**
     * Creates an audit entry for the 'API retired' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean apiRetired(ApiVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApi().getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setEntityId(bean.getApi().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Retire);
        return entry;
    }

    /**
     * Creates an audit entry for the 'client registered' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean clientRegistered(ClientVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getClient().getOrganization().getId(), AuditEntityType.Client, securityContext);
        entry.setEntityId(bean.getClient().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Register);
        return entry;
    }

    /**
     * Creates an audit entry for the 'client unregistered' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean clientUnregistered(ClientVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getClient().getOrganization().getId(), AuditEntityType.Client, securityContext);
        entry.setEntityId(bean.getClient().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Unregister);
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan locked' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planLocked(PlanVersionBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getPlan().getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getPlan().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Lock);
        return entry;
    }

    /**
     * Called when the user reorders the policies in a API.
     * @param apiVersion the API version
     * @param policyType the policy type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policiesReordered(ApiVersionBean apiVersion, PolicyType policyType,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(apiVersion.getApi().getOrganization().getId(), AuditEntityType.Api, securityContext);
        entry.setEntityId(apiVersion.getApi().getId());
        entry.setEntityVersion(apiVersion.getVersion());
        entry.setWhat(AuditEntryType.ReorderPolicies);
        return entry;
    }

    /**
     * Called when the user reorders the policies in an client.
     * @param cvb the client and version
     * @param policyType the policy type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policiesReordered(ClientVersionBean cvb, PolicyType policyType,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(cvb.getClient().getOrganization().getId(), AuditEntityType.Client, securityContext);
        entry.setEntityId(cvb.getClient().getId());
        entry.setEntityVersion(cvb.getVersion());
        entry.setWhat(AuditEntryType.ReorderPolicies);
        return entry;
    }

    /**
     * Called when the user reorders the policies in a plan.
     * @param pvb the plan and version
     * @param policyType the policy type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policiesReordered(PlanVersionBean pvb, PolicyType policyType,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(pvb.getPlan().getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(pvb.getPlan().getId());
        entry.setEntityVersion(pvb.getVersion());
        entry.setWhat(AuditEntryType.ReorderPolicies);
        return entry;
    }

    /**
     * Creates an audit entry.
     * @param orgId the organization id
     * @param type
     * @param securityContext the security context
     * @return the audit entry
     */
    private static AuditEntryBean newEntry(String orgId, AuditEntityType type, ISecurityContext securityContext) {
        // Wait for 1 ms to guarantee that two audit entries are never created at the same moment in time (which would
        // result in non-deterministic sorting by the storage layer)
        try { Thread.sleep(1); } catch (InterruptedException e) { throw new RuntimeException(e); }

        AuditEntryBean entry = new AuditEntryBean();
        entry.setOrganizationId(orgId);
        entry.setEntityType(type);
        entry.setCreatedOn(new Date());
        entry.setWho(securityContext.getCurrentUser());
        return entry;
    }

    /**
     * Converts the list of plans to a string for display/comparison.
     * @param plans the plans
     * @return the plans as a string
     */
    public static String asString_ApiPlanBeans(Set<ApiPlanBean> plans) {
        TreeSet<ApiPlanBean> sortedPlans = new TreeSet<>(new Comparator<ApiPlanBean>() {
            @Override
            public int compare(ApiPlanBean o1, ApiPlanBean o2) {
                String p1 = o1.getPlanId() + ":" + o1.getVersion(); //$NON-NLS-1$
                String p2 = o2.getPlanId() + ":" + o2.getVersion(); //$NON-NLS-1$
                return p1.compareTo(p2);
            }
        });
        sortedPlans.addAll(plans);

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        if (plans != null) {
            for (ApiPlanBean plan : sortedPlans) {
                if (!first) {
                    builder.append(", "); //$NON-NLS-1$
                }
                builder.append(plan.getPlanId()).append(":").append(plan.getVersion()); //$NON-NLS-1$
                first = false;
            }
        }
        return builder.toString();
    }


    /**
     * Converts the list of gateways to a string for display/comparison.
     * @param gateways set of gateways
     * @return the gateways as a string
     */
    public static String asString_ApiGatewayBeans(Set<ApiGatewayBean> gateways) {
        TreeSet<ApiGatewayBean> sortedGateways = new TreeSet<>(new Comparator<ApiGatewayBean>() {
            @Override
            public int compare(ApiGatewayBean o1, ApiGatewayBean o2) {
                return o1.getGatewayId().compareTo(o2.getGatewayId());
            }
        });
        sortedGateways.addAll(gateways);

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        if (gateways != null) {
            for (ApiGatewayBean gateway : sortedGateways) {
                if (!first) {
                    builder.append(", "); //$NON-NLS-1$
                }
                builder.append(gateway.getGatewayId());
                first = false;
            }
        }
        return builder.toString();
    }

}
