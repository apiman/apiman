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
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.ui.client.local.events.IsFormValidEvent;
import org.overlord.apiman.dt.ui.client.local.pages.policy.IPolicyConfigurationForm;
import org.overlord.apiman.dt.ui.client.local.pages.policy.forms.widgets.IdentitySourceSelectBox;
import org.overlord.apiman.dt.ui.client.local.services.BeanMarshallingService;
import org.overlord.apiman.engine.policies.config.BasicAuthenticationConfig;
import org.overlord.apiman.engine.policies.config.basicauth.StaticIdentity;
import org.overlord.apiman.engine.policies.config.basicauth.StaticIdentitySource;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A policy configuration form used for the IP whitelist and IP blacklist policies.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/policyconfig-basicauth.html#form")
@Dependent
public class BasicAuthPolicyConfigForm extends Composite implements IPolicyConfigurationForm {

    @Inject
    BeanMarshallingService marshaller;
    
    @Inject @DataField
    TextBox realm;
    @Inject @DataField
    TextBox authenticatedUserHeader;
    @Inject @DataField
    IdentitySourceSelectBox identitySourceSelector;
    
    // Static form fields
    //////////////////////////////////////
    @Inject @DataField
    ListBox staticIdentities;
    @Inject @DataField
    Button staticClear;
    @Inject @DataField
    Button staticRemove;
    @Inject @DataField
    TextBox staticUsername;
    @Inject @DataField
    TextBox staticPassword;
    @Inject @DataField
    Button staticAdd;
    
    private boolean valid = false;

