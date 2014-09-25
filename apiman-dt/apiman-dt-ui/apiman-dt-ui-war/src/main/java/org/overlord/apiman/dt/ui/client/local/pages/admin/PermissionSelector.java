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
package org.overlord.apiman.dt.ui.client.local.pages.admin;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Allows the user to choose permissions for a role.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/new-role.html#permissions")
public class PermissionSelector extends Composite implements HasValue<Set<String>> {
    
    @Inject @DataField
    CheckBox orgView;
    @Inject @DataField
    CheckBox orgEdit;
    @Inject @DataField
    CheckBox orgAdmin;

    @Inject @DataField
    CheckBox planView;
    @Inject @DataField
    CheckBox planEdit;
    @Inject @DataField
    CheckBox planAdmin;

    @Inject @DataField
    CheckBox svcView;
    @Inject @DataField
    CheckBox svcEdit;
    @Inject @DataField
    CheckBox svcAdmin;

    @Inject @DataField
    CheckBox appView;
    @Inject @DataField
    CheckBox appEdit;
    @Inject @DataField
    CheckBox appAdmin;

    private Set<String> value;
    
    /**
     * Constructor.
     */
    public PermissionSelector() {
    }
    
    /**
     * Bind some events.
     */
    @PostConstruct
    protected void postConstrct() {
        ValueChangeHandler<Boolean> handler = new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                Set<String> permissions = new HashSet<String>();
                if (orgView.getValue()) { permissions.add(PermissionType.orgView.toString()); }
                if (orgEdit.getValue()) { permissions.add(PermissionType.orgEdit.toString()); }
                if (orgAdmin.getValue()) { permissions.add(PermissionType.orgAdmin.toString()); }

                if (planView.getValue()) { permissions.add(PermissionType.planView.toString()); }
                if (planEdit.getValue()) { permissions.add(PermissionType.planEdit.toString()); }
                if (planAdmin.getValue()) { permissions.add(PermissionType.planAdmin.toString()); }

                if (svcView.getValue()) { permissions.add(PermissionType.svcView.toString()); }
                if (svcEdit.getValue()) { permissions.add(PermissionType.svcEdit.toString()); }
                if (svcAdmin.getValue()) { permissions.add(PermissionType.svcAdmin.toString()); }

                if (appView.getValue()) { permissions.add(PermissionType.appView.toString()); }
                if (appEdit.getValue()) { permissions.add(PermissionType.appEdit.toString()); }
                if (appAdmin.getValue()) { permissions.add(PermissionType.appAdmin.toString()); }
                
                setValue(permissions, true);
            }
        };
        orgView.addValueChangeHandler(handler);
        orgEdit.addValueChangeHandler(handler);
        orgAdmin.addValueChangeHandler(handler);

        planView.addValueChangeHandler(handler);
        planEdit.addValueChangeHandler(handler);
        planAdmin.addValueChangeHandler(handler);

        svcView.addValueChangeHandler(handler);
        svcEdit.addValueChangeHandler(handler);
        svcAdmin.addValueChangeHandler(handler);

        appView.addValueChangeHandler(handler);
        appEdit.addValueChangeHandler(handler);
        appAdmin.addValueChangeHandler(handler);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<String>> handler) {
        return super.addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public Set<String> getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Set<String> value) {
        if (value == null) {
            value = new HashSet<String>();
        }
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(Set<String> value, boolean fireEvents) {
        Set<String> oldValue = this.value;
        this.value = value;
        refresh();
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
        }
    }

    /**
     * Refresh the UI.
     */
    private void refresh() {
        orgView.setValue(value.contains(PermissionType.orgView.toString()));
        orgEdit.setValue(value.contains(PermissionType.orgEdit.toString()));
        orgAdmin.setValue(value.contains(PermissionType.orgAdmin.toString()));

        planView.setValue(value.contains(PermissionType.planView.toString()));
        planEdit.setValue(value.contains(PermissionType.planEdit.toString()));
        planAdmin.setValue(value.contains(PermissionType.planAdmin.toString()));

        svcView.setValue(value.contains(PermissionType.svcView.toString()));
        svcEdit.setValue(value.contains(PermissionType.svcEdit.toString()));
        svcAdmin.setValue(value.contains(PermissionType.svcAdmin.toString()));

        appView.setValue(value.contains(PermissionType.appView.toString()));
        appEdit.setValue(value.contains(PermissionType.appEdit.toString()));
        appAdmin.setValue(value.contains(PermissionType.appAdmin.toString()));
    }

}
