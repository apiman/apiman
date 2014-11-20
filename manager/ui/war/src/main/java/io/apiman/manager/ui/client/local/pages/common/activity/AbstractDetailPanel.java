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
import io.apiman.manager.ui.client.local.services.BeanMarshallingService;

import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Base class for all activity item detail panels.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractDetailPanel extends FlowPanel {
    
    @Inject
    BeanMarshallingService marshaller;
    @Inject
    protected TranslationService i18n;

    /**
     * Constructor.
     */
    public AbstractDetailPanel() {
        getElement().setClassName("row"); //$NON-NLS-1$
        getElement().addClassName("boxed-row"); //$NON-NLS-1$
    }
    
    /**
     * @param entry
     */
    public abstract void render(AuditEntryBean entry);
    
    /**
     * Unmarshal the audit data into a bean.
     * @param value
     * @param type
     */
    protected <T> T unmarshal(String value, Class<T> type) {
        if (value == null) {
            return null;
        }
        return marshaller.unmarshal(value, type);
    }

}
