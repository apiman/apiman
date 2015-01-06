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
package io.apiman.manager.ui.client.local.widgets;

import io.apiman.manager.ui.client.local.events.ConfirmationEvent;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent.Handler;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent.HasConfirmationHandlers;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple confirmation dialog re-usable in the UI.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/dialog-confirm.html#dialog")
@Dependent
public class ConfirmationDialog extends Composite implements HasConfirmationHandlers {
    
    @Inject
    RootPanel rootPanel;
    
    @Inject @DataField
    InlineLabel title;
    @Inject @DataField
    FlowPanel body;
    @Inject @DataField
    Button closeButton;
    @Inject @DataField
    Button noButton;
    @Inject @DataField
    Button yesButton;
    
    /**
     * Constructor.
     */
    public ConfirmationDialog() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ConfirmationEvent.fire(ConfirmationDialog.this, Boolean.FALSE);
                shutdown();
            }
        });
        noButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ConfirmationEvent.fire(ConfirmationDialog.this, Boolean.FALSE);
                shutdown();
            }
        });
        yesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ConfirmationEvent.fire(ConfirmationDialog.this, Boolean.TRUE);
                shutdown();
            }
        });
    }
    
    /**
     * @param title
     */
    public void setDialogTitle(String title) {
        this.title.setText(title);
    }
    
    /**
     * @param message
     */
    public void setDialogMessage(String message) {
        this.body.clear();
        this.body.add(new InlineLabel(message));
    }
    
    /**
     * @param message
     */
    public void setDialogMessage(Widget message) {
        this.body.clear();
        this.body.add(message);
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        rootPanel.add(this);
        showModal();
    }

    /**
     * Show the modal.
     */
    public final native void showModal() /*-{
        $wnd.jQuery('#confirmModal').modal({'keyboard': false, 'backdrop': 'static'});
    }-*/;

    /**
     * @see io.apiman.manager.ui.client.local.events.ConfirmationEvent.HasConfirmationHandlers#addConfirmationHandler(io.apiman.manager.ui.client.local.events.ConfirmationEvent.Handler)
     */
    @Override
    public HandlerRegistration addConfirmationHandler(Handler handler) {
        return addHandler(handler, ConfirmationEvent.getType());
    }

    /**
     * Shut down the dialog.  Do this in a timer to give the animation a chance to work.
     */
    protected void shutdown() {
        this.getElement().removeAttribute("id"); //$NON-NLS-1$
        Timer timer = new Timer() {
            @Override
            public void run() {
                rootPanel.remove(ConfirmationDialog.this);
            }
        };
        timer.schedule(2000);
    }

}
