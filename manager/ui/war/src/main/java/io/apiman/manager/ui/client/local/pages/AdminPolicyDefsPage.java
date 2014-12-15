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
package io.apiman.manager.ui.client.local.pages;

import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.admin.PolicyDefinitionTable;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;
import io.apiman.manager.ui.client.local.widgets.SearchBox;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;

/**
 * The Role Management admin page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/admin-policyDefs.html#page")
@Page(path="admin-policyDefs")
@Dependent
public class AdminPolicyDefsPage extends AbstractAdminPage {
    
    @Inject @DataField
    SearchBox policyDefFilter;
    @Inject @DataField
    Anchor toImportPolicyDef;
    @Inject @DataField
    PolicyDefinitionTable policyDefs;

    List<PolicyDefinitionSummaryBean> policyDefBeans;

    /**
     * Constructor.
     */
    public AdminPolicyDefsPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
        policyDefFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                filterPolicyDefs();
            }
        });
        policyDefFilter.setPlaceholder(i18n.format(AppMessages.ADMIN_ROLES_FILTER_PLACEHOLDER));
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.listPolicyDefinitions(new IRestInvokerCallback<List<PolicyDefinitionSummaryBean>>() {
            @Override
            public void onSuccess(List<PolicyDefinitionSummaryBean> response) {
                policyDefBeans = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval + 1;
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractAdminPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();

        String importPolicyDefHref = navHelper.createHrefToPage(ImportPolicyDefPage.class, MultimapUtil.emptyMap());
        toImportPolicyDef.setHref(importPolicyDefHref);
        
        filterPolicyDefs();
    }

    /**
     * Apply a filter to the list of applications.
     */
    protected void filterPolicyDefs() {
        if (policyDefFilter.getValue() == null || policyDefFilter.getValue().trim().length() == 0) {
            policyDefs.setValue(policyDefBeans);
        } else {
            List<PolicyDefinitionSummaryBean> filtered = new ArrayList<PolicyDefinitionSummaryBean>();
            for (PolicyDefinitionSummaryBean policyDef : policyDefBeans) {
                if (matchesFilter(policyDef)) {
                    filtered.add(policyDef);
                }
            }
            policyDefs.setFilteredValue(filtered);
        }
    }

    /**
     * Returns true if the given policyDef matches the current filter.
     * @param policyDef
     */
    private boolean matchesFilter(PolicyDefinitionSummaryBean policyDef) {
        if (policyDef.getName().toUpperCase().contains(policyDefFilter.getValue().toUpperCase())) {
            return true;
        }
        return false;
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_ADMIN_POLICY_DEFS);
    }

}
