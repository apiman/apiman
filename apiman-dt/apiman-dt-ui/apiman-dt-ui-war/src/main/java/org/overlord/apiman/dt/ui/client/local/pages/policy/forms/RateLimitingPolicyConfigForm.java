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
package org.overlord.apiman.dt.ui.client.local.pages.policy.forms;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.ui.client.local.events.IsFormValidEvent;
import org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox;
import org.overlord.apiman.dt.ui.client.local.pages.policy.IPolicyConfigurationForm;
import org.overlord.apiman.dt.ui.client.local.services.BeanMarshallingService;
import org.overlord.apiman.engine.policies.config.RateLimitingConfig;
import org.overlord.apiman.engine.policies.config.rates.RateLimitingGranularity;
import org.overlord.apiman.engine.policies.config.rates.RateLimitingPeriod;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A policy configuration form used for the IP whitelist and IP blacklist policies.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/policyconfig-rate-limiting.html#form")
@Dependent
public class RateLimitingPolicyConfigForm extends Composite implements IPolicyConfigurationForm {

    @Inject
    BeanMarshallingService marshaller;
    
    @Inject @DataField
    TextBox limit;
    @Inject @DataField
    GranularitySelectBox granularity;
    @Inject @DataField
    PeriodSelectBox period;
    
    @Inject @DataField
    TextBox userHeader;

    /**
     * Constructor.
     */
    public RateLimitingPolicyConfigForm() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        List<RateLimitingGranularity> granularityOptions = new ArrayList<RateLimitingGranularity>();
        // TODO limit the list based on the context - are we configuring the policy for a plan, service, or application?
        granularityOptions.add(RateLimitingGranularity.Application);
        granularityOptions.add(RateLimitingGranularity.User);
        granularityOptions.add(RateLimitingGranularity.Service);
        granularity.setOptions(granularityOptions);
        
        List<RateLimitingPeriod> periodOptions = new ArrayList<RateLimitingPeriod>();
        periodOptions.add(RateLimitingPeriod.Second);
        periodOptions.add(RateLimitingPeriod.Minute);
        periodOptions.add(RateLimitingPeriod.Hour);
        periodOptions.add(RateLimitingPeriod.Day);
        periodOptions.add(RateLimitingPeriod.Month);
        periodOptions.add(RateLimitingPeriod.Year);
        period.setOptions(periodOptions);
        
        KeyUpHandler keyUpValidityHandler = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                checkValidity();
            }
        };
        limit.addKeyUpHandler(keyUpValidityHandler);
        userHeader.addKeyUpHandler(keyUpValidityHandler);
        granularity.addValueChangeHandler(new ValueChangeHandler<RateLimitingGranularity>() {
            @Override
            public void onValueChange(ValueChangeEvent<RateLimitingGranularity> event) {
                checkValidity();
                if (event.getValue() == RateLimitingGranularity.User) {
                    showRow("userRow"); //$NON-NLS-1$
                } else {
                    hideRow("userRow"); //$NON-NLS-1$
                }
            }
        });
        period.addValueChangeHandler(new ValueChangeHandler<RateLimitingPeriod>() {
            @Override
            public void onValueChange(ValueChangeEvent<RateLimitingPeriod> event) {
                checkValidity();
            }
        });
        
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    if (granularity.getValue() == RateLimitingGranularity.User) {
                        showRow("userRow"); //$NON-NLS-1$
                    } else {
                        hideRow("userRow"); //$NON-NLS-1$
                    }
                }
            }
        });
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        RateLimitingConfig config = new RateLimitingConfig();
        String limitStr = limit.getValue();
        try { config.setLimit(new Integer(limitStr)); } catch (Throwable t) {}
        config.setGranularity(granularity.getValue());
        config.setPeriod(period.getValue());
        String header = userHeader.getValue();
        if (header != null && !header.trim().isEmpty()) {
            config.setUserHeader(header);
        }
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
        limit.setValue(""); //$NON-NLS-1$
        userHeader.setValue(""); //$NON-NLS-1$
        // TODO set the granularity based on the form context - are we adding a policy for a service, app, or plan?
        granularity.setValue(RateLimitingGranularity.Application);
        period.setValue(RateLimitingPeriod.Month);
        hideRow("userRow"); //$NON-NLS-1$
        if (value != null && !value.trim().isEmpty()) {
            RateLimitingConfig config = marshaller.unmarshal(value, RateLimitingConfig.class);
            
            limit.setValue(String.valueOf(config.getLimit()));
            granularity.setValue(config.getGranularity());
            period.setValue(config.getPeriod());
            if (config.getGranularity() == RateLimitingGranularity.User && config.getUserHeader() != null) {
                userHeader.setValue(config.getUserHeader());
                showRow("userRow"); //$NON-NLS-1$
            }
        }
        IsFormValidEvent.fire(this, Boolean.TRUE);
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * Determine whether the form is valid (the user has completed filling out the form).
     */
    protected void checkValidity() {
        Boolean validity = Boolean.TRUE;
        try {
            Integer.parseInt(limit.getValue());
        } catch (Exception e) {
            validity = Boolean.FALSE;
        }
        if (granularity.getValue() == RateLimitingGranularity.User) {
            String val = userHeader.getValue();
            if (val == null || val.trim().isEmpty()) {
                validity = Boolean.FALSE;
            }
        }
        IsFormValidEvent.fire(this, validity);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Hides a row by ID.
     * @param rowId
     */
    private native void hideRow(String rowId) /*-{
      $wnd.jQuery('#' + rowId).hide();
    }-*/;

    /**
     * Shows a row by ID.
     * @param rowId
     */
    private native void showRow(String rowId) /*-{
      $wnd.jQuery('#' + rowId).show();
    }-*/;

    /**
     * @see org.overlord.apiman.dt.ui.client.local.events.IsFormValidEvent.HasIsFormValidHandlers#addIsFormValidHandler(org.overlord.apiman.dt.ui.client.local.events.IsFormValidEvent.Handler)
     */
    @Override
    public HandlerRegistration addIsFormValidHandler(IsFormValidEvent.Handler handler) {
        return addHandler(handler, IsFormValidEvent.getType());
    }
    
    /**
     * Select box for the granularity.
     */
    public static final class GranularitySelectBox extends SelectBox<RateLimitingGranularity> {
        
        /**
         * Constructor.
         */
        public GranularitySelectBox() {
        }

        /**
         * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionName(java.lang.Object)
         */
        @Override
        protected String optionName(RateLimitingGranularity option) {
            return option.name();
        }
        
        /**
         * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionValue(java.lang.Object)
         */
        @Override
        protected String optionValue(RateLimitingGranularity option) {
            return option.name();
        }
        
    }
    
    /**
     * Select box for the period.
     */
    public static final class PeriodSelectBox extends SelectBox<RateLimitingPeriod> {
        
        /**
         * Constructor.
         */
        public PeriodSelectBox() {
        }

        /**
         * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionName(java.lang.Object)
         */
        @Override
        protected String optionName(RateLimitingPeriod option) {
            return option.name();
        }
        
        /**
         * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionValue(java.lang.Object)
         */
        @Override
        protected String optionValue(RateLimitingPeriod option) {
            return option.name();
        }
        
    }

}
