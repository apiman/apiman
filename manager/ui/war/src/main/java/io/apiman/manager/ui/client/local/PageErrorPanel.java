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
package io.apiman.manager.ui.client.local;

import io.apiman.manager.api.rest.contract.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.ui.client.local.pages.error.AbstractErrorPage;
import io.apiman.manager.ui.client.local.pages.error.Error403Page;
import io.apiman.manager.ui.client.local.pages.error.Error404Page;
import io.apiman.manager.ui.client.local.pages.error.Error409Page;
import io.apiman.manager.ui.client.local.pages.error.Error500Page;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Represents the global "page error" panel.  This should be made
 * invisible unless an error occurs during page load.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PageErrorPanel {
    
    private RootPanel errorPanel;
    
    @Inject
    Instance<Error403Page> error403Factory;
    @Inject
    Instance<Error404Page> error404Factory;
    @Inject
    Instance<Error409Page> error409Factory;
    @Inject
    Instance<Error500Page> error500Factory;
    
    /**
     * Constructor.
     */
    public PageErrorPanel() {
    }
    
    /**
     * Grabs the page loading DIV from the host page.
     */
    @PostConstruct
    void postConstruct() {
        errorPanel = RootPanel.get("errorPanel"); //$NON-NLS-1$
    }
    
    /**
     * Called to show the page loading widget.
     */
    public void show() {
        errorPanel.getElement().getStyle().clearDisplay();
        errorPanel.getElement().getStyle().clearVisibility();
    }
    
    /**
     * Called to hide the page loading widget.
     */
    public void hide() {
        errorPanel.getElement().getStyle().setDisplay(Display.NONE);
        errorPanel.getElement().getStyle().setVisibility(Visibility.HIDDEN);
    }
    
    /**
     * Clear all content from the error panel.
     */
    public void clear() {
        errorPanel.clear();
    }
    
    /**
     * Create and display an error message appropriate for the given 
     * error.
     * @param t
     */
    public void displayError(Throwable t) {
        if (t instanceof AbstractRestException) {
            AbstractRestException restError = (AbstractRestException) t;
            int httpCode = restError.getHttpCode();
            AbstractErrorPage errorPage = null;
            if (httpCode == 403) {
                errorPage = error403Factory.get();
            } else if (httpCode == 404) {
                errorPage = error404Factory.get();
            } else if (httpCode == 409) {
                errorPage = error409Factory.get();
            } else {
                errorPage = error500Factory.get();
            }
            errorPage.setValue(restError);
            errorPanel.add(errorPage);
        } else {
            AbstractErrorPage errorPage = null;
            errorPage = error500Factory.get();
            errorPage.setValue(new SystemErrorException(t));
            errorPanel.add(errorPage);
        }
    }

}
