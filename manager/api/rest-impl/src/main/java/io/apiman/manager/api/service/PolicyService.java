package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.NewPolicyBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.util.PolicyTemplateUtil;
import io.apiman.manager.api.rest.exceptions.PolicyDefinitionNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class PolicyService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(PolicyService.class);

    private IStorage storage;
    private IStorageQuery query;
    private ISecurityContext securityContext;

    @Inject
    public PolicyService(
        IStorage storage,
        IStorageQuery query,
        ISecurityContext securityContext
    ) {
        this.storage = storage;
        this.query = query;
        this.securityContext = securityContext;
    }

    public PolicyService() {
    }

    /**
     * Creates a policy for the given entity (supports creating policies for clients,
     * APIs, and plans).
     *
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param bean
     * @return the stored policy bean (with updated information)
     */
    public PolicyBean createPolicy(String organizationId, String entityId, String entityVersion,
        NewPolicyBean bean, PolicyType type) throws PolicyDefinitionNotFoundException {

        return tryAction(() -> {
            if (bean.getDefinitionId() == null) {
                throw ExceptionFactory.policyDefNotFoundException("null"); //$NON-NLS-1$
            }
            PolicyDefinitionBean def = storage.getPolicyDefinition(bean.getDefinitionId());

            if (def == null) {
                throw ExceptionFactory.policyDefNotFoundException(bean.getDefinitionId());
            }

            int newIdx = query.getMaxPolicyOrderIndex(organizationId, entityId, entityVersion, type) + 1;

            PolicyBean policy = new PolicyBean();
            policy.setId(null);
            policy.setDefinition(def);
            policy.setName(def.getName());
            policy.setConfiguration(bean.getConfiguration());
            policy.setCreatedBy(securityContext.getCurrentUser());
            policy.setCreatedOn(new Date());
            policy.setModifiedBy(securityContext.getCurrentUser());
            policy.setModifiedOn(new Date());
            policy.setOrganizationId(organizationId);
            policy.setEntityId(entityId);
            policy.setEntityVersion(entityVersion);
            policy.setType(type);
            policy.setOrderIndex(newIdx);

            if (type == PolicyType.Client) {
                ClientVersionBean cvb = storage.getClientVersion(organizationId, entityId, entityVersion);
                cvb.setModifiedBy(securityContext.getCurrentUser());
                cvb.setModifiedOn(new Date());
                storage.updateClientVersion(cvb);
            } else if (type == PolicyType.Api) {
                ApiVersionBean avb = storage.getApiVersion(organizationId, entityId, entityVersion);
                avb.setModifiedBy(securityContext.getCurrentUser());
                avb.setModifiedOn(new Date());
                storage.updateApiVersion(avb);
            } else if (type == PolicyType.Plan) {
                PlanVersionBean pvb = storage.getPlanVersion(organizationId, entityId, entityVersion);
                pvb.setModifiedBy(securityContext.getCurrentUser());
                pvb.setModifiedOn(new Date());
                storage.updatePlanVersion(pvb);
            }

            storage.createPolicy(policy);
            storage.createAuditEntry(AuditUtils.policyAdded(policy, type, securityContext));

            PolicyTemplateUtil.generatePolicyDescription(policy);

            LOGGER.debug(String.format("Created client policy: %s", policy)); //$NON-NLS-1$
            return policy;
        });
    }

    /**
     * Gets a policy by its id.  Also verifies that the policy really does belong to
     * the entity indicated.
     * @param type the policy type
     * @param organizationId the org id
     * @param entityId the entity id
     * @param entityVersion the entity version
     * @param policyId the policy id
     * @return a policy bean
     * @throws PolicyNotFoundException
     */
    public PolicyBean getPolicy(PolicyType type, String organizationId, String entityId,
        String entityVersion, long policyId) throws PolicyNotFoundException {

        PolicyBean policy = tryAction(() -> storage.getPolicy(type, organizationId, entityId, entityVersion, policyId));
        if (policy == null) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        }
        if (policy.getType() != type) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        }
        if (!policy.getOrganizationId().equals(organizationId)) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        }
        if (!policy.getEntityId().equals(entityId)) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        }
        if (!policy.getEntityVersion().equals(entityVersion)) {
            throw ExceptionFactory.policyNotFoundException(policyId);
        }
        tryAction(() -> PolicyTemplateUtil.generatePolicyDescription(policy));
        return policy;
    }



}
