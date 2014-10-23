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
package org.overlord.apiman.dt.ui.client.local.pages.common.activity;

import javax.enterprise.context.Dependent;

import org.overlord.apiman.dt.api.beans.audit.AuditEntryBean;
import org.overlord.apiman.dt.api.beans.audit.data.PolicyData;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.widgets.SpanLabel;


/**
 * Shows more information about a policy add activity item.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class RemovePolicyDetailPanel extends AbstractDetailPanel {

    /**
     * Constructor.
     */
    public RemovePolicyDetailPanel() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.activity.AbstractDetailPanel#render(org.overlord.apiman.dt.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void render(AuditEntryBean entry) {
        PolicyData data = unmarshal(entry.getData(), PolicyData.class);

        add(new SpanLabel(i18n.format(AppMessages.ACTIVITY_DATA_POLICY_REMOVED)));
        add(new SpanLabel(" ")); //$NON-NLS-1$
        SpanLabel name = new SpanLabel(data.getPolicyDefId());
        name.getElement().setClassName("emphasis"); //$NON-NLS-1$
        add(name);
    }

}
