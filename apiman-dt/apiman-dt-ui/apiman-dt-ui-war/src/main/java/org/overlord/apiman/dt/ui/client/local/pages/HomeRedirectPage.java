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
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.overlord.apiman.dt.ui.client.local.services.ConfigurationService;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

/**
 * The default page.  This is responsible for redirecting the user to
 * an appropriate starting page.
 *
 * @author eric.wittmann@redhat.com
 */
@Page(path="h", role=DefaultPage.class)
@Dependent
public class HomeRedirectPage extends AbstractRedirectPage {
    
    @Inject
    ConfigurationService config;

    /**
     * Constructor.
     */
    public HomeRedirectPage() {
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractRedirectPage#doRedirect()
     */
    @Override
    protected void doRedirect() {
        String currentUser = config.getCurrentConfig().getUser().getUsername();
        nav.goTo(UserRedirectPage.class, MultimapUtil.singleItemMap("user", currentUser)); //$NON-NLS-1$
    }

}
