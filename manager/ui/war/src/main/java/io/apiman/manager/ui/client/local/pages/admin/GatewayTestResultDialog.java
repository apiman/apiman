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
package io.apiman.manager.ui.client.local.pages.admin;

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
 * A dialog shown to the user when testing a gateway fails.  This dialog
 * shows the full details of the reason for the failure.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/dialog-testresult.html#dialog")
@Dependent
public class GatewayTestResultDialog extends Composite {
    
    @Inject
    RootPanel rootPanel;

    @Inject @DataField
    TextArea details;

    @Inject @DataField
    Button closeButton;
    @Inject @DataField
    Button okButton;
    
    private String resultDetails;

    /**
     * Constructor.
     */
    public GatewayTestResultDialog() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                shutdown();
            }
        });
        okButton.addClickHandler(new ClickHandler() {
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
        details.setValue(resultDetails);
    }

    /**
     * Show the modal.
     */
    public final native void showModal() /*-{
        $wnd.jQuery('#apiTestResultModal').modal({'keyboard': false, 'backdrop': 'static'});
    }-*/;

    /**
     * Shut down the dialog.  Do this in a timer to give the animation a chance to work.
     */
    protected void shutdown() {
        this.getElement().removeAttribute("id"); //$NON-NLS-1$
        Timer timer = new Timer() {
            @Override
            public void run() {
                rootPanel.remove(GatewayTestResultDialog.this);
            }
        };
        timer.schedule(2000);
    }

    /**
     * @return the resultDetails
     */
    public String getResultDetails() {
        return resultDetails;
    }

    /**
     * @param resultDetails the resultDetails to set
     */
    public void setResultDetails(String resultDetails) {
        this.resultDetails = resultDetails;
    }

}
