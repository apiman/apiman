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
package org.overlord.apiman.dt.ui.client.local.pages;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.ui.client.local.AppMessages;


/**
 * The "Service" page, with the Plans tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/service-plans.html#page")
@Page(path="service-plans")
@Dependent
public class ServicePlansPage extends AbstractServicePage {
    
    /**
     * Constructor.
     */
    public ServicePlansPage() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        return rval;
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_SERVICE_PLANS, serviceBean.getName());
    }

}
