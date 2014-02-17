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
package org.overlord.apiman.dt.ui.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents the global "page is loading" widget.  This should be made
 * visible/invisible depending on where in the Page Lifecycle we are.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PageLoadingWidget {
    
    private Widget plw;
    
    /**
     * Constructor.
     */
    public PageLoadingWidget() {
    }
    
    /**
     * Grabs the page loading DIV from the host page.
     */
    @PostConstruct
    void postConstruct() {
        plw = RootPanel.get("page-loading"); //$NON-NLS-1$
    }
    
    /**
     * Called to show the page loading widget.
     */
    public void show() {
        plw.getElement().getStyle().clearDisplay();
        plw.getElement().getStyle().clearVisibility();
    }
    
    /**
     * Called to hide the page loading widget.
     */
    public void hide() {
        plw.getElement().getStyle().setDisplay(Display.NONE);
        plw.getElement().getStyle().setVisibility(Visibility.HIDDEN);
    }

}
