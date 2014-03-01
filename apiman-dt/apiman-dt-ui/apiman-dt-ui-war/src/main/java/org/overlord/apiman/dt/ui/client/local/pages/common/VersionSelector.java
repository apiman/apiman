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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.commons.gwt.client.local.widgets.UnorderedListPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Widget used to select a version of an application or service.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class VersionSelector extends FlowPanel implements HasValue<String>, Focusable {
    
    @Inject
    TranslationService i18n;

    @Inject
    Button button;
    @Inject
    UnorderedListPanel versions;
    
    private String value;

    /**
     * Constructor.
     */
    public VersionSelector() {
        getElement().setClassName("btn-group"); //$NON-NLS-1$
    }
    
    @PostConstruct
    protected void postConstruct() {
        button.getElement().setClassName("btn"); //$NON-NLS-1$
        button.getElement().addClassName("btn-default"); //$NON-NLS-1$
        button.getElement().addClassName("dropdown-toggle"); //$NON-NLS-1$
        button.getElement().setAttribute("data-toggle", "dropdown"); //$NON-NLS-1$ //$NON-NLS-2$
        button.getElement().setAttribute("type", "button"); //$NON-NLS-1$ //$NON-NLS-2$
        
        versions.getElement().setClassName("dropdown-menu"); //$NON-NLS-1$
        
        add(button);
        add(versions);
        
        button.setHTML(createLabel("")); //$NON-NLS-1$
    }
    
    /**
     * Sets the organization choices available to the user for selection.
     * @param organizations
     */
    public void setVersions(final List<String> choices) {
        versions.clear();
        if (choices == null)
            return;
        for (final String ver : choices) {
            Anchor a = new Anchor(ver);
            a.setHref("#"); //$NON-NLS-1$
            a.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    setValue(ver, true);
                    event.preventDefault();
                }
            });
            versions.add(a);
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
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        return value;
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
        String oldValue = this.value;
        this.value = value;
        button.setHTML(createLabel(value));
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
        }
    }

    /**
     * Creates the label.
     * @param value
     */
    private String createLabel(String value) {
        return i18n.format(AppMessages.VERSION_SELECTOR_LABEL, value) + " <span class=\"caret\"></span>"; //$NON-NLS-1$
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
