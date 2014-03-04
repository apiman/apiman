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
    @TranslationKey(defaultValue = "APIMan - {0} (Overview)")
    public static final String TITLE_APP_OVERVIEW = "page.title.app-overview"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Policies)")
    public static final String TITLE_APP_POLICIES = "page.title.app-policies"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "APIMan - {0} (Overview)")
    public static final String TITLE_SERVICE_OVERVIEW = "page.title.service-overview"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Implementation)")
    public static final String TITLE_SERVICE_IMPL = "page.title.service-impl"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Policies)")
    public static final String TITLE_SERVICE_POLICIES = "page.title.service-policies"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "APIMan - {0} (Activity)")
    public static final String TITLE_SERVICE_ACTIVITY = "page.title.service-activity"; //$NON-NLS-1$

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

    @TranslationKey(defaultValue = "Assign roles to {0}.")
    public static final String MEMBER_CARD_ASSIGN_ROLES_HELP = "manageMembers.card.edit-help"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "No users found.")
    public static final String USER_SELECTOR_NONE_FOUND = "userSelector.users.no-uses-found"; //$NON-NLS-1$

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

}
