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
package io.apiman.manager.ui.client.local.pages.common.activity;

import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.data.ContractData;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.AppRedirectPage;
import io.apiman.manager.ui.client.local.pages.OrgRedirectPage;
import io.apiman.manager.ui.client.local.pages.PlanRedirectPage;
import io.apiman.manager.ui.client.local.pages.ServiceRedirectPage;
import io.apiman.manager.ui.client.local.util.MultimapUtil;
import io.apiman.manager.ui.client.local.widgets.SpanLabel;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;

import com.google.gwt.user.client.ui.Anchor;


/**
 * Shows more information about a contract creation activity item.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class CreateContractDetailPanel extends AbstractDetailPanel {

    @Inject
    TransitionAnchorFactory<OrgRedirectPage> orgLinkFactory;
    @Inject
    TransitionAnchorFactory<AppRedirectPage> appLinkFactory;
    @Inject
    TransitionAnchorFactory<ServiceRedirectPage> serviceLinkFactory;
    @Inject
    TransitionAnchorFactory<PlanRedirectPage> planLinkFactory;

    /**
     * Constructor.
     */
    public CreateContractDetailPanel() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.common.activity.AbstractDetailPanel#render(io.apiman.manager.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void render(AuditEntryBean entry) {
        ContractData data = unmarshal(entry.getData(), ContractData.class);
        
        add(new SpanLabel(i18n.format(AppMessages.ACTIVITY_DATA_CONTRACT_1_CREATE)));
        add(new SpanLabel(" ")); //$NON-NLS-1$

        // application links
        Anchor appOrgAnchor = orgLinkFactory.get("org", data.getAppOrgId()); //$NON-NLS-1$
        appOrgAnchor.setText(data.getAppOrgId());
        add(appOrgAnchor);
        add(new SpanLabel(" / ")); //$NON-NLS-1$
        Anchor appAnchor = appLinkFactory.get(MultimapUtil.fromMultiple("org", data.getAppOrgId(), "app", data.getAppId())); //$NON-NLS-1$ //$NON-NLS-2$
        appAnchor.setText(data.getAppId());
        add(appAnchor);
        add(new SpanLabel(":")); //$NON-NLS-1$
        Anchor appVersionAnchor = appLinkFactory.get(MultimapUtil.fromMultiple("org", data.getAppOrgId(), "app", data.getAppId(), "version", data.getAppVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        appVersionAnchor.setText(data.getAppVersion());
        add(appVersionAnchor);

        add(new SpanLabel(" ")); //$NON-NLS-1$
        add(new SpanLabel(i18n.format(AppMessages.ACTIVITY_DATA_CONTRACT_2)));
        add(new SpanLabel(" ")); //$NON-NLS-1$

        // service links
        Anchor serviceOrgAnchor = orgLinkFactory.get("org", data.getServiceOrgId()); //$NON-NLS-1$
        serviceOrgAnchor.setText(data.getServiceOrgId());
        add(serviceOrgAnchor);
        add(new SpanLabel(" / ")); //$NON-NLS-1$
        Anchor serviceAnchor = serviceLinkFactory.get(MultimapUtil.fromMultiple("org", data.getServiceOrgId(), "service", data.getServiceId())); //$NON-NLS-1$ //$NON-NLS-2$
        serviceAnchor.setText(data.getServiceId());
        add(serviceAnchor);
        add(new SpanLabel(":")); //$NON-NLS-1$
        Anchor serviceVersionAnchor = serviceLinkFactory.get(MultimapUtil.fromMultiple("org", data.getServiceOrgId(), "service", data.getServiceId(), "version", data.getServiceVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        serviceVersionAnchor.setText(data.getServiceVersion());
        add(serviceVersionAnchor);

        add(new SpanLabel(" ")); //$NON-NLS-1$
        add(new SpanLabel(i18n.format(AppMessages.ACTIVITY_DATA_CONTRACT_3)));
        add(new SpanLabel(" ")); //$NON-NLS-1$

        Anchor planVersionAnchor = planLinkFactory.get(MultimapUtil.fromMultiple("org", data.getServiceOrgId(), "plan", data.getPlanId(), "version", data.getPlanVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        planVersionAnchor.setText(data.getPlanVersion());
        add(planVersionAnchor);

        add(new SpanLabel(" ")); //$NON-NLS-1$
        add(new SpanLabel(i18n.format(AppMessages.ACTIVITY_DATA_CONTRACT_4)));
        add(new SpanLabel(" ")); //$NON-NLS-1$

        Anchor planOrgAnchor = orgLinkFactory.get("org", data.getServiceOrgId()); //$NON-NLS-1$
        planOrgAnchor.setText(data.getServiceOrgId());
        add(planOrgAnchor);
        add(new SpanLabel(" / ")); //$NON-NLS-1$
        Anchor planAnchor = planLinkFactory.get(MultimapUtil.fromMultiple("org", data.getServiceOrgId(), "plan", data.getPlanId())); //$NON-NLS-1$ //$NON-NLS-2$
        planAnchor.setText(data.getPlanId());
        add(planAnchor);
        
        add(new SpanLabel(".")); //$NON-NLS-1$
    }

}
