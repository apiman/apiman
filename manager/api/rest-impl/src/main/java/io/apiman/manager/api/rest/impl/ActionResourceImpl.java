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

package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.actions.ActionBean;
import io.apiman.manager.api.beans.actions.ContractActionDto;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.rest.IActionResource;
import io.apiman.manager.api.rest.exceptions.ActionException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.service.ActionService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Implementation of the Action REST API.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
@Transactional
public class ActionResourceImpl implements IActionResource, DataAccessUtilMixin {

    private final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ActionResourceImpl.class);
    private IStorage storage;
    private ISecurityContext securityContext;
    private ActionService actionService;

    /**
     * Constructor.
     */
    @Inject
    public ActionResourceImpl(IStorage storage, ISecurityContext securityContext, ActionService actionService) {
        this.storage = storage;
        this.securityContext = securityContext;
        this.actionService = actionService;
    }

    public ActionResourceImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performAction(ActionBean action) throws ActionException, NotAuthorizedException {
        switch (action.getType()) {
            case publishAPI:
                publishApi(action);
                return;
            case retireAPI:
                retireApi(action);
                return;
            case registerClient:
                registerClient(action);
                return;
            case unregisterClient:
                unregisterClient(action);
                return;
            case lockPlan:
                lockPlan(action);
                return;
            default:
                throw ExceptionFactory.actionException(
                     "Action type not supported: " + action.getType().toString()); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Workflow:
     * <ol>
     *     <li>Get specified contract.</li>
     *     <li>Verify if any other contracts still need approving:</li>
     *     <li>- If all approved, move to 'ready' state (or should we publish?).</li>
     *     <li>- If not all approved, leave in 'needs approval' state.</li>
     * </ol>
     */
    @Override
    public void approveContract(ContractActionDto action) throws ActionException, NotAuthorizedException {
        LOGGER.debug("Request approve contract {0} from {1}", action, securityContext.getCurrentUser());
        ContractBean contract = tryAction(() -> storage.getContract(action.getContractId()));
        // Should be a planAdmin in the org the plan was defined in (usually same org as the API is in!).
        OrganizationBean planOrg = contract.getPlan().getPlan().getOrganization();
        securityContext.checkPermissions(PermissionType.planAdmin, planOrg.getId());
        actionService.approveContract(action, securityContext.getCurrentUser());
    }

    private void lockPlan(ActionBean action) throws ActionException, NotAuthorizedException {
        LOGGER.debug("Request to lock plan {0} from {1}", action, securityContext.getCurrentUser());
        securityContext.checkPermissions(PermissionType.planAdmin, action.getOrganizationId());
        actionService.lockPlan(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
    }

    private void unregisterClient(ActionBean action) throws ActionException, NotAuthorizedException {
        LOGGER.debug("Request to unregister client {0} from {1}", action, securityContext.getCurrentUser());
        securityContext.checkPermissions(PermissionType.clientAdmin, action.getOrganizationId());
        actionService.unregisterClient(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
    }

    private void registerClient(ActionBean action) throws ActionException, NotAuthorizedException {
        LOGGER.debug("Request to register client {0} from {1}", action, securityContext.getCurrentUser());
        securityContext.checkPermissions(PermissionType.clientAdmin, action.getOrganizationId());
        actionService.registerClient(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
    }

    private void retireApi(ActionBean action) throws ActionException, NotAuthorizedException {
        LOGGER.debug("Request to retire API {0} from {1}", action, securityContext.getCurrentUser());
        securityContext.checkPermissions(PermissionType.apiAdmin, action.getOrganizationId());
        actionService.retireApi(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
    }

    private void publishApi(ActionBean action) throws ActionException, NotAuthorizedException {
        LOGGER.debug("Request to publish API {0} from {1}", action, securityContext.getCurrentUser());
        securityContext.checkPermissions(PermissionType.apiAdmin, action.getOrganizationId());
        actionService.publishApi(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
    }
}
