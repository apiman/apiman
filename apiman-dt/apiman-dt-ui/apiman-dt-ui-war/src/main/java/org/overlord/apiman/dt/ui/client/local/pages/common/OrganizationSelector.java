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

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.commons.gwt.client.local.widgets.UnorderedListPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * Widget used to select an organization, typically on the various
 * "new" pages (e.g. New Application, New Service, etc...).
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/new-app.html#orgSelector")
@Dependent
public class OrganizationSelector extends Composite implements HasValue<OrganizationSummaryBean>, Focusable {

    @Inject @DataField
    Button button;
    @Inject @DataField
    InlineLabel selectorLabel;
    @Inject @DataField
    UnorderedListPanel organizations;
    
    private OrganizationSummaryBean value;

    /**
     * Constructor.
     */
    public OrganizationSelector() {
    }
    
    /**
     * Sets the organization choices available to the user for selection.
     * @param organizations
     */
    public void setOrganizations(final List<OrganizationSummaryBean> choices) {
        organizations.clear();
        if (choices == null)
            return;
        for (final OrganizationSummaryBean org : choices) {
            Anchor a = new Anchor(org.getName());
            a.setHref("#"); //$NON-NLS-1$
            a.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    setValue(org, true);
                    event.preventDefault();
                }
            });
            organizations.add(a);
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<OrganizationSummaryBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public OrganizationSummaryBean getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(OrganizationSummaryBean value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(OrganizationSummaryBean value, boolean fireEvents) {
        OrganizationSummaryBean oldValue = this.value;
        this.value = value;
        selectorLabel.setText(value.getName());
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.Focusable#getTabIndex()
     */
    @Override
    public int getTabIndex() {
        return button.getTabIndex();
    }

    /**
     * @see com.google.gwt.user.client.ui.Focusable#setAccessKey(char)
     */
    @Override
    public void setAccessKey(char key) {
        button.setAccessKey(key);
    }

    /**
     * @see com.google.gwt.user.client.ui.Focusable#setFocus(boolean)
     */
    @Override
    public void setFocus(boolean focused) {
        button.setFocus(focused);
    }

    /**
     * @see com.google.gwt.user.client.ui.Focusable#setTabIndex(int)
     */
    @Override
    public void setTabIndex(int index) {
        button.setTabIndex(index);
    }
}
