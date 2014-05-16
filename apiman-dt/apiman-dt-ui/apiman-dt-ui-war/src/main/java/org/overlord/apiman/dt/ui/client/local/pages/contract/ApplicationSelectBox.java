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
package org.overlord.apiman.dt.ui.client.local.pages.contract;

import java.util.List;

import javax.enterprise.context.Dependent;

import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox;

/**
 * Select box for picking an application.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ApplicationSelectBox extends SelectBox<ApplicationSummaryBean> {
    
    /**
     * Constructor.
     */
    public ApplicationSelectBox() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#setOptions(java.util.List)
     */
    @Override
    public void setOptions(List<ApplicationSummaryBean> options) {
        super.setOptions(options);
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionName(java.lang.Object)
     */
    @Override
    protected String optionName(ApplicationSummaryBean option) {
        return option.getOrganizationName() + " / " + option.getName(); //$NON-NLS-1$
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionValue(java.lang.Object)
     */
    @Override
    protected String optionValue(ApplicationSummaryBean option) {
        return option.getOrganizationId() + "|" + option.getId(); //$NON-NLS-1$
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionDataContent(java.lang.Object)
     */
    @Override
    protected String optionDataContent(ApplicationSummaryBean option) {
        StringBuilder builder = new StringBuilder();
        builder.append(option.getOrganizationName())
          .append(" / ") //$NON-NLS-1$
          .append("<span class='emphasis'>") //$NON-NLS-1$
          .append(option.getName())
          .append("</span>"); //$NON-NLS-1$
        return builder.toString();
    }

}
