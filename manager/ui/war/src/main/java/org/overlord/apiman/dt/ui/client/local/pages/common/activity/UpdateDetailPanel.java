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
import org.overlord.apiman.dt.api.beans.audit.data.EntityFieldChange;
import org.overlord.apiman.dt.api.beans.audit.data.EntityUpdatedData;
import org.overlord.apiman.dt.ui.client.local.widgets.SpanLabel;

import com.google.gwt.user.client.ui.FlowPanel;


/**
 * Shows more information about an entity update activity item.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class UpdateDetailPanel extends AbstractDetailPanel {

    /**
     * Constructor.
     */
    public UpdateDetailPanel() {
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.activity.AbstractDetailPanel#render(org.overlord.apiman.dt.api.beans.audit.AuditEntryBean)
     * 
     * 
          <div><span class="emphasis capitalized">description</span></div>
          <div class="activity-value">This is the new description for this stupid service!</div>

     * 
     */
    @Override
    public void render(AuditEntryBean entry) {
        EntityUpdatedData data = unmarshal(entry.getData(), EntityUpdatedData.class);
        for (EntityFieldChange change : data.getChanges()) {
            FlowPanel div1 = new FlowPanel();
            SpanLabel field = new SpanLabel(change.getName());
            field.getElement().setClassName("capitalized"); //$NON-NLS-1$
            field.getElement().addClassName("emphasis"); //$NON-NLS-1$
            div1.add(field);
            
            FlowPanel div2 = new FlowPanel();
            div2.getElement().setClassName("activity-value"); //$NON-NLS-1$
            boolean shortened = false;
            String value = change.getAfter();
            if (value.length() > 512) {
                value = value.substring(0, 512) + "..."; //$NON-NLS-1$
                shortened = true;
            }
            SpanLabel valLabel = new SpanLabel(value);
            if (shortened) {
                valLabel.setTitle(change.getAfter());
            }
            div2.add(valLabel);
            
            add(div1);
            add(div2);
        }
    }

}
