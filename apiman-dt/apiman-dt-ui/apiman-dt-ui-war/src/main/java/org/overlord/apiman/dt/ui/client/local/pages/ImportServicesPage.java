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
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.beans.ServiceImportSourceType;
import org.overlord.apiman.dt.ui.client.local.pages.service.ServiceImportSourceSelectBox;
import org.overlord.apiman.dt.ui.client.local.widgets.DropZone;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user import a new policy definition.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/import-services.html#page")
@Page(path="import-services")
@Dependent
public class ImportServicesPage extends AbstractPage {
    
    private static final List<ServiceImportSourceType> IMPORT_TYPES = new ArrayList<ServiceImportSourceType>();
    static {
        IMPORT_TYPES.add(ServiceImportSourceType.Wadl);
    }

    @Inject
    TransitionTo<OrgServicesPage> toOrgServices;

    // Import Type/Source Page
    @Inject @DataField
    ServiceImportSourceSelectBox importSource;
    @Inject @DataField
    Anchor importTypeNext;
    
    // WADL Page
    @Inject @DataField
    TextBox wadlUrl;
    @Inject @DataField
    FileUpload wadlFile;
    @Inject @DataField
    DropZone wadlDropZone;
    @Inject @DataField
    AsyncActionButton wadlNext;

    // Confirmation Page
    @Inject @DataField
    AsyncActionButton yesButton;

    /**
     * Constructor.
     */
    public ImportServicesPage() {
    }

    /**
     * Post construct method.
     */
    @PostConstruct
    protected void postConstruct() {
        importSource.setOptions(IMPORT_TYPES);
        importTypeNext.getElement().removeAttribute("onclick"); //$NON-NLS-1$
        wadlNext.setEnabled(false);
        wadlUrl.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (wadlUrl.getValue() != null && wadlUrl.getValue().trim().length() > 0) {
                    wadlNext.setEnabled(true);
                }
            }
        });
        wadlFile.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (wadlFile.getFilename() != null && wadlFile.getFilename().trim().length() > 0) {
                    wadlNext.setEnabled(true);
                }
            }
        });
    }

    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
    }

    /**
     * Called when the user clicks Next on the import source/type page of the wizard.
     * @param event
     */
    @EventHandler("importTypeNext")
    public void onImportTypeNext(ClickEvent event) {
        showWadlPage();
    }

    /**
     * Called when the user clicks Next on the wadl page of the wizard.
     * @param event
     */
    @EventHandler("wadlNext")
    public void onWadlNext(ClickEvent event) {
        wadlNext.onActionStarted();
    }

    /**
     * Native helper method for showing the wadl page.
     */
    private native void showWadlPage() /*-{
        $wnd.jQuery("#importTypePage").animate( { "margin-left" : "-2000px" }, 200, function() { 
            $wnd.jQuery("#importTypePage").hide();
            $wnd.jQuery("#wadlPage").show();
            $wnd.jQuery("#wadlPage").animate( { "margin-left" : "0px" }, 200);
        });
    }-*/;
    
    /**
     * Native helper method for showing the confirmation page.
     */
    private native void showConfirmationPage(String fromPage) /*-{
        $wnd.jQuery("#" + fromPage).animate( { "margin-left" : "-2000px" }, 200, function() { 
            $wnd.jQuery("#" + fromPage).hide();
            $wnd.jQuery("#confirmPage").show();
            $wnd.jQuery("#confirmPage").animate( { "margin-left" : "0px" }, 200);
        });
    }-*/;

    /**
     * Called when the user clicks Yes to confirm the import.
     * @param event
     */
    @EventHandler("yesButton")
    public void onConfirm(ClickEvent event) {
        yesButton.onActionStarted();
        yesButton.onActionComplete();
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_IMPORT_SERVICES);
    }

}
