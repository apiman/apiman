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
package org.overlord.apiman.dt.api.rest.impl.util;

import org.overlord.apiman.dt.api.rest.contract.exceptions.ActionException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ContractAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ContractNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidApplicationStatusException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidServiceStatusException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.MemberNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PlanVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;
import org.overlord.apiman.dt.api.rest.impl.i18n.Messages;

/**
 * Simple factory for creating REST exceptions.
 *
 * @author eric.wittmann@redhat.com
 */
public final class ExceptionFactory {

    /**
     * Creates an exception from a username.
     * @param username
     */
    public static final UserNotFoundException userNotFoundException(String username) {
        return new UserNotFoundException(Messages.i18n.format("UserNotFound", username)); //$NON-NLS-1$
    }

    /**
     * Creates a not authorized exception.
     * @param username
     */
    public static final NotAuthorizedException notAuthorizedException() {
        return new NotAuthorizedException(Messages.i18n.format("AccessDenied")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an organization name.
     * @param organizationName
     */
    public static final OrganizationAlreadyExistsException organizationAlreadyExistsException(String organizationName) {
        return new OrganizationAlreadyExistsException(Messages.i18n.format("OrganizationAlreadyExists", organizationName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an organization id.
     * @param organizationId
     */
    public static final OrganizationNotFoundException organizationNotFoundException(String organizationId) {
        return new OrganizationNotFoundException(Messages.i18n.format("OrganizationDoesNotExist", organizationId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an role id.
     * @param roleId
     */
    public static final RoleAlreadyExistsException roleAlreadyExistsException(String roleId) {
        return new RoleAlreadyExistsException(Messages.i18n.format("RoleAlreadyExists", roleId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from a username.
     * @param roleId
     */
    public static final RoleNotFoundException roleNotFoundException(String roleId) {
        return new RoleNotFoundException(Messages.i18n.format("RoleNotFound", roleId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an application name.
     * @param applicationName
     */
    public static final ApplicationAlreadyExistsException applicationAlreadyExistsException(String applicationName) {
        return new ApplicationAlreadyExistsException(Messages.i18n.format("ApplicationAlreadyExists", applicationName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     */
    public static final ContractAlreadyExistsException contractAlreadyExistsException() {
        return new ContractAlreadyExistsException(Messages.i18n.format("ContractAlreadyExists")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an application id.
     * @param applicationId
     */
    public static final ApplicationNotFoundException applicationNotFoundException(String applicationId) {
        return new ApplicationNotFoundException(Messages.i18n.format("ApplicationDoesNotExist", applicationId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an contract id.
     * @param contractId
     */
    public static final ContractNotFoundException contractNotFoundException(Long contractId) {
        return new ContractNotFoundException(Messages.i18n.format("ContractDoesNotExist", contractId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an application id and version.
     * @param applicationId
     */
    public static final ApplicationVersionNotFoundException applicationVersionNotFoundException(String applicationId, String version) {
        return new ApplicationVersionNotFoundException(Messages.i18n.format("ApplicationVersionDoesNotExist", applicationId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an invalid service status exception.
     */
    public static final InvalidApplicationStatusException invalidApplicationStatusException() {
        return new InvalidApplicationStatusException(Messages.i18n.format("InvalidApplicationStatus")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an service name.
     * @param serviceName
     */
    public static final ServiceAlreadyExistsException serviceAlreadyExistsException(String serviceName) {
        return new ServiceAlreadyExistsException(Messages.i18n.format("ServiceAlreadyExists", serviceName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an service id.
     * @param serviceId
     */
    public static final ServiceNotFoundException serviceNotFoundException(String serviceId) {
        return new ServiceNotFoundException(Messages.i18n.format("ServiceDoesNotExist", serviceId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an service id and version.
     * @param serviceId
     */
    public static final ServiceVersionNotFoundException serviceVersionNotFoundException(String serviceId, String version) {
        return new ServiceVersionNotFoundException(Messages.i18n.format("ServiceVersionDoesNotExist", serviceId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an invalid service status exception.
     */
    public static final InvalidServiceStatusException invalidServiceStatusException() {
        return new InvalidServiceStatusException(Messages.i18n.format("InvalidServiceStatus")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an member id.
     * @param memberId
     */
    public static final MemberNotFoundException memberNotFoundException(String memberId) {
        return new MemberNotFoundException(Messages.i18n.format("MemberDoesNotExist", memberId)); //$NON-NLS-1$
    }
    
    /**
     * Creates an exception from an plan name.
     * @param planName
     */
    public static final PlanAlreadyExistsException planAlreadyExistsException(String planName) {
        return new PlanAlreadyExistsException(Messages.i18n.format("PlanAlreadyExists", planName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan id.
     * @param planId
     */
    public static final PlanNotFoundException planNotFoundException(String planId) {
        return new PlanNotFoundException(Messages.i18n.format("PlanDoesNotExist", planId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan id and version.
     * @param planId
     */
    public static final PlanVersionNotFoundException planVersionNotFoundException(String planId, String version) {
        return new PlanVersionNotFoundException(Messages.i18n.format("PlanVersionDoesNotExist", planId, version)); //$NON-NLS-1$
    }
    
    /**
     * Creates an action exception.
     * @param message
     */
    public static final ActionException actionException(String message) {
        return new ActionException(message);
    }

    /**
     * Creates an action exception.
     * @param message
     * @param cause
     */
    public static ActionException actionException(String message, Exception cause) {
        return new ActionException(message, cause);
    }

    /**
     * Creates an exception from a policy id.
     * @param applicationId
     */
    public static final PolicyNotFoundException policyNotFoundException(long policyId) {
        return new PolicyNotFoundException(Messages.i18n.format("PolicyDoesNotExist", policyId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an policyDef name.
     * @param policyDefName
     */
    public static final PolicyDefinitionAlreadyExistsException policyDefAlreadyExistsException(String policyDefName) {
        return new PolicyDefinitionAlreadyExistsException(Messages.i18n.format("PolicyDefinitionAlreadyExists", policyDefName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an policyDef id.
     * @param policyDefId
     */
    public static final PolicyDefinitionNotFoundException policyDefNotFoundException(String policyDefId) {
        return new PolicyDefinitionNotFoundException(Messages.i18n.format("PolicyDefinitionDoesNotExist", policyDefId)); //$NON-NLS-1$
    }

}
