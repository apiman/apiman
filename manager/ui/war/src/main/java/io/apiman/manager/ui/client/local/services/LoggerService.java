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
package io.apiman.manager.ui.client.local.services;

import io.apiman.manager.ui.client.local.LogViewer;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * A simple logging service.  All pages can use this to log messages to 
 * both the javascript console and to the built-in logger window which
 * can be activated via Ctrl-`
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class LoggerService {
    
    private LoggerSeverity severity = LoggerSeverity.Info;
    
    @Inject
    private RootPanel rootPanel;

    @Inject
    private LogViewer viewer;
    private boolean viewerIsAttached = false;
    
    /**
     * Constructor.
     */
    public LoggerService() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        viewer.getClearButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                viewer.getMessages().clear();
            }
        });
        viewer.getCloseButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toggleViewer();
            }
        });
        viewer.getSeverity().addValueChangeHandler(new ValueChangeHandler<LoggerSeverity>() {
            @Override
            public void onValueChange(ValueChangeEvent<LoggerSeverity> event) {
                severity = event.getValue();
            }
        });
    }
    
    /**
     * Called to toggle the log viewer UI widget.
     */
    public void toggleViewer() {
        if (viewerIsAttached) {
            rootPanel.remove(viewer);
            viewerIsAttached = false;
        } else {
            viewer.getSeverity().setValue(severity);
            rootPanel.add(viewer);
            viewerIsAttached = true;
        }
    }
    
    /**
     * Logs the given message.
     * @param message
     */
    public void debug(String messagePattern, Object... args) {
        log(LoggerSeverity.Debug, messagePattern, args);
    }

    /**
     * Logs the given message.
     * @param message
     */
    public void info(String messagePattern, Object... args) {
        log(LoggerSeverity.Info, messagePattern, args);
    }

    /**
     * Logs the given message.
     * @param message
     */
    public void warning(String messagePattern, Object... args) {
        log(LoggerSeverity.Warning, messagePattern, args);
    }

    /**
     * Logs the given message.
     * @param message
     */
    public void error(String messagePattern, Object... args) {
        log(LoggerSeverity.Error, messagePattern, args);
    }

    /**
     * Logs the message with the given severity.
     * @param severity
     * @param messagePattern
     * @param args
     */
    public void log(LoggerSeverity severity, String messagePattern, Object... args) {
        if (severity.getLevel() >= this.severity.getLevel()) {
            String message = null;
            if (args.length == 0) {
                message = messagePattern;
            } else {
                StringBuilder builder = new StringBuilder(messagePattern);
                int argId = 0;
                for (Object arg : args) {
                  String rcode = "{" + (argId++) + "}"; //$NON-NLS-1$ //$NON-NLS-2$
                  int startIdx = builder.indexOf(rcode);
                  int endIdx = startIdx + rcode.length();
                  builder.replace(startIdx, endIdx, String.valueOf(arg));
                }
                message = builder.toString();
            }
            Date now = new Date();
            String formattedNow = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM).format(now);
            logToViewer(formattedNow, severity, message);
            logToJavaScriptConsole(formattedNow, severity, message);
        }
    }

    /**
     * Logs the message to the log viewer widget.
     * @param time
     * @param severity
     * @param message
     */
    private void logToViewer(String time, LoggerSeverity severity, String message) {
        viewer.addMessage(severity, "[" + time + "] - " + message); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Logs a message to the javascript console.
     * @param severity
     * @param message
     */
    private static native void logToJavaScriptConsole(String time, LoggerSeverity severity, String message) /*-{
        try { console.log('[' + severity + '] - [' + time + '] - ' + message); } catch (e) {  }
    }-*/;

    /**
     * @return the severity
     */
    public LoggerSeverity getSeverity() {
        return severity;
    }

    /**
     * @param severity the severity to set
     */
    public void setSeverity(LoggerSeverity severity) {
        this.severity = severity;
    }

}
