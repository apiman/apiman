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
package org.overlord.apiman.dt.ui.client.local.pages.policy.forms.widgets;

import javax.inject.Inject;

import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.ClientMessages;
import org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox;

/**
 * A select box for choosing a source of identity for authentication
 * policies.
 *
 * @author eric.wittmann@redhat.com
 */
public class IdentitySourceSelectBox extends SelectBox<String> {
    
    @Inject
    private ClientMessages i18n;

    /**
     * Constructor.
     */
    public IdentitySourceSelectBox() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionName(java.lang.Object)
     */
    @Override
    protected String optionName(String option) {
        if (option == null) {
            return i18n.format(AppMessages.CHOOSE_IDENTITY_SOURCE);
        } else {
            return option;
        }
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionValue(java.lang.Object)
     */
    @Override
    protected String optionValue(String option) {
        if (option == null) {
            return "__null__"; //$NON-NLS-1$
        } else {
            return option;
        }
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.SelectBox#optionDataContent(java.lang.Object)
     */
    @Override
    protected String optionDataContent(String option) {
        StringBuilder builder = new StringBuilder();
        builder.append("<span"); //$NON-NLS-1$
        if (option == null) {
            builder.append(" class='apiman-label-faded'"); //$NON-NLS-1$
        }
        builder.append(">"); //$NON-NLS-1$
        if (option == null) {
            builder.append(i18n.format(AppMessages.CHOOSE_IDENTITY_SOURCE));
        } else {
            builder.append(option);
        }
        builder.append("</span>"); //$NON-NLS-1$
        return builder.toString();
    }

}
