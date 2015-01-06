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
package io.apiman.manager.ui.client.local.pages.app;

import io.apiman.manager.api.beans.summary.ApiEntryBean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * A dialog shown to the user when they want to copy the endpoint for a managed service
 * on the application's APIs tab.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/dialog-copyapi.html#dialog")
@Dependent
public class CopyApiEndpointDialog extends Composite {
    
    @Inject
    RootPanel rootPanel;

    @Inject @DataField
    TextArea asQueryParam;
    @Inject @DataField
    TextArea asRequestHeader;

    @Inject @DataField
    Button closeButton;
    @Inject @DataField
    Button doneButton;
    
    private ApiEntryBean apiEntry;

    /**
     * Constructor.
     */
    public CopyApiEndpointDialog() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                shutdown();
            }
        });
        doneButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                shutdown();
            }
        });
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        refresh();
        rootPanel.add(this);
        showModal();
    }

    /**
     * Refresh the UI with values from the current bean.
     */
    private void refresh() {
        // As Query Param
        String fullUrl = apiEntry.getHttpEndpoint();
        if (fullUrl == null) {
            fullUrl = ""; //$NON-NLS-1$
        } else if (fullUrl.contains("?")) { //$NON-NLS-1$
            fullUrl += "&apikey=" + apiEntry.getApiKey(); //$NON-NLS-1$
        } else {
            fullUrl += "?apikey=" + apiEntry.getApiKey(); //$NON-NLS-1$
        }
        asQueryParam.setValue(fullUrl);
        
        // As HTTP Request Header
        String requestParam = "X-API-Key: " + apiEntry.getApiKey(); //$NON-NLS-1$
        asRequestHeader.setValue(requestParam);
    }

    /**
     * Show the modal.
     */
    public final native void showModal() /*-{
        $wnd.jQuery('#apiCopyModal').modal({'keyboard': false, 'backdrop': 'static'});
    }-*/;

    /**
     * Shut down the dialog.  Do this in a timer to give the animation a chance to work.
     */
    protected void shutdown() {
        this.getElement().removeAttribute("id"); //$NON-NLS-1$
        Timer timer = new Timer() {
            @Override
            public void run() {
                rootPanel.remove(CopyApiEndpointDialog.this);
            }
        };
        timer.schedule(2000);
    }

    /**
     * @return the apiEntry
     */
    public ApiEntryBean getApiEntry() {
        return apiEntry;
    }

    /**
     * @param apiEntry the apiEntry to set
     */
    public void setApiEntry(ApiEntryBean apiEntry) {
        this.apiEntry = apiEntry;
    }

}
