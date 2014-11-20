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

import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.beans.JavaScriptFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

/**
 * A simple div drop-zone. This allows users to drag and drop files form their
 * computer onto this widget.
 * 
 * @author eric.wittmann@redhat.com
 */
public class DropZone extends Label implements HasValue<List<JavaScriptFile>> {

    @Inject
    private TranslationService i18n;
    
    private int fileSizeLimit = 1024 * 1024;
    private Set<String> acceptableTypes = new HashSet<String>();
    private List<JavaScriptFile> value = new ArrayList<JavaScriptFile>();

    /**
     * Constructor.
     */
    public DropZone() {
    }

    @PostConstruct
    protected void postConstruct() {
        addDragEnterHandler(new DragEnterHandler() {
            @Override
            public void onDragEnter(DragEnterEvent event) {
                getElement().addClassName("dropping"); //$NON-NLS-1$
                event.preventDefault();
                event.stopPropagation();
            }
        });
        addDragLeaveHandler(new DragLeaveHandler() {
            @Override
            public void onDragLeave(DragLeaveEvent event) {
                getElement().removeClassName("dropping"); //$NON-NLS-1$
                event.preventDefault();
                event.stopPropagation();
            }
        });
        addDragOverHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                if (!getElement().getClassName().contains("dropping")) { //$NON-NLS-1$
                    getElement().addClassName("dropping"); //$NON-NLS-1$
                }
                event.preventDefault();
                event.stopPropagation();
            }
        });
        addDropHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                getElement().removeClassName("dropping"); //$NON-NLS-1$
                event.preventDefault();
                event.stopPropagation();
                doDrop(event);
            }
        });
    }

    /**
     * Called when the user drops a file.
     * 
     * @param event
     */
    protected void doDrop(DropEvent event) {
        value.clear();
        handleFiles(event.getDataTransfer());
        getElement().addClassName("dropped"); //$NON-NLS-1$
        String text = createDroppedText();
        getElement().setInnerText(text);
        fireValueChangeEvent();
    }

    /**
     * @return the text to display when files have been dropped
     */
    private String createDroppedText() {
        StringBuilder builder = new StringBuilder();
        builder.append(i18n.format(AppMessages.FILES_DROPPED));
        boolean first = true;
        for (JavaScriptFile javaScriptFile : value) {
            if (!first) {
                builder.append(", "); //$NON-NLS-1$
            }
            builder.append(javaScriptFile.getName());
            first = false;
        }
        return builder.toString();
    }

    /**
     * Called to handle the list of files that were dropped.
     * 
     * @param dt
     */
    public final native void handleFiles(DataTransfer dt) /*-{
        if (dt && dt.files) {
            var files = dt.files;
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                if (this.@io.apiman.manager.ui.client.local.widgets.DropZone::accept(Ljava/lang/String;ILjava/lang/String;)(file.name, file.size, file.type)) {
                    this.@io.apiman.manager.ui.client.local.widgets.DropZone::addFile(Lcom/google/gwt/core/client/JavaScriptObject;)(file);
                }
            }
        }
    }-*/;
    
    /**
     * Adds a file to the list of files.
     * @param jsFile
     */
    protected void addFile(JavaScriptObject jsFile) {
        value.add(new JavaScriptFile(jsFile));
    }

    /**
     * Returns true if the drop zone accepts the dropped file.
     * 
     * @param fileName
     * @param fileSize
     * @param fileType
     */
    protected boolean accept(String fileName, int fileSize, String fileType) {
        return true;
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<JavaScriptFile>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<JavaScriptFile> getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<JavaScriptFile> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object,
     *      boolean)
     */
    @Override
    public void setValue(List<JavaScriptFile> value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            fireValueChangeEvent();
        }
    }

    /**
     * Fires the value change event.
     */
    private void fireValueChangeEvent() {
        ValueChangeEvent.fire(this, value);
    }

}
