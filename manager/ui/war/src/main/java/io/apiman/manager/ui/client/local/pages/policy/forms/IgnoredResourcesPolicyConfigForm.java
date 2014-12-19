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
package io.apiman.manager.ui.client.local.pages.policy.forms;

import io.apiman.gateway.engine.policies.config.IgnoredResourcesConfig;
import io.apiman.manager.ui.client.local.events.IsFormValidEvent;
import io.apiman.manager.ui.client.local.pages.policy.IPolicyConfigurationForm;
import io.apiman.manager.ui.client.local.services.BeanMarshallingService;

import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A policy configuration form used for the Ignored Resources policy.
 *
 * @author rubenrm1@gmail.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/policyconfig-ignoredresources.html#form")
@Dependent
public class IgnoredResourcesPolicyConfigForm extends Composite implements IPolicyConfigurationForm {

    @Inject
    BeanMarshallingService marshaller;
    
    @Inject @DataField
    ListBox pathsToIgnore;

    @Inject @DataField
    Button clear;
    @Inject @DataField
    Button remove;

    @Inject @DataField
    TextBox pathToIgnore;
    @Inject @DataField
    Button add;

    /**
     * Constructor.
     */
    public IgnoredResourcesPolicyConfigForm() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        clear.setEnabled(false);
        remove.setEnabled(false);
        add.setEnabled(false);
        pathToIgnore.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String val = pathToIgnore.getValue();
                add.setEnabled(!val.trim().isEmpty());
            }
        });
        pathsToIgnore.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                remove.setEnabled(pathsToIgnore.getSelectedIndex() != -1);
            }
        });
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        IgnoredResourcesConfig config = new IgnoredResourcesConfig();
        for (int idx = 0; idx < pathsToIgnore.getItemCount(); idx++) {
            String val = pathsToIgnore.getValue(idx);
            config.getPathsToIgnore().add(val);
        }
        return marshaller.marshal(config);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(String value, boolean fireEvents) {
        pathsToIgnore.clear();
        clear.setEnabled(false);
        remove.setEnabled(false);
        add.setEnabled(false);
        pathToIgnore.setValue(""); //$NON-NLS-1$
        if (value != null && !value.trim().isEmpty()) {
            IgnoredResourcesConfig config = marshaller.unmarshal(value, IgnoredResourcesConfig.class);
            
            TreeSet<String> sorted = new TreeSet<String>();
            if (config.getPathsToIgnore() != null && !config.getPathsToIgnore().isEmpty()) {
                sorted.addAll(config.getPathsToIgnore());
                clear.setEnabled(true);
            }
            for (String ip : sorted) {
                pathsToIgnore.addItem(ip);
            }
        }
        IsFormValidEvent.fire(this, Boolean.TRUE);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
    
    /**
     * Called when the clear button is clicked.
     * @param event
     */
    @EventHandler("clear")
    protected void onClear(ClickEvent event) {
        pathsToIgnore.clear();
        remove.setEnabled(false);
        clear.setEnabled(false);
    }
    
    /**
     * Called when the clear button is clicked.
     * @param event
     */
    @EventHandler("remove")
    protected void onRemove(ClickEvent event) {
        for (int idx = pathsToIgnore.getItemCount() - 1; idx >= 0; idx--) {
            if (pathsToIgnore.isItemSelected(idx)) {
                pathsToIgnore.removeItem(idx);
            }
        }
        remove.setEnabled(false);
        clear.setEnabled(pathsToIgnore.getItemCount() > 0);
    }
    
    /**
     * Called when the clear button is clicked.
     * @param event
     */
    @EventHandler("add")
    protected void onAdd(ClickEvent event) {
        String newPath = pathToIgnore.getValue();
        boolean inserted = false;
        for (int idx = 0; idx < pathsToIgnore.getItemCount(); idx++) {
            String v = pathsToIgnore.getValue(idx);
            if (newPath.compareTo(v) < 0) {
                pathsToIgnore.insertItem(newPath, idx);
                pathsToIgnore.setSelectedIndex(idx);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            pathsToIgnore.addItem(newPath);
            pathsToIgnore.setSelectedIndex(pathsToIgnore.getItemCount() - 1);
        }
        remove.setEnabled(true);
        clear.setEnabled(true);
        add.setEnabled(false);
        pathToIgnore.setValue(""); //$NON-NLS-1$
        pathToIgnore.setFocus(true);
    }

    /**
     * @see io.apiman.manager.ui.client.local.events.IsFormValidEvent.HasIsFormValidHandlers#addIsFormValidHandler(io.apiman.manager.ui.client.local.events.IsFormValidEvent.Handler)
     */
    @Override
    public HandlerRegistration addIsFormValidHandler(IsFormValidEvent.Handler handler) {
        return addHandler(handler, IsFormValidEvent.getType());
    }

}