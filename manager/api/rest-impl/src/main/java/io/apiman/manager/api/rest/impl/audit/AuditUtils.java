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

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.AuditEntryType;
import io.apiman.manager.api.beans.audit.data.ContractData;
import io.apiman.manager.api.beans.audit.data.EntityUpdatedData;
import io.apiman.manager.api.beans.audit.data.MembershipData;
import io.apiman.manager.api.beans.audit.data.PolicyData;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceGatewayBean;
import io.apiman.manager.api.beans.services.ServicePlanBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Date;
import java.util.Set;

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
     * @param before
     * @param after
     */
    public static boolean valueChanged(String before, String after) {
        if (before == null && after == null) {
            return false;
        }
        if (after == null) {
            return false;
        }
        if (before == null && after != null) {
            return true;
        }
        return !before.trim().equals(after.trim());
    }

    /**
     * Returns true only if the set has changed.
     * @param before
     * @param after
     */
    public static boolean valueChanged(Set<?> before, Set<?> after) {
        if (after == null) {
            return false;
        }
        if (before == null && after.isEmpty()) {
            return false;
        }
        if (before == null && !after.isEmpty()) {
            return true;
        }
        if (before.size() != after.size()) {
            return true;
        }
        for (Object bean : after) {
            if (!before.contains(bean)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true only if the value changed.
     * @param before
     * @param after
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
     * @param bean
     * @param securityContext
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
     * @param bean
     * @param data
     * @param securityContext
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
     * @param organizationId
     * @param securityContext
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
     * @param organizationId
     * @param securityContext
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
     * Creates an audit entry for the 'service created' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean serviceCreated(ServiceBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setData(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'service updated' event.
     * @param bean
     * @param data
     * @param securityContext
     */
    public static AuditEntryBean serviceUpdated(ServiceBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'service version created' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean serviceVersionCreated(ServiceVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getOrganizationId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'service version updated' event.
     * @param bean
     * @param data
     * @param securityContext
     */
    public static AuditEntryBean serviceVersionUpdated(ServiceVersionBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getService().getOrganizationId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'application created' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean applicationCreated(ApplicationBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setData(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'application updated' event.
     * @param bean
     * @param data
     * @param securityContext
     */
    public static AuditEntryBean applicationUpdated(ApplicationBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'application version created' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean applicationVersionCreated(ApplicationVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getOrganizationId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getApplication().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'application version updated' event.
     * @param bean
     * @param data
     * @param securityContext
     */
    public static AuditEntryBean applicationVersionUpdated(ApplicationVersionBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getApplication().getOrganizationId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getApplication().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract created' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean contractCreatedFromApp(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getApplication().getOrganizationId(), AuditEntityType.Application, securityContext);
        entry.setWhat(AuditEntryType.CreateContract);
        entry.setEntityId(bean.getApplication().getApplication().getId());
        entry.setEntityVersion(bean.getApplication().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract created' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean contractCreatedToService(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getService().getOrganizationId(), AuditEntityType.Service, securityContext);
        // Ensure the order of contract-created events are deterministic by adding 1 ms to this one
        entry.setWhen(new Date(entry.getWhen().getTime() + 1));
        entry.setWhat(AuditEntryType.CreateContract);
        entry.setEntityId(bean.getService().getService().getId());
        entry.setEntityVersion(bean.getService().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract broken' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean contractBrokenFromApp(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getApplication().getOrganizationId(), AuditEntityType.Application, securityContext);
        entry.setWhat(AuditEntryType.BreakContract);
        entry.setEntityId(bean.getApplication().getApplication().getId());
        entry.setEntityVersion(bean.getApplication().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract broken' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean contractBrokenToService(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getService().getOrganizationId(), AuditEntityType.Service, securityContext);
        entry.setWhat(AuditEntryType.BreakContract);
        entry.setEntityId(bean.getService().getService().getId());
        entry.setEntityVersion(bean.getService().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'policy added' event.  Works for all
     * three kinds of policies.
     * @param bean
     * @param type
     * @param securityContext
     */
    public static AuditEntryBean policyAdded(PolicyBean bean, PolicyType type,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), null, securityContext);
        entry.setWhat(AuditEntryType.AddPolicy);
        entry.setEntityId(bean.getEntityId());
        entry.setEntityVersion(bean.getEntityVersion());
        switch (type) {
        case Application:
            entry.setEntityType(AuditEntityType.Application);
            break;
        case Plan:
            entry.setEntityType(AuditEntityType.Plan);
            break;
        case Service:
            entry.setEntityType(AuditEntityType.Service);
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
     * @param bean
     * @param type
     * @param securityContext
     */
    public static AuditEntryBean policyRemoved(PolicyBean bean, PolicyType type,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), null, securityContext);
        entry.setWhat(AuditEntryType.RemovePolicy);
        entry.setEntityId(bean.getEntityId());
        entry.setEntityVersion(bean.getEntityVersion());
        switch (type) {
        case Application:
            entry.setEntityType(AuditEntityType.Application);
            break;
        case Plan:
            entry.setEntityType(AuditEntityType.Plan);
            break;
        case Service:
            entry.setEntityType(AuditEntityType.Service);
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
     * @param bean
     * @param type
     * @param securityContext
     */
    public static AuditEntryBean policyUpdated(PolicyBean bean, PolicyType type,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), null, securityContext);
        entry.setWhat(AuditEntryType.UpdatePolicy);
        entry.setEntityId(bean.getEntityId());
        entry.setEntityVersion(bean.getEntityVersion());
        switch (type) {
        case Application:
            entry.setEntityType(AuditEntityType.Application);
            break;
        case Plan:
            entry.setEntityType(AuditEntityType.Plan);
            break;
        case Service:
            entry.setEntityType(AuditEntityType.Service);
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
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean planCreated(PlanBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setData(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan updated' event.
     * @param bean
     * @param data
     * @param securityContext
     */
    public static AuditEntryBean planUpdated(PlanBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan version created' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean planVersionCreated(PlanVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getPlan().getOrganizationId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getPlan().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan version updated' event.
     * @param bean
     * @param data
     * @param securityContext
     */
    public static AuditEntryBean planVersionUpdated(PlanVersionBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getPlan().getOrganizationId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getPlan().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'service published' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean servicePublished(ServiceVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getOrganizationId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Publish);
        return entry;
    }

    /**
     * Creates an audit entry for the 'service retired' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean serviceRetired(ServiceVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getOrganizationId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Retire);
        return entry;
    }

    /**
     * Creates an audit entry for the 'application registered' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean applicationRegistered(ApplicationVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getOrganizationId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getApplication().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Register);
        return entry;
    }

    /**
     * Creates an audit entry for the 'application unregistered' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean applicationUnregistered(ApplicationVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getOrganizationId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getApplication().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Unregister);
        return entry;
    }

    /**
     * Called when the user reorders the policies in a service.
     * @param svb
     * @param service
     * @param securityContext
     */
    public static AuditEntryBean policiesReordered(ServiceVersionBean svb, PolicyType service,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(svb.getService().getOrganizationId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(svb.getService().getId());
        entry.setEntityVersion(svb.getVersion());
        entry.setWhat(AuditEntryType.ReorderPolicies);
        return entry;
    }

    /**
     * Called when the user reorders the policies in an application.
     * @param avb
     * @param service
     * @param securityContext
     */
    public static AuditEntryBean policiesReordered(ApplicationVersionBean avb, PolicyType service,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(avb.getApplication().getOrganizationId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(avb.getApplication().getId());
        entry.setEntityVersion(avb.getVersion());
        entry.setWhat(AuditEntryType.ReorderPolicies);
        return entry;
    }

    /**
     * Called when the user reorders the policies in a plan.
     * @param pvb
     * @param service
     * @param securityContext
     */
    public static AuditEntryBean policiesReordered(PlanVersionBean pvb, PolicyType service,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(pvb.getPlan().getOrganizationId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(pvb.getPlan().getId());
        entry.setEntityVersion(pvb.getVersion());
        entry.setWhat(AuditEntryType.ReorderPolicies);
        return entry;
    }
    
    /**
     * Creates an audit entry.
     * @param orgId
     * @param type
     * @param securityContext
     */
    private static AuditEntryBean newEntry(String orgId, AuditEntityType type, ISecurityContext securityContext) {
        AuditEntryBean entry = new AuditEntryBean();
        entry.setOrganizationId(orgId);
        entry.setEntityType(type);
        entry.setWhen(new Date());
        entry.setWho(securityContext.getCurrentUser());
        return entry;
    }

    /**
     * Converts the list of plans to a string for display/comparison.
     * @param plans
     */
    public static String asString_ServicePlanBeans(Set<ServicePlanBean> plans) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        if (plans != null) {
            for (ServicePlanBean plan : plans) {
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
     * @param plans
     */
    public static String asString_ServiceGatewayBeans(Set<ServiceGatewayBean> gateways) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        if (gateways != null) {
            for (ServiceGatewayBean gateway : gateways) {
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
