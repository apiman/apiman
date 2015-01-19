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
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

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
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user adds a plugin.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/new-plugin.html#page")
@Page(path="new-plugin")
@Dependent
public class NewPluginPage extends AbstractPage {

    @Inject
    TransitionTo<AdminPluginsPage> toPlugins;
    
    @Inject @DataField
    TextBox groupId;
    @Inject @DataField
    TextBox artifactId;
    @Inject @DataField
    TextBox version;
    @Inject @DataField
    TextBox classifier;
    @Inject @DataField
    TextBox type;

    @Inject @DataField
    AsyncActionButton addButton;
    
    /**
     * Constructor.
     */
    public NewPluginPage() {
    }

    @PostConstruct
    protected void postConstruct() {
        KeyUpHandler handler = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                enableCreateButtonIfValid();
            }
        };
        groupId.addKeyUpHandler(handler);
        artifactId.addKeyUpHandler(handler);
        version.addKeyUpHandler(handler);
        classifier.addKeyUpHandler(handler);
        type.addKeyUpHandler(handler);
    }
    
    /**
     * Enables the create button only if the contents of the form are valid.
     */
    protected void enableCreateButtonIfValid() {
        List<String> values = new ArrayList<String>();
        values.add(groupId.getValue());
        values.add(artifactId.getValue());
        values.add(version.getValue());
        boolean valid = true;
        for (String value : values) {
            if (value == null || value.trim().length() == 0) {
                valid = false;
            }
        }
        addButton.setEnabled(valid);
    }

    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        groupId.setFocus(true);
        addButton.reset();
        addButton.setEnabled(false);
    }

    /**
     * Called when the user clicks the Add Plugin button.
     * @param event
     */
    @EventHandler("addButton")
    public void onAdd(ClickEvent event) {
        addButton.onActionStarted();
        PluginBean plugin = new PluginBean();
        plugin.setGroupId(groupId.getValue().trim());
        plugin.setArtifactId(artifactId.getValue().trim());
        plugin.setVersion(version.getValue().trim());
        if (classifier.getValue().trim().length() > 0) {
            plugin.setClassifier(classifier.getValue().trim());
        }
        if (type.getValue().trim().length() > 0) {
            plugin.setType(type.getValue().trim());
        }
        rest.createPlugin(plugin, new IRestInvokerCallback<PluginBean>() {
            @Override
            public void onSuccess(PluginBean response) {
                toPlugins.go();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_NEW_PLUGIN);
    }

}
