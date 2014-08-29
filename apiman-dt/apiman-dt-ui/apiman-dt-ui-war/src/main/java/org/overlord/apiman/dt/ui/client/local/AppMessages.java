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
package org.overlord.apiman.dt.ui.client.local;

import org.jboss.errai.ui.shared.api.annotations.TranslationKey;

/**
 * i18n messages used in the Java portion of the application (vs. the templates).
 *
 * @author eric.wittmann@redhat.com
 */
public class AppMessages {

    @TranslationKey(defaultValue = "APIMan - Home")
    public static final String TITLE_DASHBOARD = "page.title.dashboard"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - Organizations")
    public static final String TITLE_CONSUME_ORGS = "page.title.consume-orgs"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - Services")
    public static final String TITLE_CONSUME_SERVICES = "page.title.consume-services"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - Organization")
    public static final String TITLE_CONSUME_ORG = "page.title.consume-org"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - New Organization")
    public static final String TITLE_NEW_ORG = "page.title.new-org"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - New Application")
    public static final String TITLE_NEW_APP = "page.title.new-app"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - New Application Version")
    public static final String TITLE_NEW_APP_VERSION = "page.title.new-app-version"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - New Service")
    public static final String TITLE_NEW_SERVICE = "page.title.new-service"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - New Service Version")
    public static final String TITLE_NEW_SERVICE_VERSION = "page.title.new-service-version"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - Add Member")
    public static final String TITLE_NEW_MEMBER = "page.title.new-member"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - New Plan")
    public static final String TITLE_NEW_PLAN = "page.title.new-plan"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - New Plan Version")
    public static final String TITLE_NEW_PLAN_VERSION = "page.title.new-plan-version"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - New Contract")
    public static final String TITLE_NEW_CONTRACT = "page.title.new-contract"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - New Policy")
    public static final String TITLE_NEW_POLICY = "page.title.new-policy"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - Edit Policy")
    public static final String TITLE_EDIT_POLICY = "page.title.edit-policy"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "APIMan - {0} (Organizations)")
    public static final String TITLE_USER_ORGS = "page.title.user-orgs"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Applications)")
    public static final String TITLE_USER_APPS = "page.title.user-apps"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Services)")
    public static final String TITLE_USER_SERVICES = "page.title.user-services"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Activity)")
    public static final String TITLE_USER_ACTIVITY = "page.title.user-activity"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "APIMan - {0} (Applications)")
    public static final String TITLE_ORG_APPS = "page.title.org-apps"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Activity)")
    public static final String TITLE_ORG_ACTIVITY = "page.title.org-activity"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Members)")
    public static final String TITLE_ORG_MEMBERS = "page.title.org-members"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Plans)")
    public static final String TITLE_ORG_PLANS = "page.title.org-plans"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Services)")
    public static final String TITLE_ORG_SERVICES = "page.title.org-services"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Manage Members)")
    public static final String TITLE_ORG_MANAGE_MEMBERS = "page.title.org-manage-members"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "APIMan - {0} (Activity)")
    public static final String TITLE_APP_ACTIVITY = "page.title.app-activity"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Contracts)")
    public static final String TITLE_APP_CONTRACTS = "page.title.app-contracts"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (APIs)")
    public static final String TITLE_APP_APIS = "page.title.app-apis"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Overview)")
    public static final String TITLE_APP_OVERVIEW = "page.title.app-overview"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Policies)")
    public static final String TITLE_APP_POLICIES = "page.title.app-policies"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "APIMan - {0} (Overview)")
    public static final String TITLE_SERVICE_OVERVIEW = "page.title.service-overview"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Implementation)")
    public static final String TITLE_SERVICE_IMPL = "page.title.service-impl"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Plans)")
    public static final String TITLE_SERVICE_PLANS = "page.title.service-plans"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Policies)")
    public static final String TITLE_SERVICE_POLICIES = "page.title.service-policies"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Activity)")
    public static final String TITLE_SERVICE_ACTIVITY = "page.title.service-activity"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "APIMan - {0} (Overview)")
    public static final String TITLE_PLAN_OVERVIEW = "page.title.plan-overview"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Policies)")
    public static final String TITLE_PLAN_POLICIES = "page.title.plan-policies"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Activity)")
    public static final String TITLE_PLAN_ACTIVITY = "page.title.plan-activity"; //$NON-NLS-1$
    
    @TranslationKey(defaultValue = "Configuration service not available.")
    public static final String CONFIG_SERVICE_NOT_AVAILABLE = "authInterceptor.configServiceNotAvailable"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "It looks like there aren't (yet) any applications in this organization! Now might be a good time to click the New App button up above...")
    public static final String NO_APPS_IN_ORG_MESSAGE = "noEntitiesFound.applications-in-org"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No applications matched the filter criteria.  Please try modifying the filter or else create a new application using the button above.")
    public static final String NO_FILTERED_APPS_IN_ORG_MESSAGE = "noEntitiesFound.applications-in-org.filtered"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "We couldn't find any services in this organization.  Probably because none exist.  We hope.  Try creating one using the New Service button.")
    public static final String NO_SERVICES_IN_ORG_MESSAGE = "noEntitiesFound.services-in-org"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No services matched the current filter criteria.  Please try modifying the filter or else create a new service using the button above.")
    public static final String NO_FILTERED_SERVICES_IN_ORG_MESSAGE = "noEntitiesFound.services-in-org.filtered"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "There aren't any plans configured for this organization.  That means all published services in this organization will be considered public.")
    public static final String NO_PLANS_IN_ORG_MESSAGE = "noEntitiesFound.plans-in-org"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No plans matched the current filter criteria.  Please try modifying the filter or else create a new plan using the button above.")
    public static final String NO_FILTERED_PLANS_IN_ORG_MESSAGE = "noEntitiesFound.plans-in-org.filtered"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "This organization has no members!  Use the Manage Members button above to change that.")
    public static final String NO_MEMBERS_IN_ORG_MESSAGE = "noEntitiesFound.members-in-org"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No members matched the filter criteria.  Please try modifying the filter or else change memberships with the Manage Members button.")
    public static final String NO_FILTERED_MEMBERS_IN_ORG_MESSAGE = "noEntitiesFound.members-in-org.filtered"; //$NON-NLS-1$
    
    @TranslationKey(defaultValue = "No organizations found.  This user should be granted membership in an organization or perhaps create a new one with the button above.")
    public static final String NO_ORGS_FOR_USER_MESSAGE = "noEntitiesFound.organizations-for-user"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No organization matches the current filter criteria.  Please try changing the filter criteria or else create a new Organization using the button above.")
    public static final String NO_FILTERED_ORGS_FOR_USER_MESSAGE = "noEntitiesFound.organizations-for-user.filtered"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "User is not managing any applications.  Perhaps that's just not her thing.  But if it is, she can create a new Application using the New App button above.")
    public static final String NO_APPS_FOR_USER_MESSAGE = "noEntitiesFound.applications-for-user"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No applications matched the filter criteria.  Please try modifying the filter or else create a new application using the button above.")
    public static final String NO_FILTERED_APPS_FOR_USER_MESSAGE = "noEntitiesFound.applications-for-user.filtered"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "It looks like this user isn't responsible for any services.  Maybe she's just all about the applications?  If not, maybe she could log in and try creating a New Service.")
    public static final String NO_SERVICES_FOR_USER_MESSAGE = "noEntitiesFound.services-for-user"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No services matched the current filter criteria.  Please try modifying the filter or else create a new service using the button above.")
    public static final String NO_FILTERED_SERVICES_FOR_USER_MESSAGE = "noEntitiesFound.services-for-user.filtered"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "This application doesn't appear to have any service contracts.  You could try creating one using the New Contract button above.  Alternatively you can browse for a Service in the UI and create a contract from the Service details page.")
    public static final String NO_CONTRACTS_FOR_APP_MESSAGE = "noEntitiesFound.contracts-in-app"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "This application doesn't appear to have any service contracts (and therefore no referenced APIs).  There are a variety of ways to create contracts for this application, but you may want to start with the Contracts tab on this page.")
    public static final String NO_APIS_FOR_APP_MESSAGE = "noEntitiesFound.apis-in-app"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No contracts matched the filter criteria.  Please try modifying the filter or else create a new contract using the New Contract button above.")
    public static final String NO_FILTERED_CONTRACTS_FOR_APP_MESSAGE = "noEntitiesFound.contracts-in-app.filtered"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "No organizations found.  Either no organizations matched the query or you haven't queried yet!")
    public static final String NO_ORGANIZATIONS_MESSAGE = "noEntitiesFound.consumer-orgs"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No services are currently offered by this organization.")
    public static final String NO_SERVICES_IN_CONSUMER_ORG_MESSAGE = "noEntitiesFound.services-in-consumer-org"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No services matched the current filter criteria.")
    public static final String NO_FILTERED_SERVICES_IN_CONSUMER_ORG_MESSAGE = "noEntitiesFound.services-in-consumer-org.filtered"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Assign roles to {0}.")
    public static final String MEMBER_CARD_ASSIGN_ROLES_HELP = "manageMembers.card.edit-help"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "No users found.")
    public static final String USER_SELECTOR_NONE_FOUND = "userSelector.users.no-users-found"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No services found.")
    public static final String SERVICE_SELECTOR_NONE_FOUND = "serviceSelector.users.no-services-found"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Search for users above")
    public static final String NEW_MEMBER_SEARCH_TEXT = "newMember.userSelector.help-text"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "No members found matching the selected filter criteria.")
    public static final String ORG_MANAGE_MEMBERS_NO_MEMBERS_FOUND = "manageMembers.no-members-found"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Created on")
    public static final String CREATED_ON = "created-on"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Joined on")
    public static final String JOINED_ON = "joined-on"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Version: {0}")
    public static final String VERSION_SELECTOR_LABEL = "version-selector.label"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Copy")
    public static final String COPY = "copy"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Key")
    public static final String KEY = "key"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Break Contract")
    public static final String BREAK_CONTRACT = "break-contract"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Breaking...")
    public static final String BREAKING = "breaking"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Service version")
    public static final String SERVICE_VERSION = "service-version"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "via plan")
    public static final String VIA_PLAN = "via-plan"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "entered into on")
    public static final String ENTERED_INTO_ON = "entered-into-on"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Really break contract with service {0}?")
    public static final String CONFIRM_BREAK_CONTRACT = "confirm-break-contract"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Failed to break contract!")
    public static final String BREAK_CONTRACT_FAILURE = "break-contract-failure"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Found {0} matching organizations.")
    public static final String ORGANIZATION_COUNT = "organization-count"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Request Membership")
    public static final String REQUEST_MEMBERSHIP = "request-membership"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "You are already a member of this Organization.")
    public static final String ALREADY_A_MEMBER = "already-a-member"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "No members found")
    public static final String NO_MEMBERS_MESSAGE = "no-members-found"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Please copy the value below (e.g. Ctrl-C).")
    public static final String APIS_LIST_PLEASE_COPY = "appApisList.please-copy"; //$NON-NLS-1$
    
    @TranslationKey(defaultValue = "No policies matched the filter criteria.  Please try modifying the filter or else create a new policy using the button above.")
    public static final String NO_FILTERED_POLICIES_MESSAGE = "no-filtered-policies"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "It looks like there aren't (yet) any policies defined!  That may be exactly what you want (of course) but if not, you may try defining one using the New Policy button above...")
    public static final String NO_POLICIES_MESSAGE = "no-policies"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Policy created by")
    public static final String POLICY_CREATED_BY = "policy-created-by"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Remove")
    public static final String REMOVE = "remove"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Removing...")
    public static final String REMOVING = "removing"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Really remove policy {0}?")
    public static final String CONFIRM_REMOVE_POLICY = "confirm-remove-policy"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "on")
    public static final String ON = "on"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Please manually configure your policy's JSON configuration below.")
    public static final String DEFAULT_POLICY_FORM_HELP = "default-policy-form-help"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Choose a policy type...")
    public static final String CHOOSE_POLICY_TYPE = "choose-policy-type"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Choose an Identity Source...")
    public static final String CHOOSE_IDENTITY_SOURCE = "choose-identity-source"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Create Contract")
    public static final String CREATE_CONTRACT = "create-contract"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "No services found.  Either no services matched the query or you haven't queried yet!")
    public static final String NO_SERVICES_MESSAGE = "noEntitiesFound.consumer-services"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "Found {0} matching services.")
    public static final String SERVICE_COUNT = "service-count"; //$NON-NLS-1$

}
