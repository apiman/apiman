/*
 * Copyright 2013 JBoss Inc
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

package org.overlord.apiman.dt.ui.client.local.pages.consumer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.ui.client.local.widgets.SimpleVersionSelectBox;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 * A way to display info about a service in the consumer UI.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/consumer-service.html#serviceCard")
@Dependent
public class ServiceCard extends Composite implements TakesValue<ServiceBean>, HasValueChangeHandlers<String> {

    @Inject @DataField
    private Anchor titleOrg;
    @Inject @DataField
    private InlineLabel titleService;
    @Inject @DataField
    private Label description;
    @Inject @DataField
    private SimpleVersionSelectBox versionSelector;
    
    
    private ServiceBean value;

    /**
     * Constructor.
     */
    public ServiceCard() {
    }
    
    @PostConstruct
    protected void postConstruct() {
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return versionSelector.addValueChangeHandler(handler);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public ServiceBean getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(ServiceBean value) {
        this.value = value;
        refresh();
    }

    /**
     * Refresh the UI with the new data.
     */
    private void refresh() {
        // TODO fix this - the text should be the org name, not the org id - need the OrganizationBean for this
        titleOrg.setText(value.getOrganizationId());
        titleService.setText(value.getName());
        description.setText(value.getDescription());
    }
    
    public void refreshVersions() {
        
    }

}
