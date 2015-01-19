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

import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent.Handler;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.widgets.ConfirmationDialog;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.InlineLabel;


/**
 * Page that lets the user edit (or delete) a Plugin.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/edit-plugin.html#page")
@Page(path="edit-plugin")
@Dependent
public class EditPluginPage extends AbstractPage {

    @Inject
    TransitionTo<AdminPluginsPage> toPlugins;

    @PageState
    String id;

    @Inject @DataField
    InlineLabel groupId;
    @Inject @DataField
    InlineLabel artifactId;
    @Inject @DataField
    InlineLabel version;
    @Inject @DataField
    InlineLabel classifier;
    @Inject @DataField
    InlineLabel type;

    @Inject @DataField
    InlineLabel name;
    @Inject @DataField
    InlineLabel description;

    @Inject @DataField
    AsyncActionButton deleteButton;

    PluginBean pluginBean;
    
    /**
     * Constructor.
     */
    public EditPluginPage() {
    }
    
    @PostConstruct
    protected void postConstruct() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getPlugin(Long.valueOf(id), new IRestInvokerCallback<PluginBean>() {
            @Override
            public void onSuccess(PluginBean response) {
                pluginBean = response;
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
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        deleteButton.reset();
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        name.setText(pluginBean.getName());
        if (pluginBean.getDescription() != null) {
            description.setText(pluginBean.getDescription());
        }
        groupId.setText(pluginBean.getGroupId());
        artifactId.setText(pluginBean.getArtifactId());
        version.setText(pluginBean.getVersion());
        if (pluginBean.getClassifier() != null) {
            classifier.setText(pluginBean.getClassifier());
        }
        if (pluginBean.getType() != null) {
            type.setText(pluginBean.getType());
        } else {
            type.setText("war"); //$NON-NLS-1$
        }
    }

    /**
     * Called when the user clicks the Delete Plugin button.
     * @param event
     */
    @EventHandler("deleteButton")
    public void onDelete(ClickEvent event) {
        deleteButton.onActionStarted();
        
        ConfirmationDialog dialog = confirmationDialogFactory.get();
        dialog.setDialogTitle(i18n.format(AppMessages.CONFIRM_PLUGIN_DELETE_TITLE));
        dialog.setDialogMessage(i18n.format(AppMessages.CONFIRM_PLUGIN_DELETE_MESSAGE, pluginBean.getName()));
        dialog.addConfirmationHandler(new Handler() {
            @Override
            public void onConfirmation(ConfirmationEvent event) {
                if (event.isConfirmed()) {
                    PluginBean plugin = new PluginBean();
                    plugin.setId(Long.valueOf(id));
                    rest.deletePlugin(plugin, new IRestInvokerCallback<Void>() {
                        @Override
                        public void onSuccess(Void response) {
                            toPlugins.go();
                        }
                        @Override
                        public void onError(Throwable error) {
                            dataPacketError(error);
                        }
                    });
                } else {
                    deleteButton.reset();
                }
            }
        });
        dialog.show();
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_EDIT_PLUGIN);
    }

}
