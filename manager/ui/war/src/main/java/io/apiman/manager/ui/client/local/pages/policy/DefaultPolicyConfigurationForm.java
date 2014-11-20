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
package io.apiman.manager.ui.client.local.pages.policy;

import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.ClientMessages;
import io.apiman.manager.ui.client.local.events.IsFormValidEvent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * A default implementation of a policy configuration form.  This implementation
 * is a simple text editor to let the user manually configure (via valid JSON) the
 * policy.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class DefaultPolicyConfigurationForm extends FlowPanel implements IPolicyConfigurationForm {
    
    @Inject
    private ClientMessages i18n;
    
    private TextArea text;

    /**
     * Constructor.
     */
    public DefaultPolicyConfigurationForm() {
    }
    
    /**
     * Post-construct.
     */
    @PostConstruct
    protected void postConstruct() {
        getElement().addClassName("form"); //$NON-NLS-1$
        getElement().addClassName("policy-config"); //$NON-NLS-1$
        getElement().addClassName("default"); //$NON-NLS-1$
        add(new InlineLabel(i18n.format(AppMessages.DEFAULT_POLICY_FORM_HELP)));
        text = new TextArea();
        add(text);
    }
    
    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return text.addValueChangeHandler(handler);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        return text.getValue();
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
        text.setValue(value, fireEvents);
        
        IsFormValidEvent.fire(this, Boolean.TRUE);
    }

    /**
     * @see io.apiman.manager.ui.client.local.events.IsFormValidEvent.HasIsFormValidHandlers#addIsFormValidHandler(io.apiman.manager.ui.client.local.events.IsFormValidEvent.Handler)
     */
    @Override
    public HandlerRegistration addIsFormValidHandler(IsFormValidEvent.Handler handler) {
        return addHandler(handler, IsFormValidEvent.getType());
    }

}
