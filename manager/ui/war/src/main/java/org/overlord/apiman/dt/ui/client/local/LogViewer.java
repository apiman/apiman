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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.ui.client.local.pages.common.SeveritySelectBox;
import org.overlord.apiman.dt.ui.client.local.services.LoggerSeverity;
import org.overlord.apiman.dt.ui.client.local.widgets.SpanLabel;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The singleton log viewer widget.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/log-viewer.html#viewer")
@ApplicationScoped
public class LogViewer extends Composite {

    @Inject @DataField
    SeveritySelectBox severity;
    @Inject @DataField
    Button closeButton;
    @Inject @DataField
    FlowPanel messages;
    @Inject @DataField
    Button clearButton;

    /**
     * Constructor.
     */
    public LogViewer() {
    }

    /**
     * @return the severity
     */
    public SeveritySelectBox getSeverity() {
        return severity;
    }

    /**
     * @return the closeButton
     */
    public Button getCloseButton() {
        return closeButton;
    }

    /**
     * @return the messages
     */
    public FlowPanel getMessages() {
        return messages;
    }

    /**
     * @return the clearButton
     */
    public Button getClearButton() {
        return clearButton;
    }

    /**
     * Adds a log message to the viewer.
     * @param severity
     * @param message
     */
    public void addMessage(LoggerSeverity severity, String message) {
        Widget w = createViewerMessage(severity, message);
        getMessages().insert(w, 0);
        if (getMessages().getWidgetCount() > 200) {
            getMessages().remove(getMessages().getWidgetCount() - 1);
        }
    }

    /**
     * Creates a widget for the message.
     * @param severity
     * @param message
     */
    private Widget createViewerMessage(LoggerSeverity severity, String message) {
        FlowPanel div = new FlowPanel();
        String iconName = "info-circle"; //$NON-NLS-1$
        switch (severity) {
        case Debug:
            iconName = "bug"; //$NON-NLS-1$
            break;
        case Error:
            iconName = "frown-o"; //$NON-NLS-1$
            break;
        case Info:
            iconName = "info-circle"; //$NON-NLS-1$
            break;
        case Warning:
            iconName = "warning"; //$NON-NLS-1$
            break;
        }
        FontAwesomeIcon icon = new FontAwesomeIcon(iconName, true);
        div.add(icon);
        SpanLabel msgLabel = new SpanLabel(message);
        div.add(msgLabel);
        return div;
    }
    
}
