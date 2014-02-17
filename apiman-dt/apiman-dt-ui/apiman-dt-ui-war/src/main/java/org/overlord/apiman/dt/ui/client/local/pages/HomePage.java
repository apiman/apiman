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
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.overlord.apiman.dt.ui.client.local.services.ConfigurationService;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.user.client.ui.Label;

/**
 * The default page.  This is responsible for redirecting the user to
 * an appropriate starting page.
 *
 * @author eric.wittmann@redhat.com
 */
@Page(path="h", role=DefaultPage.class)
@Dependent
public class HomePage extends Label {
    
    @Inject
    Navigation nav;
    @Inject
    ConfigurationService config;

    /**
     * Constructor.
     */
    public HomePage() {
    }
    
    @PageShowing
    public void onPageShowing() {
        Multimap<String, String> params = HashMultimap.create();
        params.put("user", config.getCurrentConfig().getUser().getUsername()); //$NON-NLS-1$
        nav.goTo(UserOrgsPage.class, params);
    }

}
