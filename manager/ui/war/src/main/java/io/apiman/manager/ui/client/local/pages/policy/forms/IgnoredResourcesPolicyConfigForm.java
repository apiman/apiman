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
package io.apiman.manager.ui.client.local.pages.policy.forms;

import io.apiman.gateway.engine.policies.config.IgnoredResourcesConfig;
import io.apiman.manager.ui.client.local.events.IsFormValidEvent;
import io.apiman.manager.ui.client.local.pages.policy.IPolicyConfigurationForm;
import io.apiman.manager.ui.client.local.services.BeanMarshallingService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A policy configuration form used for the Ignored Resources policy.
 *
 * @author rubenrm1@gmail.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/policyconfig-ignoredresources.html#form")
@Dependent
public class IgnoredResourcesPolicyConfigForm extends Composite implements IPolicyConfigurationForm {

    @Inject
    BeanMarshallingService marshaller;
    
    @Inject @DataField
    TextBox pathToIgnore;

    /**
     * Constructor.
     */
    public IgnoredResourcesPolicyConfigForm() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        KeyUpHandler keyUpValidityHandler = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                checkValidity();
            }
        };
        ValueChangeHandler<String> valueChangeHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                checkValidity();
            }
        };
        pathToIgnore.addKeyUpHandler(keyUpValidityHandler);
        pathToIgnore.addValueChangeHandler(valueChangeHandler);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        IgnoredResourcesConfig config = new IgnoredResourcesConfig();
        config.setPathToIgnore(pathToIgnore.getValue());

        return marshaller.marshal(config);
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
        if (value != null && !value.trim().isEmpty()) {
        	IgnoredResourcesConfig config = marshaller.unmarshal(value, IgnoredResourcesConfig.class);
            pathToIgnore.setValue(config.getPathToIgnore());
        }
        checkValidity();
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
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
     * @see io.apiman.manager.ui.client.local.events.IsFormValidEvent.HasIsFormValidHandlers#addIsFormValidHandler(io.apiman.manager.ui.client.local.events.IsFormValidEvent.Handler)
     */
    @Override
    public HandlerRegistration addIsFormValidHandler(IsFormValidEvent.Handler handler) {
        return addHandler(handler, IsFormValidEvent.getType());
    }

    /**
     * Determine whether the form is valid (the user has completed filling out the form).
     */
    protected void checkValidity() {
        Boolean validity = Boolean.TRUE;
        String path = pathToIgnore.getValue();
        if (path.trim().isEmpty()) {
            validity = Boolean.FALSE;
        }
        IsFormValidEvent.fire(this, validity);
    }

}
