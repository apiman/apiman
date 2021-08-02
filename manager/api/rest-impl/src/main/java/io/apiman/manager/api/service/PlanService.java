package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.data.EntityUpdatedData;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.NewPlanBean;
import io.apiman.manager.api.beans.plans.NewPlanVersionBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plans.UpdatePlanBean;
import io.apiman.manager.api.beans.policies.NewPolicyBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyChainBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.policies.UpdatePolicyBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.exceptions.ApiNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.exceptions.InvalidPlanStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidVersionException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanVersionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.FieldValidator;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import static java.util.stream.Collectors.toList;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class PlanService implements DataAccessUtilMixin {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(PlanService.class);

    private final IStorage storage;
    private final IStorageQuery query;
    private final ISecurityContext securityContext;
    private final OrganizationService organizationService;
    private final PolicyService policyService;

    @Inject
    public PlanService(
        IStorage storage,
        IStorageQuery query,
        ISecurityContext securityContext,
        OrganizationService organizationService,
        PolicyService policyService
    ) {
        this.storage = storage;
        this.query = query;
        this.securityContext = securityContext;
        this.organizationService = organizationService;
        this.policyService = policyService;
    }

    @Transactional
    public PlanBean createPlan(String organizationId, NewPlanBean bean) throws OrganizationNotFoundException,
        PlanAlreadyExistsException, NotAuthorizedException, InvalidNameException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        FieldValidator.validateName(bean.getName());

        PlanBean newPlan = new PlanBean();
        newPlan.setName(bean.getName());
        newPlan.setDescription(bean.getDescription());
        newPlan.setId(BeanUtils.idFromName(bean.getName()));
        newPlan.setCreatedOn(new Date());
        newPlan.setCreatedBy(securityContext.getCurrentUser());

        return tryAction(() -> {
            // Store/persist the new plan
            OrganizationBean orgBean = organizationService.getOrg(organizationId);
            if (storage.getPlan(orgBean.getId(), newPlan.getId()) != null) {
                throw ExceptionFactory.planAlreadyExistsException(newPlan.getName());
            }
            newPlan.setOrganization(orgBean);
            storage.createPlan(newPlan);
            storage.createAuditEntry(AuditUtils.planCreated(newPlan, securityContext));

            if (bean.getInitialVersion() != null) {
                NewPlanVersionBean newPlanVersion = new NewPlanVersionBean();
                newPlanVersion.setVersion(bean.getInitialVersion());
                createPlanVersionInternal(newPlanVersion, newPlan);
            }

            LOGGER.debug(String.format("Created plan: %s", newPlan)); //$NON-NLS-1$
            return newPlan;
        });
    }

    @Transactional
    public PlanBean getPlan(String organizationId, String planId)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);
        return tryAction(() -> {
            PlanBean bean = storage.getPlan(organizationId, planId);
            if (bean == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }
            LOGGER.debug(String.format("Got plan: %s", bean)); //$NON-NLS-1$
            return bean;
        });
    }

    @Transactional
    public SearchResultsBean<AuditEntryBean> getPlanActivity(String organizationId, String planId, int page, int pageSize)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        final int finalPage = Math.max(page, 1);
        final int finalPageSize = pageSize <= 0 ? 20 : pageSize;

        PagingBean paging = new PagingBean();
        paging.setPage(finalPage);
        paging.setPageSize(finalPageSize);

        return tryAction(() -> query.auditEntity(organizationId, planId, null, PlanBean.class, paging));
    }

    @Transactional
    public List<PlanSummaryBean> listPlans(String organizationId) throws OrganizationNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.orgView, organizationId);

        organizationService.getOrg(organizationId);

        return tryAction(() -> query.getPlansInOrg(organizationId));
    }

    @Transactional
    public void updatePlan(String organizationId, String planId, UpdatePlanBean bean)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        EntityUpdatedData auditData = new EntityUpdatedData();

        tryAction(() -> {
            PlanBean planForUpdate = storage.getPlan(organizationId, planId);
            if (planForUpdate == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }
            if (AuditUtils.valueChanged(planForUpdate.getDescription(), bean.getDescription())) {
                auditData.addChange("description", planForUpdate.getDescription(), bean.getDescription()); //$NON-NLS-1$
                planForUpdate.setDescription(bean.getDescription());
            }
            storage.updatePlan(planForUpdate);
            storage.createAuditEntry(AuditUtils.planUpdated(planForUpdate, auditData, securityContext));
            LOGGER.debug(String.format("Updated plan: %s", planForUpdate)); //$NON-NLS-1$
        });
    }

    @Transactional
    public PlanVersionBean createPlanVersion(String organizationId, String planId, NewPlanVersionBean bean)
        throws PlanNotFoundException, NotAuthorizedException, InvalidVersionException,
        PlanVersionAlreadyExistsException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        FieldValidator.validateVersion(bean.getVersion());

        PlanVersionBean newVersion = tryAction(() -> {
            PlanBean plan = storage.getPlan(organizationId, planId);
            if (plan == null) {
                throw ExceptionFactory.planNotFoundException(planId);
            }

            if (storage.getPlanVersion(organizationId, planId, bean.getVersion()) != null) {
                throw ExceptionFactory.planVersionAlreadyExistsException(planId, bean.getVersion());
            }

            return createPlanVersionInternal(bean, plan);
        });

        if (bean.isClone() && bean.getCloneVersion() != null) {
            try {
                List<PolicySummaryBean> policies = listPlanPolicies(organizationId, planId, bean.getCloneVersion());
                for (PolicySummaryBean policySummary : policies) {
                    PolicyBean policy = getPlanPolicy(organizationId, planId, bean.getCloneVersion(), policySummary.getId());
                    NewPolicyBean npb = new NewPolicyBean();
                    npb.setDefinitionId(policy.getDefinition().getId());
                    npb.setConfiguration(policy.getConfiguration());
                    createPlanPolicy(organizationId, planId, newVersion.getVersion(), npb);
                }
            } catch (Exception e) {
                // TODO it's ok if the clone fails - we did our best
            }
        }

        LOGGER.debug(String.format("Created plan %s version: %s", planId, newVersion)); //$NON-NLS-1$
        return newVersion;
    }

    /**
     * Creates a plan version.
     */
    private PlanVersionBean createPlanVersionInternal(NewPlanVersionBean bean, PlanBean plan)
        throws StorageException {
        if (!BeanUtils.isValidVersion(bean.getVersion())) {
            throw new StorageException("Invalid/illegal plan version: " + bean.getVersion()); //$NON-NLS-1$
        }

        PlanVersionBean newVersion = new PlanVersionBean();
        newVersion.setCreatedBy(securityContext.getCurrentUser());
        newVersion.setCreatedOn(new Date());
        newVersion.setModifiedBy(securityContext.getCurrentUser());
        newVersion.setModifiedOn(new Date());
        newVersion.setStatus(PlanStatus.Created);
        newVersion.setPlan(plan);
        newVersion.setVersion(bean.getVersion());
        storage.createPlanVersion(newVersion);
        storage.createAuditEntry(AuditUtils.planVersionCreated(newVersion, securityContext));
        return newVersion;
    }

    @Transactional
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
        throws PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        return getPlanVersionInternal(organizationId, planId, version);
    }

    private PlanVersionBean getPlanVersionInternal(String organizationId, String planId, String version) throws PlanVersionNotFoundException {
        return tryAction(() -> {
            PlanVersionBean planVersion = getPlanVersionFromStorage(organizationId, planId, version);
            LOGGER.debug(String.format("Got plan %s version: %s", planId, planVersion)); //$NON-NLS-1$
            return planVersion;
        });
    }

    private PlanVersionBean getPlanVersionFromStorage(String organizationId, String planId, String version) throws PlanVersionNotFoundException, StorageException {
        PlanVersionBean planVersion = storage.getPlanVersion(organizationId, planId, version);
        if (planVersion == null) {
            throw ExceptionFactory.planVersionNotFoundException(planId, version);
        }
        return planVersion;
    }

    public SearchResultsBean<AuditEntryBean> getPlanVersionActivity(String organizationId, String planId,
        String version, int page, int pageSize) throws PlanVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        final int finalPage = Math.max(page, 1);
        final int finalPageSize = pageSize == 0 ? 20 : pageSize;

        PagingBean paging = new PagingBean();
        paging.setPage(finalPage);
        paging.setPageSize(finalPageSize);
        return tryAction(() -> query.auditEntity(organizationId, planId, version, PlanBean.class, paging));
    }

    public List<PlanVersionSummaryBean> listPlanVersions(String organizationId, String planId)
        throws PlanNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        // Try to get the plan first - will throw a PlanNotFoundException if not found.
        getPlan(organizationId, planId);

        return tryAction(() -> query.getPlanVersions(organizationId, planId));
    }

    public PolicyBean createPlanPolicy(String organizationId, String planId, String version,
        NewPolicyBean bean) throws OrganizationNotFoundException, PlanVersionNotFoundException,
        NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        // Make sure the plan version exists and is in the right state
        PlanVersionBean pvb = getPlanVersionInternal(organizationId, planId, version);
        if (pvb.getStatus() == PlanStatus.Locked) {
            throw ExceptionFactory.invalidPlanStatusException();
        }

        return tryAction(() -> {
            pvb.setModifiedOn(new Date());
            pvb.setModifiedBy(securityContext.getCurrentUser());

            LOGGER.debug(String.format("Creating plan %s policy %s", planId, pvb)); //$NON-NLS-1$
            return policyService.createPolicy(organizationId, planId, version, bean, PolicyType.Plan);
        });
    }


    public PolicyBean getPlanPolicy(String organizationId, String planId, String version, long policyId)
        throws OrganizationNotFoundException, PlanVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        // Make sure the plan version exists
        getPlanVersionInternal(organizationId, planId, version);

        PolicyBean policy = policyService.getPolicy(PolicyType.Plan, organizationId, planId, version, policyId);

        LOGGER.debug(String.format("Got plan policy %s", policy)); //$NON-NLS-1$
        return policy;
    }


    public void updatePlanPolicy(String organizationId, String planId, String version,
        long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
        PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        // Make sure the plan version exists
        PlanVersionBean pvb = getPlanVersionInternal(organizationId, planId, version);

        tryAction(() -> {
            PolicyBean policy = storage.getPolicy(PolicyType.Plan, organizationId, planId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            if (AuditUtils.valueChanged(policy.getConfiguration(), bean.getConfiguration())) {
                policy.setConfiguration(bean.getConfiguration());
                // Note: we do not audit the policy configuration since it may have sensitive data
            }
            policy.setModifiedOn(new Date());
            policy.setModifiedBy(this.securityContext.getCurrentUser());
            storage.updatePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyUpdated(policy, PolicyType.Plan, securityContext));

            pvb.setModifiedBy(securityContext.getCurrentUser());
            pvb.setModifiedOn(new Date());
            storage.updatePlanVersion(pvb);

            LOGGER.debug(String.format("Updated plan policy %s", policy)); //$NON-NLS-1$
        });
    }

    public void deletePlanPolicy(String organizationId, String planId, String version, long policyId)
        throws OrganizationNotFoundException, PlanVersionNotFoundException,
        PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        // Make sure the plan version exists
        PlanVersionBean pvb = getPlanVersionInternal(organizationId, planId, version);
        if (pvb.getStatus() == PlanStatus.Locked) {
            throw ExceptionFactory.invalidPlanStatusException();
        }

        tryAction(() -> {
            PolicyBean policy = this.storage.getPolicy(PolicyType.Plan, organizationId, planId, version, policyId);
            if (policy == null) {
                throw ExceptionFactory.policyNotFoundException(policyId);
            }
            storage.deletePolicy(policy);
            storage.createAuditEntry(AuditUtils.policyRemoved(policy, PolicyType.Plan, securityContext));

            pvb.setModifiedBy(securityContext.getCurrentUser());
            pvb.setModifiedOn(new Date());
            storage.updatePlanVersion(pvb);

            storage.commitTx();
            LOGGER.debug(String.format("Deleted plan policy %s", policy)); //$NON-NLS-1$
        });
    }

    public void deletePlan(String organizationId, String planId)
        throws ApiNotFoundException, NotAuthorizedException, InvalidPlanStatusException {
        securityContext.checkPermissions(PermissionType.planAdmin, organizationId);

        List<PlanVersionSummaryBean> lockedPlans = listPlanVersions(organizationId, planId).stream()
            .filter(summary -> summary.getStatus() == PlanStatus.Locked).collect(toList());

        if (!lockedPlans.isEmpty())
            throw ExceptionFactory.invalidPlanStatusException(lockedPlans);

        tryAction(() -> {
            PlanBean plan = storage.getPlan(organizationId, planId);
            storage.deletePlan(plan);
            storage.commitTx();
        });
    }

    public List<PolicySummaryBean> listPlanPolicies(String organizationId, String planId, String version)
        throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planView, organizationId);

        // Try to get the plan first - will throw an exception if not found.
        getPlanVersionInternal(organizationId, planId, version);

        return tryAction(() -> query.getPolicies(organizationId, planId, version, PolicyType.Plan));
    }

    public void reorderPlanPolicies(String organizationId, String planId, String version,
        PolicyChainBean policyChain) throws OrganizationNotFoundException,
        PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planEdit, organizationId);

        // Make sure the plan version exists
        PlanVersionBean pvb = getPlanVersionInternal(organizationId, planId, version);

        tryAction(() -> {
            List<Long> newOrder = new ArrayList<>(policyChain.getPolicies().size());
            for (PolicySummaryBean psb : policyChain.getPolicies()) {
                newOrder.add(psb.getId());
            }
            storage.reorderPolicies(PolicyType.Plan, organizationId, planId, version, newOrder);
            storage.createAuditEntry(AuditUtils.policiesReordered(pvb, PolicyType.Plan, securityContext));

            pvb.setModifiedBy(securityContext.getCurrentUser());
            pvb.setModifiedOn(new Date());
            storage.updatePlanVersion(pvb);
        });
    }

}
