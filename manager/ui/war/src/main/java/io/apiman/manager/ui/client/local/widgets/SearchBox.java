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
package io.apiman.manager.ui.client.local.widgets;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A simple search box.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/user-orgs.html#orgFilter")
@Dependent
public class SearchBox extends Composite implements HasValue<String> {

    @Inject @DataField
    TextBox textBox;
    @Inject @DataField
    Button button;
    
    private String oldValue = ""; //$NON-NLS-1$
    private boolean hasValue = false;
    
    /**
     * Constructor.
     */
    public SearchBox() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        setPlaceholder(""); //$NON-NLS-1$
        textBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    doEnter();
                }
            }
        });
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (hasValue) {
                    textBox.setValue(""); //$NON-NLS-1$
                    textBox.setFocus(true);
                } else {
                    textBox.setFocus(false);
                }
                doEnter();
            }
        });
    }

    /**
     * Sets the placeholder text.
     * @param text
     */
    public void setPlaceholder(String text) {
        textBox.getElement().setAttribute("placeholder", text); //$NON-NLS-1$
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
        return textBox.getValue();
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
        textBox.setValue(value, fireEvents);
    }

    /**
     * Called when the user clicks the Search button or hits the ENTER key.
     */
    protected void doEnter() {
        String newValue = textBox.getValue();
        ValueChangeEvent.fireIfNotEqual(SearchBox.this, oldValue, newValue);
        oldValue = newValue;
        hasValue = !newValue.isEmpty();
        if (hasValue) {
            button.setHTML("<i class=\"fa fa-fw fa-times\"></i>"); //$NON-NLS-1$
        } else {
            button.setHTML("<i class=\"fa fa-fw fa-search\"></i>"); //$NON-NLS-1$
        }
    }
}
