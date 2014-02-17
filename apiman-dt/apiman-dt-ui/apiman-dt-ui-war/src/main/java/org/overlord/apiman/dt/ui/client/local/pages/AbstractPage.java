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
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.overlord.apiman.dt.ui.client.local.PageLoadingWidget;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for all pages, includes/handles the header and footer.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPage extends Composite {

    @Inject
    protected ClientMessageBus bus;
    @Inject
    protected PageLoadingWidget pageLoadingWidget;
    @Inject
    protected Navigation navigation;
    
    private int expectedDataPackets;
    private int dataPacketsReceived;

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
                doPageLoadingLifecycle();
            }
        });
    }

    /**
     * Execute the "page loading" lifecycle.  The lifecycle consists of the following
     * steps:
     * 
     * 1) begin data loading (pages can kick off async data fetching here)
     * 2) wait for all data to be loaded (pages must call dataLoaded() to indicate a piece of data was loaded successfully)
     * 3) render and display page
     */
    protected void doPageLoadingLifecycle() {
        onPageLoading();
        pageLoadingWidget.show();
        navigation.getContentPanel().asWidget().getElement().getStyle().setVisibility(Visibility.HIDDEN);
        navigation.getContentPanel().asWidget().getElement().getStyle().setDisplay(Display.NONE);
        dataPacketsReceived = 0;
        expectedDataPackets = loadPageData();
        if (expectedDataPackets == 0) {
            showPage();
        }
    }
    
    /**
     * Subclasses should implement this method.  This method represents an opportunity
     * for the page to load its data asynchronously.  If the page does not need to 
     * load any data, it should return 0;
     */
    protected int loadPageData() {
        return 0;
    }
    
    /**
     * Called when a single piece of data that the page requires is successfully
     * loaded.  Subclasses should call this method upon return of an async data
     * call.
     */
    protected void dataPacketLoaded() {
        dataPacketsReceived++;
        if (dataPacketsReceived == expectedDataPackets) {
            showPage();
        }
    }

    /**
     * Called when an error occurs trying to load page data.
     */
    protected void dataPacketError(Throwable t) {
        // TODO implement this!
        Window.alert("Error loading page data: " + t.getMessage()); //$NON-NLS-1$
    }

    /**
     * Called after all data has been loaded.
     */
    protected void showPage() {
        renderPage();
        pageLoadingWidget.hide();
        navigation.getContentPanel().asWidget().getElement().getStyle().clearVisibility();
        navigation.getContentPanel().asWidget().getElement().getStyle().clearDisplay();
        onPageLoaded();
    }

    /**
     * Subclasses can implement this to do any work they need done when the page
     * is about to be shown.
     */
    protected void onPageLoading() {
    }
    
    /**
     * Called once all page data has been loaded and the page is ready to be
     * rendered.
     */
    protected void renderPage() {
        
    }
    
    /**
     * Called once all data has been loaded and the page has been rendered.
     */
    protected void onPageLoaded() {
        
    }

}
