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
package io.apiman.manager.api.rest.exceptions.util;

import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.ContractStatus;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.rest.exceptions.*;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Contract;

/**
 * Simple factory for creating REST exceptions.
 *
 * @author eric.wittmann@redhat.com
 */
public final class ExceptionFactory {

    private ExceptionFactory() {
    }

    /**
     * Creates an exception from a username.
     * @param username the username
     * @return the exception
     */
    public static UserNotFoundException userNotFoundException(String username) {
        return new UserNotFoundException(Messages.i18n.format("UserNotFound", username)); //$NON-NLS-1$
    }

    /**
     * Creates a not authorized exception.
     * @return the exception
     */
    public static NotAuthorizedException notAuthorizedException() {
        return new NotAuthorizedException(Messages.i18n.format("AccessDenied")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an organization name.
     * @param organizationName the organization name
     * @return the exception
     */
    public static OrganizationAlreadyExistsException organizationAlreadyExistsException(String organizationName) {
        return new OrganizationAlreadyExistsException(Messages.i18n.format("OrganizationAlreadyExists", organizationName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an organization id.
     * @param organizationId the organization id
     * @return the exception
     */
    public static OrganizationNotFoundException organizationNotFoundException(String organizationId) {
        return new OrganizationNotFoundException(Messages.i18n.format("OrganizationDoesNotExist", organizationId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an role id.
     * @param roleId the role id
     * @return the exception
     */
    public static RoleAlreadyExistsException roleAlreadyExistsException(String roleId) {
        return new RoleAlreadyExistsException(Messages.i18n.format("RoleAlreadyExists", roleId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from a username.
     * @param roleId the role id
     * @return the exception
     */
    public static RoleNotFoundException roleNotFoundException(String roleId) {
        return new RoleNotFoundException(Messages.i18n.format("RoleNotFound", roleId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an client name.
     * @param clientName the client name
     * @return the exception
     */
    public static ClientAlreadyExistsException clientAlreadyExistsException(String clientName) {
        return new ClientAlreadyExistsException(Messages.i18n.format("ClientAlreadyExists", clientName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an client name.
     * @param clientName the client name
     * @param version the version
     * @return the exception
     */
    public static ClientVersionAlreadyExistsException clientVersionAlreadyExistsException(String clientName, String version) {
        return new ClientVersionAlreadyExistsException(Messages.i18n.format("clientVersionAlreadyExists", clientName, version)); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     * @return the exception
     */
    public static ContractAlreadyExistsException contractAlreadyExistsException() {
        return new ContractAlreadyExistsException(Messages.i18n.format("ContractAlreadyExists")); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     * @return the exception
     */
    public static ContractAlreadyExistsException contractDuplicateException() {
        return new ContractAlreadyExistsException(Messages.i18n.format("ContractDuplicate")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an client id.
     * @param clientId the client id
     * @return the exception
     */
    public static ClientNotFoundException clientNotFoundException(String clientId) {
        return new ClientNotFoundException(Messages.i18n.format("ClientDoesNotExist", clientId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an contract id.
     * @param contractId the contract id
     * @return the exception
     */
    public static ContractNotFoundException contractNotFoundException(Long contractId) {
        return new ContractNotFoundException(Messages.i18n.format("ContractDoesNotExist", contractId)); //$NON-NLS-1$
    }

    public static InvalidContractStatusException contractNotYetApprovedException(List<ContractSummaryBean> contracts) {
        return new InvalidContractStatusException(Messages.i18n.format("ContractNotYetApproved",
             contracts.stream()
                      .filter(c -> c.getStatus() == ContractStatus.AwaitingApproval)
                      .map(ContractSummaryBean::getContractId)
                      .collect(Collectors.toList())));
    }

    public static InvalidContractStatusException invalidContractStatus(ContractStatus expected, ContractStatus actual) {
        return new InvalidContractStatusException(Messages.i18n.format("ContractWrongState", expected, actual)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an client id and version.
     * @param clientId the client id
     * @param version the client version
     * @return the exception
     */
    public static ClientVersionNotFoundException clientVersionNotFoundException(String clientId, String version) {
        return new ClientVersionNotFoundException(Messages.i18n.format("clientVersionDoesNotExist", clientId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an invalid client status exception.
     * @return the exception
     */
    public static InvalidClientStatusException invalidClientStatusException() {
        return new InvalidClientStatusException(Messages.i18n.format("InvalidClientStatus")); //$NON-NLS-1$
    }

    public static InvalidClientStatusException clientAwaitingApprovalException() {
        return new InvalidClientStatusException(Messages.i18n.format("ClientAwaitingApproval")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an API name.
     * @param apiName the API name
     * @return the exception
     */
    public static ApiAlreadyExistsException apiAlreadyExistsException(String apiName) {
        return new ApiAlreadyExistsException(Messages.i18n.format("ApiAlreadyExists", apiName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an API name.
     * @param apiName the API name
     * @param version the version
     * @return the exception
     */
    public static ApiVersionAlreadyExistsException apiVersionAlreadyExistsException(String apiName, String version) {
        return new ApiVersionAlreadyExistsException(Messages.i18n.format("ApiVersionAlreadyExists", apiName, version)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an API id.
     * @param apiId the API id
     * @return the exception
     */
    public static ApiNotFoundException apiNotFoundException(String apiId) {
        return new ApiNotFoundException(Messages.i18n.format("ApiDoesNotExist", apiId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an API id and version.
     * @param apiId the API id
     * @param version the API version
     * @return the exception
     */
    public static ApiVersionNotFoundException apiVersionNotFoundException(String apiId, String version) {
        return new ApiVersionNotFoundException(Messages.i18n.format("ApiVersionDoesNotExist", apiId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an API id and version.
     * @param apiId the API id
     * @param version the API version
     * @return the exception
     */
    public static ApiDefinitionNotFoundException apiDefinitionNotFoundException(String apiId, String version) {
        return new ApiDefinitionNotFoundException(Messages.i18n.format("ApiDefinitionDoesNotExist", apiId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an invalid API status exception.
     * @return the exception
     */
    public static InvalidApiStatusException invalidApiStatusException() {
        return new InvalidApiStatusException(Messages.i18n.format("InvalidApiStatus")); //$NON-NLS-1$
    }

    /**
     * Creates an invalid plan status exception.
     * @return the exception
     */
    public static InvalidPlanStatusException invalidPlanStatusException() {
        return new InvalidPlanStatusException(Messages.i18n.format("InvalidPlanStatus")); //$NON-NLS-1$
    }

    /**
     * Creates an invalid plan status exception.
     * @param lockedPlans the list of locked plans
     * @return the exception
     */
    public static InvalidPlanStatusException invalidPlanStatusException(List<PlanVersionSummaryBean> lockedPlans) {
        return new InvalidPlanStatusException(Messages.i18n.format("InvalidPlanStatus") + " " + joinList(lockedPlans)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Creates an exception from an member id.
     * @param memberId the member id
     * @return the exception
     */
    public static MemberNotFoundException memberNotFoundException(String memberId) {
        return new MemberNotFoundException(Messages.i18n.format("MemberDoesNotExist", memberId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan name.
     * @param planName the plan name
     * @return the exception
     */
    public static PlanAlreadyExistsException planAlreadyExistsException(String planName) {
        return new PlanAlreadyExistsException(Messages.i18n.format("PlanAlreadyExists", planName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan name.
     * @param planName the plan name
     * @param version the version
     * @return the exception
     */
    public static PlanVersionAlreadyExistsException planVersionAlreadyExistsException(String planName, String version) {
        return new PlanVersionAlreadyExistsException(Messages.i18n.format("PlanVersionAlreadyExists", planName, version)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan id.
     * @param planId the plan id
     * @return the exception
     */
    public static PlanNotFoundException planNotFoundException(String planId) {
        return new PlanNotFoundException(Messages.i18n.format("PlanDoesNotExist", planId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plan id and version.
     * @param planId the plan id
     * @param version the version id
     * @return the exception
     */
    public static PlanVersionNotFoundException planVersionNotFoundException(String planId, String version) {
        return new PlanVersionNotFoundException(Messages.i18n.format("PlanVersionDoesNotExist", planId, version)); //$NON-NLS-1$
    }

    /**
     * Creates an action exception.
     * @param message the exception message
     * @return the exception
     */
    public static ActionException actionException(String message) {
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
    public static PolicyNotFoundException policyNotFoundException(long policyId) {
        return new PolicyNotFoundException(Messages.i18n.format("PolicyDoesNotExist", policyId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an policyDef name.
     * @param policyDefName the policy definition name
     * @return the exception
     */
    public static PolicyDefinitionAlreadyExistsException policyDefAlreadyExistsException(String policyDefName) {
        return new PolicyDefinitionAlreadyExistsException(Messages.i18n.format("PolicyDefinitionAlreadyExists", policyDefName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an policyDef id.
     * @param policyDefId the policy definition id
     * @return the exception
     */
    public static PolicyDefinitionNotFoundException policyDefNotFoundException(String policyDefId) {
        return new PolicyDefinitionNotFoundException(Messages.i18n.format("PolicyDefinitionDoesNotExist", policyDefId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     * @param message the exception message
     * @return the exception
     */
    public static PolicyDefinitionInvalidException policyDefInvalidException(String message) {
        return new PolicyDefinitionInvalidException(message);
    }

    /**
     * Creates an exception from an gateway name.
     * @param gatewayName the gateway name
     * @return the exception
     */
    public static GatewayAlreadyExistsException gatewayAlreadyExistsException(String gatewayName) {
        return new GatewayAlreadyExistsException(Messages.i18n.format("GatewayAlreadyExists", gatewayName)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an gateway id.
     * @param gatewayId the gateway id
     * @return the exception
     */
    public static GatewayNotFoundException gatewayNotFoundException(String gatewayId) {
        return new GatewayNotFoundException(Messages.i18n.format("GatewayDoesNotExist", gatewayId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plugin name.
     * @return the exception
     */
    public static PluginAlreadyExistsException pluginAlreadyExistsException() {
        return new PluginAlreadyExistsException(Messages.i18n.format("PluginAlreadyExists")); //$NON-NLS-1$
    }

    /**
     * Creates an exception from an plugin id.
     * @param pluginId the plugin id
     * @return the exception
     */
    public static PluginNotFoundException pluginNotFoundException(Long pluginId) {
        return new PluginNotFoundException(Messages.i18n.format("PluginDoesNotExist", pluginId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     * @param resourceName the resource name
     * @param coordinates the maven coordinates
     * @return the exception
     */
    public static PluginResourceNotFoundException pluginResourceNotFoundException(String resourceName,
            PluginCoordinates coordinates) {
        return new PluginResourceNotFoundException(Messages.i18n.format(
                "PluginResourceNotFound", resourceName, coordinates.toString())); //$NON-NLS-1$
    }

    /**
     * Creates an exception from a developer id.
     * @param developerId the developer id
     * @return the exception
     */
    public static DeveloperNotFoundException developerNotFoundException(String developerId) {
        return new DeveloperNotFoundException(Messages.i18n.format("DeveloperNotExist", developerId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception if the developer already exists.
     * @return the exception
     */
    public static DeveloperAlreadyExistsException developerAlreadyExistsException(String developerId) {
        return new DeveloperAlreadyExistsException(Messages.i18n.format("DeveloperAlreadyExists", developerId)); //$NON-NLS-1$
    }

    /**
     * Creates an exception.
     * @param message the message
     * @return the exception
     */
    public static InvalidMetricCriteriaException invalidMetricCriteriaException(String message) {
        return new InvalidMetricCriteriaException(message);
    }

    /**
     * Creates an exception.
     * @param message the message
     * @return the exception
     */
    public static InvalidNameException invalidNameException(String message) {
        return new InvalidNameException(message);
    }

    /**
     * Creates an exception.
     * @param message the message
     * @return the exception
     */
    public static InvalidVersionException invalidVersionException(String message) {
        return new InvalidVersionException(message);
    }

    public static EntityStillActiveException entityStillActiveExceptionContracts(List<ContractBean> contracts) {
        return new EntityStillActiveException(Messages.i18n.format("EntityStillActiveContracts", joinList(contracts))); //$NON-NLS-1$
    }

    public static EntityStillActiveException entityStillActiveExceptionClientVersions(List<ClientVersionBean> clientApps) {
        return new EntityStillActiveException(Messages.i18n.format("EntityStillActiveClientApps", joinList(clientApps))); //$NON-NLS-1$
    }

    public static EntityStillActiveException entityStillActiveExceptionApiVersions(List<ApiVersionBean> apis) {
        return new EntityStillActiveException(Messages.i18n.format("EntityStillActiveApis", joinList(apis))); //$NON-NLS-1$
    }

    public static EntityStillActiveException entityStillActiveExceptionContracts(Iterator<ContractBean> contracts) {
        return new EntityStillActiveException(Messages.i18n.format("EntityStillActiveContracts", joinIter(contracts))); //$NON-NLS-1$
    }

    public static EntityStillActiveException entityStillActiveExceptionClientVersions(Iterator<ClientVersionBean> clientApps) {
        return new EntityStillActiveException(Messages.i18n.format("EntityStillActiveClientApps", joinIter(clientApps))); //$NON-NLS-1$
    }

    public static EntityStillActiveException entityStillActiveExceptionApiVersions(Iterator<ApiVersionBean> apis) {
        return new EntityStillActiveException(Messages.i18n.format("EntityStillActiveApis", joinIter(apis))); //$NON-NLS-1$
    }

    private static <T> String joinList(List<T> items) {
        return items.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }

    private static <T> String joinIter(Iterator<T> iter) {
        Iterable<T> iterable = () -> iter;
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(Object::toString)
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
    }
}
