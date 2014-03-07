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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;

/**
 * A select box using bootstrap-select as the UI enhancement.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class SelectBox<T> extends ListBox implements HasValue<T> {
    
    private T value;
    private List<T> options;
    
    /**
     * Constructor.
     */
    public SelectBox() {
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    initUI(getElement());
                }
            }
        });
        addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                fireValueChangeEvent();
            }
        });
    }
    
    /**
     * Sets the select box's options.
     * @param options
     */
    public void setOptions(List<T> options) {
        clear();
        this.options = options;
        for (T option : options) {
            String name = optionName(option);
            String value = optionValue(option);
            this.addItem(name, value);
        }
        if (isAttached()) {
            refreshUI(getElement());
        }
    }

    /**
     * @param option
     */
    protected abstract String optionName(T option);

    /**
     * @param option
     */
    protected abstract String optionValue(T option);

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(T value) {
        int idx = indexOf(value);
        if (idx != -1) {
            setValue(value, false);
            setSelectedIndex(idx);
            refreshUI(getElement());
        } else {
            setSelectedIndex(0);
            setValue(options.get(0));
        }
    }

    /**
     * Returns the index of the given item or -1 if not found.
     * @param value
     */
    protected int indexOf(T value) {
        if (value == null)
            return -1;
        String itemValue = optionValue(value);
        for (int i = 0; i < getItemCount(); i++) {
            String rowValue = getValue(i);
            if (rowValue.equals(itemValue)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(T value, boolean fireEvents) {
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
     * Called when something changes.
     */
    protected void fireValueChangeEvent() {
        int idx = this.getSelectedIndex();
        String value = this.getValue(idx);
        T newValue = null;
        for (T option : options) {
            String optionVal = optionValue(option);
            if (value.equals(optionVal)) {
                newValue = option;
            }
        }
        setValue(newValue, true);
    }

}
