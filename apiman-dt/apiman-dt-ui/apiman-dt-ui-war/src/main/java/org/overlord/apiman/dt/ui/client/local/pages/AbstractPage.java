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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ui.nav.client.local.PageShowing;

import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for all pages, includes/handles the header and footer.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPage extends Composite {

    @Inject
    protected ClientMessageBus bus;

    /**
     * Constructor.
     */
    public AbstractPage() {
    }

    /**
     * Called after the page is constructed.
     */
    @PostConstruct
    private final void _onPostConstruct() {
    }

    /**
     * Called when a page is about to be shown (either when the app is first loaded or
     * when navigating TO this page from another).
     */
    @PageShowing
    private final void _onPageShowing() {
        // Do initial page loading work, but do it as a post-init task
        // of the errai bus so that all RPC endpoints are ready.  This
        // is only necessary on initial app load, but it doesn't hurt
        // to always do it.
        CDI.addPostInitTask(new Runnable() {
            @Override
            public void run() {
                onPageShowing();
            }
        });
    }

    /**
     * Subclasses can implement this to do any work they need done when the page
     * is about to be shown.
     */
    protected void onPageShowing() {
    }

}
