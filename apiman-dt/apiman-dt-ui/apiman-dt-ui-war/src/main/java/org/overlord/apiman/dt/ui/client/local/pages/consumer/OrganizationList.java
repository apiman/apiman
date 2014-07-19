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
package org.overlord.apiman.dt.ui.client.local.pages.consumer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.events.RequestMembershipEvent;
import org.overlord.apiman.dt.ui.client.local.events.RequestMembershipEvent.Handler;
import org.overlord.apiman.dt.ui.client.local.events.RequestMembershipEvent.HasRequestMembershipHandlers;
import org.overlord.apiman.dt.ui.client.local.pages.ConsumerOrgPage;
import org.overlord.apiman.dt.ui.client.local.pages.OrgServicesPage;
import org.overlord.apiman.dt.ui.client.local.pages.common.NoEntitiesWidget;
import org.overlord.apiman.dt.ui.client.local.services.NavigationHelperService;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a list of organizations in the consumer UI.  This is shown to the user on
 * the "Browse Organizations" page - the page that lets the user search for 
 * organizations to join.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class OrganizationList extends FlowPanel implements HasValue<List<OrganizationBean>>, HasRequestMembershipHandlers  {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    
    @Inject
    protected TransitionAnchorFactory<ConsumerOrgPage> toOrgDetails;
    @Inject
    protected TransitionAnchorFactory<OrgServicesPage> toOrgServices;
    
    private List<OrganizationBean> organizations;
    private Set<String> memberOrgs = new HashSet<String>();

    /**
     * Constructor.
     */
    public OrganizationList() {
        getElement().setClassName("apiman-organizations"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<OrganizationBean>> handler) {
        return super.addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<OrganizationBean> getValue() {
        return organizations;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<OrganizationBean> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<OrganizationBean> value, boolean fireEvents) {
        organizations = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (organizations != null && !organizations.isEmpty()) {
            Widget count = createCount();
            add(count);
            for (OrganizationBean bean : organizations) {
                Widget row = createOrganizationRow(bean);
                add(row);
            }
        } else {
            add(createNoEntitiesWidget());
        }
    }

    /**
     * @return a widget to show when there are no entities.
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        return new NoEntitiesWidget(i18n.format(AppMessages.NO_ORGANIZATIONS_MESSAGE), false);
    }

    /**
     * @return a widget to display the number of matching items
     */
    private Widget createCount() {
        String cmsg = i18n.format(AppMessages.ORGANIZATION_COUNT, String.valueOf(organizations.size()));
        Label label = new Label(cmsg);
        label.getElement().setClassName("count"); //$NON-NLS-1$
        return label;
    }

    /**
     * Creates a single organization row.
     * @param bean
     */
    private Widget createOrganizationRow(OrganizationBean bean) {
        FlowPanel item = new FlowPanel();
        item.getElement().setClassName("item"); //$NON-NLS-1$
        
        createTitleRow(bean, item);
        createDescriptionRow(bean, item);
        createActionsRow(bean, item);
        if (getMemberOrgs().contains(bean.getId())) {
            createIsMemberBadge(bean, item);
        }
        
        return item;
    }

    /**
     * @param bean
     * @param item
     */
    private void createTitleRow(OrganizationBean bean, FlowPanel item) {
        FlowPanel title = new FlowPanel();
        item.add(title);
        title.getElement().setClassName("title"); //$NON-NLS-1$
        
        FontAwesomeIcon icon = new FontAwesomeIcon("shield"); //$NON-NLS-1$
        title.add(icon);
        icon.getElement().addClassName("icon"); //$NON-NLS-1$
        
        Anchor a = toOrgDetails.get(MultimapUtil.singleItemMap("org", bean.getId())); //$NON-NLS-1$
        title.add(a);
        a.setText(bean.getName());
    }

    /**
     * @param bean
     * @param item
     */
    private void createDescriptionRow(OrganizationBean bean, FlowPanel item) {
        Label description = new Label();
        item.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
        description.setTitle(bean.getDescription());
        String d = bean.getDescription();
        if (d != null && d.length() >= 70) {
            d = d.substring(0, 70) + "..."; //$NON-NLS-1$
        }
        description.setText(d);
    }

    /**
     * @param bean
     * @param item
     */
    private void createActionsRow(final OrganizationBean bean, FlowPanel item) {
        FlowPanel actions = new FlowPanel();
        item.add(actions);
        actions.getElement().setClassName("actions"); //$NON-NLS-1$
        
        Button button = new Button();
        actions.add(button);
        button.setText(i18n.format(AppMessages.REQUEST_MEMBERSHIP));
        button.getElement().setClassName("btn"); //$NON-NLS-1$
        button.getElement().addClassName("btn-default"); //$NON-NLS-1$
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RequestMembershipEvent.fire(OrganizationList.this, bean);
            }
        });
    }

    /**
     * @param bean
     * @param item
     */
    private void createIsMemberBadge(OrganizationBean bean, FlowPanel item) {
        Anchor a = toOrgServices.get(MultimapUtil.singleItemMap("org", bean.getId())); //$NON-NLS-1$
        item.add(a);
        a.getElement().setClassName("ismember"); //$NON-NLS-1$
        a.setTitle(i18n.format(AppMessages.ALREADY_A_MEMBER));
    }

    /**
     * @return the memberOrgs
     */
    public Set<String> getMemberOrgs() {
        return memberOrgs;
    }

    /**
     * @param memberOrgs the memberOrgs to set
     */
    public void setMemberOrgs(Set<String> memberOrgs) {
        this.memberOrgs = memberOrgs;
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.events.RequestMembershipEvent.HasRequestMembershipHandlers#addRequestMembershipHandler(org.overlord.apiman.dt.ui.client.local.events.RequestMembershipEvent.Handler)
     */
    @Override
    public HandlerRegistration addRequestMembershipHandler(Handler handler) {
        return addHandler(handler, RequestMembershipEvent.getType());
    }

}
