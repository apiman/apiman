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
package io.apiman.manager.ui.client.local.pages.dash;

import io.apiman.manager.ui.client.local.pages.AdminGatewaysPage;
import io.apiman.manager.ui.client.local.pages.AdminPolicyDefsPage;
import io.apiman.manager.ui.client.local.pages.AdminRolesPage;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;

/**
 * The panel added to the dashboard if the user is an admin.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/dash.html#adminDashPanel")
@Dependent
public class AdminDashPanel extends Composite {
    
    @Inject @DataField
    TransitionAnchor<AdminRolesPage> manageRoles;
    @Inject @DataField
    TransitionAnchor<AdminPolicyDefsPage> managePolicyDefs;
    @Inject @DataField
    TransitionAnchor<AdminGatewaysPage> manageGateways;

    /**
     * Constructor.
     */
    public AdminDashPanel() {
    }

}
