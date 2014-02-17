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
package org.overlord.apiman.dt.ui.client.local.services;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.ErraiNavExtension;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.overlord.apiman.dt.ui.client.local.pages.AbstractPage;

import com.google.common.collect.Multimap;

/**
 * Provides some navigation functionality to the application.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class NavigationHelperService {
    
    @Inject
    Navigation nav;
    
    /**
     * Constructor.
     */
    public NavigationHelperService() {
    }
    
    /**
     * Creates an HREF to an app page.
     * @param toPage
     * @param state
     */
    public <T extends AbstractPage> String createHrefToPage(Class<T> toPage, Multimap<String, String> state) {
        return ErraiNavExtension.createHrefToPage(nav, toPage, state);
    }

}
