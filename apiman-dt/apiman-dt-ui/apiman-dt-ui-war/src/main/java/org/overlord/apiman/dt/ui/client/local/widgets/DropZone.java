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
package org.overlord.apiman.dt.ui.client.local.widgets;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.overlord.apiman.dt.ui.client.local.events.FilesDroppedEvent;
import org.overlord.apiman.dt.ui.client.local.events.FilesDroppedEvent.Handler;
import org.overlord.apiman.dt.ui.client.local.events.FilesDroppedEvent.HasFileDropHandlers;

import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;

/**
 * A simple div drop-zone.  This allows users to drag and drop files form their
 * computer onto this widget.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class DropZone extends Label implements HasFileDropHandlers {
    
    private int fileSizeLimit = 1024 * 1024;
    private Set<String> acceptableTypes = new HashSet<String>();
    
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
     * @param event
     */
    protected void doDrop(DropEvent event) {
        handleFiles(event.getDataTransfer());
    }


    public static final native void handleFiles(DataTransfer dt) /*-{
        if (dt && dt.files) {
            var files = dt.files;
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                console.log(file.name + ":" + file.size + ":" + file.type);
            }
        }
    }-*/;

    /**
     * @see org.overlord.apiman.dt.ui.client.local.events.FilesDroppedEvent.HasFileDropHandlers#addFileDropHandler(org.overlord.apiman.dt.ui.client.local.events.FilesDroppedEvent.Handler)
     */
    @Override
    public HandlerRegistration addFileDropHandler(Handler handler) {
        return addHandler(handler, FilesDroppedEvent.getType());
    }

}
