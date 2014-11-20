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
package io.apiman.manager.ui.client.local.pages;

import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.PageShowing;

import com.google.gwt.user.client.ui.Label;

/**
 * Base class for all pages that simply redirect somewhere else.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractRedirectPage extends Label {

    @Inject
    Navigation nav;

    /**
     * Constructor.
     */
    public AbstractRedirectPage() {
    }
    
    /**
     * Do the redirect when the page is 'showing'.
     */
    @PageShowing
    public void onPageShowing() {
        doRedirect();
    }

    /**
     * Called to actually do the redirect.
     */
    protected abstract void doRedirect();

}
