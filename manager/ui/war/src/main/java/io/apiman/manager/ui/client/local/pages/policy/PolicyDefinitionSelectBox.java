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
package io.apiman.manager.ui.client.local.pages.policy;

import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.ClientMessages;
import io.apiman.manager.ui.client.local.pages.common.SelectBox;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Select box for picking a policy type.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class PolicyDefinitionSelectBox extends SelectBox<PolicyDefinitionBean> {
    
    @Inject
    private ClientMessages i18n;

    /**
     * Constructor.
     */
    public PolicyDefinitionSelectBox() {
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.common.SelectBox#optionName(java.lang.Object)
     */
    @Override
    protected String optionName(PolicyDefinitionBean option) {
        if (option == null) {
            return i18n.format(AppMessages.CHOOSE_POLICY_TYPE);
        } else {
            return option.getName();
        }
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.common.SelectBox#optionValue(java.lang.Object)
     */
    @Override
    protected String optionValue(PolicyDefinitionBean option) {
        if (option == null) {
            return "__null__"; //$NON-NLS-1$
        } else {
            return option.getId();
        }
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.common.SelectBox#optionDataContent(java.lang.Object)
     */
    @Override
    protected String optionDataContent(PolicyDefinitionBean option) {
        StringBuilder builder = new StringBuilder();
        builder.append("<i class='fa fa-inline fa-fw"); //$NON-NLS-1$
        if (option != null && option.getIcon() != null) {
            builder.append(" fa-" + option.getIcon()); //$NON-NLS-1$
        }
        builder.append("'></i> <span"); //$NON-NLS-1$
        if (option == null) {
            builder.append(" class='apiman-label-faded'"); //$NON-NLS-1$
        }
        builder.append(">"); //$NON-NLS-1$
        if (option == null) {
            builder.append(i18n.format(AppMessages.CHOOSE_POLICY_TYPE));
        } else {
            builder.append(option.getName());
        }
        builder.append("</span>"); //$NON-NLS-1$
        return builder.toString();
    }

}
