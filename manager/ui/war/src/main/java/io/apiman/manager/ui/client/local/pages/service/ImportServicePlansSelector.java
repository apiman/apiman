/*
 * Copyright 2015 JBoss Inc
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

import javax.enterprise.context.Dependent;

/**
 * Extends the service plans selector so it looks and behaves slightly different
 * and can be used in the Import Services wizard.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ImportServicePlansSelector extends ServicePlansSelector {

    /**
     * Constructor.
     */
    public ImportServicePlansSelector() {
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.service.ServicePlansSelector#createPlanWidget()
     */
    @Override
    protected ServicePlanWidget createPlanWidget() {
        ServicePlanWidget widget = super.createPlanWidget();
        widget.name.getElement().getParentElement().setClassName(""); //$NON-NLS-1$
        return widget;
    }

}
