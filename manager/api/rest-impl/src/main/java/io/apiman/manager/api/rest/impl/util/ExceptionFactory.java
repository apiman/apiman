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
package io.apiman.manager.api.rest.impl.util;

import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.manager.api.rest.contract.exceptions.ActionException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationVersionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ContractAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ContractNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.GatewayAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidApplicationStatusException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidMetricCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidServiceStatusException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidVersionException;
import io.apiman.manager.api.rest.contract.exceptions.MemberNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PluginAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PluginNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PluginResourceNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionInvalidException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.RoleAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceDefinitionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceVersionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.UserNotFoundException;
import io.apiman.manager.api.rest.impl.i18n.Messages;

/**
 * Simple factory for creating REST exceptions.
 *
 * @author eric.wittmann@redhat.com
 */
public final class ExceptionFactory {

    /**
     * Creates an exception from a username.
     * @param username the username
     * @return the exception
     */
    public static final UserNotFoundException userNotFoundException(String username) {
        return new UserNotFoundException(Messages.i18n.format("UserNotFound", username)); //$NON-NLS-1$
    }

    /**
     * Creates a not authorized exception.
     * @return the exception
     */
    public static final NotAuthorizedException notAuthorizedException() {
        return new NotAuthorizedException(Messages.i18n.format("AccessDenied")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an organization name.
     * @param organizationName the organization name
     * @return the exception
     */
    public static final OrganizationAlreadyExistsException organizationAlreadyExistsException(String organizationName) {
        return new OrganizationAlreadyExistsException(Messages.i18n.format("OrganizationAlreadyExists", organizationName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an organization id.
     * @param organizationId the organization id
     * @return the exception
     */
    public static final OrganizationNotFoundException organizationNotFoundException(String organizationId) {
        return new OrganizationNotFoundException(Messages.i18n.format("OrganizationDoesNotExist", organizationId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an role id.
     * @param roleId the role id
     * @return the exception
     */
    public static final RoleAlreadyExistsException roleAlreadyExistsException(String roleId) {
        return new RoleAlreadyExistsException(Messages.i18n.format("RoleAlreadyExists", roleId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from a username.
     * @param roleId the role id
     * @return the exception
     */
    public static final RoleNotFoundException roleNotFoundException(String roleId) {
        return new RoleNotFoundException(Messages.i18n.format("RoleNotFound", roleId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an application name.
     * @param applicationName the application name
     * @return the exception
     */
    public static final ApplicationAlreadyExistsException applicationAlreadyExistsException(String applicationName) {
        return new ApplicationAlreadyExistsException(Messages.i18n.format("ApplicationAlreadyExists", applicationName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an application name.
     * @param applicationName the application name
     * @param version the version
     * @return the exception
     */
    public static final ApplicationVersionAlreadyExistsException applicationVersionAlreadyExistsException(String applicationName, String version) {
        return new ApplicationVersionAlreadyExistsException(Messages.i18n.format("ApplicationVersionAlreadyExists", applicationName, version)); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     * @return the exception
     */
    public static final ContractAlreadyExistsException contractAlreadyExistsException() {
        return new ContractAlreadyExistsException(Messages.i18n.format("ContractAlreadyExists")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an application id.
     * @param applicationId the application id
     * @return the exception
     */
    public static final ApplicationNotFoundException applicationNotFoundException(String applicationId) {
        return new ApplicationNotFoundException(Messages.i18n.format("ApplicationDoesNotExist", applicationId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an contract id.
     * @param contractId the contract id
     * @return the exception
     */
    public static final ContractNotFoundException contractNotFoundException(Long contractId) {
        return new ContractNotFoundException(Messages.i18n.format("ContractDoesNotExist", contractId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an application id and version.
     * @param applicationId the application id
     * @param version the application version
     * @return the exception
     */
    public static final ApplicationVersionNotFoundException applicationVersionNotFoundException(String applicationId, String version) {
        return new ApplicationVersionNotFoundException(Messages.i18n.format("ApplicationVersionDoesNotExist", applicationId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an invalid service status exception.
     * @return the exception
     */
    public static final InvalidApplicationStatusException invalidApplicationStatusException() {
        return new InvalidApplicationStatusException(Messages.i18n.format("InvalidApplicationStatus")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an service name.
     * @param serviceName the service name
     * @return the exception
     */
    public static final ServiceAlreadyExistsException serviceAlreadyExistsException(String serviceName) {
        return new ServiceAlreadyExistsException(Messages.i18n.format("ServiceAlreadyExists", serviceName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an service name.
     * @param serviceName the service name
     * @param version the version
     * @return the exception
     */
    public static final ServiceVersionAlreadyExistsException serviceVersionAlreadyExistsException(String serviceName, String version) {
        return new ServiceVersionAlreadyExistsException(Messages.i18n.format("ServiceVersionAlreadyExists", serviceName, version)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an service id.
     * @param serviceId the service id
     * @return the exception
     */
    public static final ServiceNotFoundException serviceNotFoundException(String serviceId) {
        return new ServiceNotFoundException(Messages.i18n.format("ServiceDoesNotExist", serviceId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an service id and version.
     * @param serviceId the service id
     * @param version the service version
     * @return the exception
     */
    public static final ServiceVersionNotFoundException serviceVersionNotFoundException(String serviceId, String version) {
        return new ServiceVersionNotFoundException(Messages.i18n.format("ServiceVersionDoesNotExist", serviceId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an service id and version.
     * @param serviceId the service id
     * @param version the service version
     * @return the exception
     */
    public static final ServiceDefinitionNotFoundException serviceDefinitionNotFoundException(String serviceId, String version) {
        return new ServiceDefinitionNotFoundException(Messages.i18n.format("ServiceDefinitionDoesNotExist", serviceId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an invalid service status exception.
     * @return the exception
     */
    public static final InvalidServiceStatusException invalidServiceStatusException() {
        return new InvalidServiceStatusException(Messages.i18n.format("InvalidServiceStatus")); //$NON-NLS-1$
    }

    /**
     * Creates an invalid plan status exception.
     * @return the exception
     */
    public static final InvalidServiceStatusException invalidPlanStatusException() {
        return new InvalidServiceStatusException(Messages.i18n.format("InvalidPlanStatus")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an member id.
     * @param memberId the member id
     * @return the exception
     */
    public static final MemberNotFoundException memberNotFoundException(String memberId) {
        return new MemberNotFoundException(Messages.i18n.format("MemberDoesNotExist", memberId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan name.
     * @param planName the plan name
     * @return the exception
     */
    public static final PlanAlreadyExistsException planAlreadyExistsException(String planName) {
        return new PlanAlreadyExistsException(Messages.i18n.format("PlanAlreadyExists", planName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan name.
     * @param planName the plan name
     * @param version the version
     * @return the exception
     */
    public static final PlanVersionAlreadyExistsException planVersionAlreadyExistsException(String planName, String version) {
        return new PlanVersionAlreadyExistsException(Messages.i18n.format("PlanVersionAlreadyExists", planName, version)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan id.
     * @param planId the plan id
     * @return the exception
     */
    public static final PlanNotFoundException planNotFoundException(String planId) {
        return new PlanNotFoundException(Messages.i18n.format("PlanDoesNotExist", planId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan id and version.
     * @param planId the plan id
     * @param version the version id
     * @return the exception
     */
    public static final PlanVersionNotFoundException planVersionNotFoundException(String planId, String version) {
        return new PlanVersionNotFoundException(Messages.i18n.format("PlanVersionDoesNotExist", planId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an action exception.
     * @param message the exception message
     * @return the exception
     */
    public static final ActionException actionException(String message) {
        return new ActionException(message);
    }

    /**
     * Creates an action exception.
     * @param message the exception message
     * @param cause the exception cause
     * @return the exception
     */
    public static ActionException actionException(String message, Exception cause) {
        return new ActionException(message, cause);
    }

    /**
     * Creates an exception from a policy id.
     * @param policyId the policy id
     * @return the exception
     */
    public static final PolicyNotFoundException policyNotFoundException(long policyId) {
        return new PolicyNotFoundException(Messages.i18n.format("PolicyDoesNotExist", policyId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an policyDef name.
     * @param policyDefName the policy definition name
     * @return the exception
     */
    public static final PolicyDefinitionAlreadyExistsException policyDefAlreadyExistsException(String policyDefName) {
        return new PolicyDefinitionAlreadyExistsException(Messages.i18n.format("PolicyDefinitionAlreadyExists", policyDefName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an policyDef id.
     * @param policyDefId the policy definition id
     * @return the exception
     */
    public static final PolicyDefinitionNotFoundException policyDefNotFoundException(String policyDefId) {
        return new PolicyDefinitionNotFoundException(Messages.i18n.format("PolicyDefinitionDoesNotExist", policyDefId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     * @param message the exception message
     * @return the exception
     */
    public static final PolicyDefinitionInvalidException policyDefInvalidException(String message) {
        return new PolicyDefinitionInvalidException(message);
    }

    /**
     * Creates an exception from an gateway name.
     * @param gatewayName the gateway name
     * @return the exception
     */
    public static final GatewayAlreadyExistsException gatewayAlreadyExistsException(String gatewayName) {
        return new GatewayAlreadyExistsException(Messages.i18n.format("GatewayAlreadyExists", gatewayName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an gateway id.
     * @param gatewayId the gateway id
     * @return the exception
     */
    public static final GatewayNotFoundException gatewayNotFoundException(String gatewayId) {
        return new GatewayNotFoundException(Messages.i18n.format("GatewayDoesNotExist", gatewayId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plugin name.
     * @return the exception
     */
    public static final PluginAlreadyExistsException pluginAlreadyExistsException() {
        return new PluginAlreadyExistsException(Messages.i18n.format("PluginAlreadyExists")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plugin id.
     * @param pluginId the plugin id
     * @return the exception
     */
    public static final PluginNotFoundException pluginNotFoundException(Long pluginId) {
        return new PluginNotFoundException(Messages.i18n.format("PluginDoesNotExist", pluginId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     * @param resourceName the resource name
     * @param coordinates the maven coordinates
     * @return the exception
     */
    public static final PluginResourceNotFoundException pluginResourceNotFoundException(String resourceName,
            PluginCoordinates coordinates) {
        return new PluginResourceNotFoundException(Messages.i18n.format(
                "PluginResourceNotFound", resourceName, coordinates.toString())); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     * @param message
     * @return the exception
     */
    public static final InvalidMetricCriteriaException invalidMetricCriteriaException(String message) {
        return new InvalidMetricCriteriaException(message);
    }

    /**
     * Creates an exception.
     * @param message
     */
    public static final InvalidNameException invalidNameException(String message) {
        return new InvalidNameException(message);
    }

    /**
     * Creates an exception.
     * @param message
     */
    public static final InvalidVersionException invalidVersionException(String message) {
        return new InvalidVersionException(message);
    }

}
