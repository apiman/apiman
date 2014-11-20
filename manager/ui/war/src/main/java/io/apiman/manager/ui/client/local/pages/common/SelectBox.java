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
package io.apiman.manager.ui.client.local.pages.common;

import java.util.List;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
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
            String dataContent = optionDataContent(option);
            if (value == null)
                this.addItem(name);
            else
                this.addItem(name, value);
            if (dataContent != null) {
                SelectElement select = getElement().cast();
                NodeList<OptionElement> o = select.getOptions();
                OptionElement item = o.getItem(o.getLength() - 1);
                item.setAttribute("data-content", dataContent); //$NON-NLS-1$
            }
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
    protected String optionValue(T option) {
        return null;
    }

    /**
     * @param option
     */
    protected String optionDataContent(T option) {
        return null;
    }

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
            setSelectedIndex(idx);
            setValue(value, false);
        } else {
            setSelectedIndex(0);
            setValue(options.get(0), false);
        }
        if (isAttached())
            renderUI(getElement());
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
     * Refresh the UI.  This is done when the select box's options
     * are modified programmatically (after it is initially built).
     * @param elem
     */
    private native void refreshUI(Element elem) /*-{
        $wnd.jQuery(elem).selectpicker('refresh');
    }-*/;

    /**
     * Re-render the UI.  This is done when the select box's value
     * is changed programmatically.  The UI will get updated to
     * match the new value.
     * @param elem
     */
    private native void renderUI(Element elem) /*-{
        $wnd.jQuery(elem).selectpicker('render');
    }-*/;

    /**
     * Called when something changes.
     */
    protected void fireValueChangeEvent() {
        int idx = this.getSelectedIndex();
        T newValue = options.get(idx);
        setValue(newValue, true);
    }

}
