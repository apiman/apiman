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
package org.overlord.apiman.dt.ui.client.local.pages.policy.forms;

import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.ui.client.local.pages.policy.IPolicyConfigurationForm;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A policy configuration form used for the IP whitelist and IP blacklist policies.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/policyconfig-ip-list.html#form")
@Dependent
public class IPListPolicyConfigurationForm extends Composite implements IPolicyConfigurationForm {
    
    @Inject @DataField
    ListBox ipAddresses;

    @Inject @DataField
    Button clear;
    @Inject @DataField
    Button remove;

    @Inject @DataField
    TextBox ipAddress;
    @Inject @DataField
    Button add;

    /**
     * Constructor.
     */
    public IPListPolicyConfigurationForm() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        clear.setEnabled(false);
        remove.setEnabled(false);
        add.setEnabled(false);
        ipAddress.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String val = ipAddress.getValue();
                add.setEnabled(!val.trim().isEmpty());
            }
        });
        ipAddresses.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                remove.setEnabled(ipAddresses.getSelectedIndex() != -1);
            }
        });
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        JSONObject root = new JSONObject();
        JSONArray array = new JSONArray();
        for (int idx = 0; idx < ipAddresses.getItemCount(); idx++) {
            String val = ipAddresses.getValue(idx);
            array.set(idx, new JSONString(val));
        }
        root.put("ipList", array); //$NON-NLS-1$
        return root.toString();
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
        ipAddresses.clear();
        clear.setEnabled(false);
        remove.setEnabled(false);
        add.setEnabled(false);
        ipAddress.setValue(""); //$NON-NLS-1$
        if (value != null && !value.trim().isEmpty()) {
            JSONValue jsonval = JSONParser.parseStrict(value);
            JSONObject jsonObject = jsonval.isObject();
            JSONValue ipList = jsonObject.get("ipList"); //$NON-NLS-1$
            TreeSet<String> sorted = new TreeSet<String>();
            if (ipList != null) {
                JSONArray array = ipList.isArray();
                for (int idx = 0; idx < array.size(); idx++) {
                    String ip = array.get(idx).isString().stringValue();
                    sorted.add(ip);
                }
            }
            for (String ip : sorted) {
                ipAddresses.addItem(ip);
            }
        }
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
        ipAddresses.clear();
        remove.setEnabled(false);
        clear.setEnabled(false);
    }
    
    /**
     * Called when the clear button is clicked.
     * @param event
     */
    @EventHandler("remove")
    protected void onRemove(ClickEvent event) {
        for (int idx = ipAddresses.getItemCount() - 1; idx >= 0; idx--) {
            if (ipAddresses.isItemSelected(idx)) {
                ipAddresses.removeItem(idx);
            }
        }
        remove.setEnabled(false);
        clear.setEnabled(ipAddresses.getItemCount() > 0);
    }
    
    /**
     * Called when the clear button is clicked.
     * @param event
     */
    @EventHandler("add")
    protected void onAdd(ClickEvent event) {
        String newIP = ipAddress.getValue();
        boolean inserted = false;
        for (int idx = 0; idx < ipAddresses.getItemCount(); idx++) {
            String v = ipAddresses.getValue(idx);
            if (newIP.compareTo(v) < 0) {
                ipAddresses.insertItem(newIP, idx);
                ipAddresses.setSelectedIndex(idx);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            ipAddresses.addItem(newIP);
            ipAddresses.setSelectedIndex(ipAddresses.getItemCount() - 1);
        }
        remove.setEnabled(true);
        clear.setEnabled(true);
        add.setEnabled(false);
        ipAddress.setValue(""); //$NON-NLS-1$
        ipAddress.setFocus(true);
    }

}
