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
package org.overlord.apiman.dt.ui.client.local.pages.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.overlord.apiman.dt.api.beans.idm.RoleBean;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Widget used to select an organization, typically on the various
 * "new" pages (e.g. New Application, New Service, etc...).
 *
 * @author eric.wittmann@redhat.com
 */
public class RoleMultiSelector extends ListBox implements HasValue<Set<String>> {

    private List<RoleBean> all;
    private Set<String> value;

    /**
     * Constructor.
     */
    public RoleMultiSelector() {
        addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                fireValueChangeEvent();
            }
        });
    }
    
    /**
     * Sets the organization choices available to the user for selection.
     * @param organizations
     */
    public void setRoles(final List<RoleBean> choices) {
        all = choices;
        for (RoleBean roleBean : choices) {
            this.addItem(roleBean.getName(), roleBean.getId());
        }
        initUI(getElement());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
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
        setValue(value, false);
        // mark the correct items as seleted in the control
        for (int i = 0; i < getItemCount(); i++) {
            String roleId = getValue(i);
            boolean shouldSelect = value.contains(roleId);
            setItemSelected(i, shouldSelect);
        }
        refreshUI(getElement());
    }

    /**
     * Returns true if a role with the given ID is included in the list.
     * @param roleId
     * @param roles
     */
    private static boolean isInList(String roleId, List<RoleBean> roles) {
        for (RoleBean roleBean : roles) {
            if (roleBean.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(Set<String> value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * Init the control as a bootstrap-select box.
     * @param elem
     */
    private native void initUI(Element elem) /*-{
        $wnd.jQuery(elem).selectpicker();
    }-*/;

    /**
     * Refresh the UI.  This is done when the values in the select box
     * are modified programmatically and the widget's UI needs to 
     * reflect the changed state.
     * @param elem
     */
    private native void refreshUI(Element elem) /*-{
        $wnd.jQuery(elem).selectpicker('render');
    }-*/;

    /**
     * Called when something changes.  This is used to gather up the
     * selected items and fire a proper value change event.
     */
    protected void fireValueChangeEvent() {
        Set<String> newValue = new HashSet<String>();
        for (int i = 0; i < getItemCount(); i++) {
            if (isItemSelected(i)) {
                newValue.add(getValue(i));
            }
        }
        setValue(newValue, true);
    }

}
