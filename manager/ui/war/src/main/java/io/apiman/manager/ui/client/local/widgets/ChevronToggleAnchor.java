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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasValue;

/**
 * A simple toggle anchor that uses the font awesome chevron icon to display
 * the state of the toggle.
 *
 * @author eric.wittmann@redhat.com
 */
public class ChevronToggleAnchor extends Anchor implements HasValue<Boolean> {
    
    private boolean open = false;

    /**
     * Constructor.
     */
    public ChevronToggleAnchor() {
        setHTML(generateHTML(open));
        setHref("#"); //$NON-NLS-1$
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.preventDefault();
                event.stopPropagation();
                
                boolean newValue = !open;
                setHTML(generateHTML(newValue));
                setValue(newValue, true);
            }
        });
    }

    /**
     * @param isToggleOpen
     */
    private static String generateHTML(boolean isToggleOpen) {
        if (isToggleOpen) {
            return "<i class=\"fa fa-fw fa-chevron-down\"></i>"; //$NON-NLS-1$
        } else {
            return "<i class=\"fa fa-fw fa-chevron-right\"></i>"; //$NON-NLS-1$
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public Boolean getValue() {
        return open;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Boolean value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        this.open = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, open);
        }
    }
}
