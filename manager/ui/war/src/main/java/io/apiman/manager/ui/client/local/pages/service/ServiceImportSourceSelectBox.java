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
package io.apiman.manager.ui.client.local.pages.service;

import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.beans.ServiceImportSourceType;
import io.apiman.manager.ui.client.local.pages.common.SelectBox;

import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;

/**
 * Select box allowing users to choose their service import source.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceImportSourceSelectBox extends SelectBox<ServiceImportSourceType> {
    
    @Inject
    private TranslationService i18n;

    /**
     * Constructor.
     */
    public ServiceImportSourceSelectBox() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.common.SelectBox#optionName(java.lang.Object)
     */
    @Override
    protected String optionName(ServiceImportSourceType option) {
        switch (option) {
        case Wadl:
            return i18n.format(AppMessages.WADL_FILE);
        default:
            return "Unknown"; //$NON-NLS-1$
        }
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.common.SelectBox#optionValue(java.lang.Object)
     */
    @Override
    protected String optionValue(ServiceImportSourceType option) {
        return option.name();
    }

}
