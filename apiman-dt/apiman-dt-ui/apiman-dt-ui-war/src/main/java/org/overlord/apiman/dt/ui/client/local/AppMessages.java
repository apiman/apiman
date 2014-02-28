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
    @TranslationKey(defaultValue = "APIMan - New Service")
    public static final String TITLE_NEW_SERVICE = "page.title.new-service"; //$NON-NLS-1$
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

    @TranslationKey(defaultValue = "Configuration service not available.")
    public static final String CONFIG_SERVICE_NOT_AVAILABLE = "authInterceptor.configServiceNotAvailable"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "It looks like there aren't (yet) any applications in this organization! Now might be a good time to click the New App button up above...")
    public static final String NO_APPS_IN_ORG_MESSAGE = "noEntitiesFound.applications-in-org"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "We couldn't find any services in this organization.  Probably because none exist.  We hope.  Try creating one using the New Service button.")
    public static final String NO_SERVICES_IN_ORG_MESSAGE = "noEntitiesFound.services-in-org"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "There aren't any plans configured for this organization.  That means all published services in this organization will be considered public.")
    public static final String NO_PLANS_IN_ORG_MESSAGE = "noEntitiesFound.plans-in-org"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "You aren't a member of any organizations yet!  You'll need to either be granted membership in an existing organization by its admin or else create your own.")
    public static final String NO_ORGS_FOR_USER_MESSAGE = "noEntitiesFound.organizations-for-user"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "You are not yet managing any applications.  Perhaps that's just not your thing.  But if it is, you can create a new Application using the New App button!")
    public static final String NO_APPS_FOR_USER_MESSAGE = "noEntitiesFound.applications-for-user"; //$NON-NLS-1$
    @TranslationKey(defaultValue = "It looks like you aren't responsible for any services at all.  Maybe you're just all about the applications?  If not, maybe try creating a New Service.")
    public static final String NO_SERVICES_FOR_USER_MESSAGE = "noEntitiesFound.services-for-user"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Assign roles to {0}.")
    public static final String MEMBER_CARD_ASSIGN_ROLES_HELP = "manageMembers.card.edit-help"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "No users found.")
    public static final String USER_SELECTOR_NONE_FOUND = "userSelector.users.no-uses-found"; //$NON-NLS-1$

    @TranslationKey(defaultValue = "Search for users above")
    public static final String NEW_MEMBER_SEARCH_TEXT = "newMember.userSelector.help-text"; //$NON-NLS-1$
   
}
