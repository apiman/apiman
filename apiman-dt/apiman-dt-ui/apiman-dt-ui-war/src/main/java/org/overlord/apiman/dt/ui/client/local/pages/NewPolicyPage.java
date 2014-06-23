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
package org.overlord.apiman.dt.ui.client.local.pages;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.policy.PolicyTypeSelectBox;
import org.overlord.apiman.dt.ui.client.shared.beans.PolicyTypeBean;


/**
 * Page that lets the user create a new Policy.  This page allows the user
 * to create a policy for applications, services, and plans.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/new-policy.html#page")
@Page(path="add-policy")
@Dependent
public class NewPolicyPage extends AbstractPage {

    @PageState
    String org;
    @PageState
    String id;
    @PageState
    String ver;
    @PageState
    String type;
    
    @DataField @Inject
    PolicyTypeSelectBox typeSelector;
    
    /**
     * Constructor.
     */
    public NewPolicyPage() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        List<PolicyTypeBean> policyTypes = new ArrayList<PolicyTypeBean>();
        policyTypes.add(new PolicyTypeBean("Choose a policy type...", null, null));
        policyTypes.add(new PolicyTypeBean("Authentication Policy", "org.overlord.policies.AuthenticationPolicyImpl", "lock"));
        policyTypes.add(new PolicyTypeBean("Rate Limiting Policy", "org.overlord.policies.AuthenticationPolicyImpl", "arrow-circle-right"));
        policyTypes.add(new PolicyTypeBean("User Defined (Custom) Policy", "user", "puzzle-piece"));
        typeSelector.setOptions(policyTypes);
    }    

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_NEW_POLICY);
    }

}
