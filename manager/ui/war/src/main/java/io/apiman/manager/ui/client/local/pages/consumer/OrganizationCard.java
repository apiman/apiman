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

package io.apiman.manager.ui.client.local.pages.consumer;

import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.ui.client.local.events.RequestMembershipEvent;
import io.apiman.manager.ui.client.local.events.RequestMembershipEvent.Handler;
import io.apiman.manager.ui.client.local.events.RequestMembershipEvent.HasRequestMembershipHandlers;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 * Models 
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/consumer-org.html#organizationCard")
@Dependent
public class OrganizationCard extends Composite implements HasValue<OrganizationBean>, HasRequestMembershipHandlers {

    @Inject @DataField
    private InlineLabel title;
    @Inject @DataField
    private Label description;
    @Inject @DataField
    private Button requestMembership;
    @Inject @DataField
    private Anchor isMemberLink;
    
    private OrganizationBean value;

    /**
     * Constructor.
     */
    public OrganizationCard() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        requestMembership.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO implement Request Membership
            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<OrganizationBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see io.apiman.manager.ui.client.local.events.RequestMembershipEvent.HasRequestMembershipHandlers#addRequestMembershipHandler(io.apiman.manager.ui.client.local.events.RequestMembershipEvent.Handler)
     */
    @Override
    public HandlerRegistration addRequestMembershipHandler(Handler handler) {
        return addHandler(handler, RequestMembershipEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public OrganizationBean getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(OrganizationBean value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(OrganizationBean value, boolean fireEvents) {
        this.value = value;
        refresh();
    }

    /**
     * Refresh the UI with the new data.
     */
    private void refresh() {
        title.setText(value.getName());
        description.setText(value.getDescription());
        
        // TODO implement the "is member" functionality - need the list of orgs this user is a member of to check this boolean
        isMemberLink.setVisible(false);
    }

}
