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

import io.apiman.manager.ui.client.local.beans.JavaScriptFile;
import io.apiman.manager.ui.client.local.beans.JavaScriptFile.IDataReadHandler;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

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
import com.google.gwt.user.client.ui.TextArea;

/**
 * A text area that also supports drag and drop of a file onto it.  When this
 * happens the contents of the TextArea will be replaced by the contents found
 * in the file.
 *
 * @author eric.wittmann@redhat.com
 */
public class DroppableTextArea extends TextArea {

    private int fileSizeLimit = 1024 * 1024;
    private Set<String> acceptableTypes = new HashSet<String>();
    private JavaScriptFile droppedFile;
    
    /**
     * Constructor.
     */
    public DroppableTextArea() {
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
        setValue(""); //$NON-NLS-1$
        handleFiles(event.getDataTransfer());
        if (droppedFile != null) {
            droppedFile.readAsText(new IDataReadHandler() {
                @Override
                public void onDataLoaded(String data) {
                    setValue(data);
                    ValueChangeEvent.fire(DroppableTextArea.this, getValue());
                }
            });
        }
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
                if (this.@io.apiman.manager.ui.client.local.widgets.DroppableTextArea::accept(Ljava/lang/String;ILjava/lang/String;)(file.name, file.size, file.type)) {
                    this.@io.apiman.manager.ui.client.local.widgets.DroppableTextArea::addFile(Lcom/google/gwt/core/client/JavaScriptObject;)(file);
                }
            }
        }
    }-*/;
    
    /**
     * Adds a file to the list of files.
     * @param jsFile
     */
    protected void addFile(JavaScriptObject jsFile) {
        droppedFile = new JavaScriptFile(jsFile);
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

}