    /**
     * Constructor.
     */
    public BasicAuthPolicyConfigForm() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        KeyUpHandler keyUpValidityHandler = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                checkValidity();
            }
        };
        realm.addKeyUpHandler(keyUpValidityHandler);
        authenticatedUserHeader.addKeyUpHandler(keyUpValidityHandler);
        staticIdentities.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                checkValidity();
            }
        });
        
        staticClear.setEnabled(false);
        staticRemove.setEnabled(false);
        staticAdd.setEnabled(false);
        staticUsername.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String val = staticUsername.getValue();
                staticAdd.setEnabled(!val.trim().isEmpty());
            }
        });
        staticIdentities.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                staticRemove.setEnabled(staticIdentities.getSelectedIndex() != -1);
            }
        });
        ArrayList<String> identitySourceTypes = new ArrayList<String>(4);
        identitySourceTypes.add(null);
        identitySourceTypes.add("Static"); //$NON-NLS-1$
        identitySourceTypes.add("JDBC"); //$NON-NLS-1$
        identitySourceTypes.add("LDAP"); //$NON-NLS-1$
        identitySourceSelector.setOptions(identitySourceTypes);
        identitySourceSelector.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                showSubForm(identitySourceSelector.getValue());
                checkValidity();
            }
        });
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    showSubForm(identitySourceSelector.getValue());
                }
            }
        });
    }

    /**
     * Called to show a particular sub-form (static, jdbc, ldap).
     * @param subForm
     */
    protected void showSubForm(String subForm) {
        String sfId;
        if ("Static".equals(subForm)) { //$NON-NLS-1$
            sfId = "static-form-fields"; //$NON-NLS-1$
        } else if ("JDBC".equals(subForm)) { //$NON-NLS-1$
            sfId = "jdbc-form-fields"; //$NON-NLS-1$
        } else if ("LDAP".equals(subForm)) { //$NON-NLS-1$
            sfId = "ldap-form-fields"; //$NON-NLS-1$
        } else {
            sfId = null;
        }
        _showSubForm(sfId);
    }
    
    /**
     * Native show/hide code for the sub forms.
     * @param formId
     */
    public static native void _showSubForm(String formId) /*-{
        $wnd.jQuery('.sub-form-fields').hide();
        if (formId) {
            $wnd.jQuery('#' + formId).show();
        }
    }-*/;

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public String getValue() {
        BasicAuthenticationConfig config = new BasicAuthenticationConfig();
        config.setStaticIdentity(new StaticIdentitySource());
        config.setRealm(realm.getValue());
        if (!authenticatedUserHeader.getValue().trim().isEmpty()) {
            config.setForwardIdentityHttpHeader(authenticatedUserHeader.getValue().trim());
        }

        for (int idx = 0; idx < staticIdentities.getItemCount(); idx++) {
            String val = staticIdentities.getValue(idx);
            int div = val.indexOf(':');
            String username = val.substring(0, div);
            String password = ""; //$NON-NLS-1$
            if (div < val.length() - 1) {
                password = val.substring(div + 1);
            }
            StaticIdentity identity = new StaticIdentity();
            identity.setUsername(username);
            identity.setPassword(password);
            identity.setIsHash(false);
            config.getStaticIdentity().getIdentities().add(identity);
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
        staticIdentities.clear();
        staticClear.setEnabled(false);
        staticRemove.setEnabled(false);
        staticAdd.setEnabled(false);
        staticUsername.setValue(""); //$NON-NLS-1$
        staticPassword.setValue(""); //$NON-NLS-1$
        if (value != null && !value.trim().isEmpty()) {
            BasicAuthenticationConfig config = marshaller.unmarshal(value, BasicAuthenticationConfig.class);
            realm.setValue(config.getRealm());
            authenticatedUserHeader.setValue(config.getForwardIdentityHttpHeader());
            StaticIdentitySource staticIdentity = config.getStaticIdentity();
            if (staticIdentity != null) {
                List<StaticIdentity> identities = staticIdentity.getIdentities();
                Set<String> sorted = new TreeSet<String>();
                for (StaticIdentity identity : identities) {
                    String val = identity.getUsername() + ":" + identity.getPassword(); //$NON-NLS-1$
                    sorted.add(val);
                }
                for (String val : sorted) {
                    staticIdentities.addItem(val);
                }
                this.identitySourceSelector.setValue("Static"); //$NON-NLS-1$
                this.showSubForm("Static"); //$NON-NLS-1$
                staticClear.setEnabled(true);
            }
            IsFormValidEvent.fire(this, Boolean.TRUE);
        } else {
            IsFormValidEvent.fire(this, Boolean.FALSE);
        }
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
     * Called when the clear button is clicked.
     * @param event
     */
    @EventHandler("staticClear")
    protected void onClear(ClickEvent event) {
        staticIdentities.clear();
        staticRemove.setEnabled(false);
        staticClear.setEnabled(false);
        checkValidity();
    }
    
    /**
     * Called when the remove button is clicked.
     * @param event
     */
    @EventHandler("staticRemove")
    protected void onRemove(ClickEvent event) {
        for (int idx = staticIdentities.getItemCount() - 1; idx >= 0; idx--) {
            if (staticIdentities.isItemSelected(idx)) {
                staticIdentities.removeItem(idx);
            }
        }
        staticRemove.setEnabled(false);
        staticClear.setEnabled(staticIdentities.getItemCount() > 0);
        checkValidity();
    }
    
    /**
     * Called when the add button is clicked.
     * @param event
     */
    @EventHandler("staticAdd")
    protected void onAdd(ClickEvent event) {
        String newUsername = staticUsername.getValue();
        String newPassword = staticPassword.getValue();
        String newValue = newUsername + ":" + newPassword; //$NON-NLS-1$
        boolean inserted = false;
        for (int idx = 0; idx < staticIdentities.getItemCount(); idx++) {
            String v = staticIdentities.getValue(idx);
            // Check for dupes
            if (v.startsWith(newUsername + ":")) { //$NON-NLS-1$
                inserted = true;
                staticIdentities.setSelectedIndex(idx);
                break;
            }
            // Order is important
            if (newUsername.compareTo(v) < 0) {
                staticIdentities.insertItem(newValue, idx);
                staticIdentities.setSelectedIndex(idx);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            staticIdentities.addItem(newValue);
            staticIdentities.setSelectedIndex(staticIdentities.getItemCount() - 1);
        }
        staticRemove.setEnabled(true);
        staticClear.setEnabled(true);
        staticAdd.setEnabled(false);
        staticUsername.setValue(""); //$NON-NLS-1$
        staticUsername.setFocus(true);
        staticPassword.setValue(""); //$NON-NLS-1$
        checkValidity();
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.events.IsFormValidEvent.HasIsFormValidHandlers#addIsFormValidHandler(org.overlord.apiman.dt.ui.client.local.events.IsFormValidEvent.Handler)
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
        String authRealm = realm.getValue();
        String identitySourceType = identitySourceSelector.getValue();
        if (authRealm.trim().isEmpty()) {
            validity = Boolean.FALSE;
        }
        if ("Static".equals(identitySourceType)) { //$NON-NLS-1$
            if (staticIdentities.getItemCount() == 0) {
                validity = Boolean.FALSE;
            }
        } else if ("JDBC".equals(identitySourceType)) { //$NON-NLS-1$
        } else if ("LDAP".equals(identitySourceType)) { //$NON-NLS-1$
        } else {
            validity = Boolean.FALSE;
        }
        IsFormValidEvent.fire(this, validity);
    }

}
