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
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.beans.PolicyDefinitionsBean;
import org.overlord.apiman.dt.ui.client.local.pages.admin.PolicyDefinitionTable;
import org.overlord.apiman.dt.ui.client.local.services.BeanMarshallingService;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextArea;


/**
 * Page that lets the user import a new policy definition.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/import-policyDef.html#page")
@Page(path="import-policyDef")
@Dependent
public class ImportPolicyDefPage extends AbstractPage {

    @Inject
    BeanMarshallingService marshaller;

    @Inject
    TransitionTo<AdminPolicyDefsPage> toPolicyDefs;
    
    @Inject @DataField
    TextArea data;
    @Inject @DataField
    PolicyDefinitionTable policyDefs;
    
    @Inject @DataField
    AsyncActionButton importButton;
    @Inject @DataField
    AsyncActionButton yesButton;
    
    List<PolicyDefinitionBean> beansToImport;
    
    /**
     * Constructor.
     */
    public ImportPolicyDefPage() {
    }

    /**
     * Post construct method.
     */
    @PostConstruct
    protected void postConstruct() {
        KeyUpHandler kph = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onFormChanged();
            }
        };
        data.addKeyUpHandler(kph);
        importButton.getElement().removeAttribute("onclick"); //$NON-NLS-1$
    }

    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        data.setFocus(true);
        importButton.reset();
        importButton.setEnabled(false);
    }
    
    /**
     * Called when the user clicks the Import button.
     * @param event
     */
    @EventHandler("importButton")
    public void onImport(ClickEvent event) {
        importButton.onActionStarted();
        List<PolicyDefinitionBean> parsedBeans = parseData();
        policyDefs.setValue(parsedBeans);
        beansToImport = parsedBeans;
        showConfirmationPage();
    }

    /**
     * Native helper method for showing the confirmation page.
     */
    private native void showConfirmationPage() /*-{
        $wnd.jQuery("#dataPage").animate( { "margin-left" : "-1000px" }, 250, function() { 
            $wnd.jQuery("#dataPage").hide();
            $wnd.jQuery("#confirmPage").show();
            $wnd.jQuery("#confirmPage").animate( { "margin-left" : "0px" }, 250);
        });
    }-*/;
    
    /**
     * Parse the data entered by the user into beans.
     */
    private List<PolicyDefinitionBean> parseData() {
        String data = this.data.getValue().trim();
        if (data.isEmpty()) {
            return null;
        }
        if (data.charAt(0) == '[') {
            PolicyDefinitionsBean bean = marshaller.unmarshal("{ \"definitions\" : " + data + " } ", PolicyDefinitionsBean.class); //$NON-NLS-1$ //$NON-NLS-2$
            return bean.getDefinitions();
        } else if (data.charAt(0) == '{') {
            PolicyDefinitionBean bean = marshaller.unmarshal(data, PolicyDefinitionBean.class);
            List<PolicyDefinitionBean> rval = new ArrayList<PolicyDefinitionBean>();
            rval.add(bean);
            return rval;
        }
        return null;
    }

    /**
     * Called whenever the user modifies the form.  Checks for form validity and then
     * enables or disables the Import button as appropriate.
     */
    protected void onFormChanged() {
        boolean formComplete = true;
        if (data.getValue() == null || data.getValue().trim().isEmpty()) {
            formComplete = false;
        }
        importButton.setEnabled(formComplete);
    }

    /**
     * Called when the user clicks Yes to confirm the import.
     * @param event
     */
    @EventHandler("yesButton")
    public void onConfirm(ClickEvent event) {
        yesButton.onActionStarted();
        final List<PolicyDefinitionBean> completedImports = new ArrayList<PolicyDefinitionBean>();
        for (PolicyDefinitionBean policyDefinitionBean : beansToImport) {
            rest.createPolicyDefinition(policyDefinitionBean, new IRestInvokerCallback<PolicyDefinitionBean>() {
                @Override
                public void onSuccess(PolicyDefinitionBean response) {
                    completedImports.add(response);
                    if (completedImports.size() == beansToImport.size()) {
                        toPolicyDefs.go();
                    }
                }
                @Override
                public void onError(Throwable error) {
                    completedImports.add(null);
                    if (completedImports.size() == beansToImport.size()) {
                        toPolicyDefs.go();
                    }
                }
            });
        }
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_IMPORT_POLICY_DEF);
    }

}
