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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HasValue;

/**
 * A file upload form control that provides access to the 
 * list of chosen files so they can be locally processed.
 *
 * @author eric.wittmann@redhat.com
 */
public class LocalFileChooser extends FileUpload implements HasValue<List<JavaScriptFile>> {
    
    private List<JavaScriptFile> value = new ArrayList<JavaScriptFile>();

    /**
     * Constructor.
     */
    public LocalFileChooser() {
        addDomHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onFilesChosen();
            }
        }, ChangeEvent.getType());
    }

    /**
     * Called when the user changes the files selected.
     */
    protected void onFilesChosen() {
        value.clear();
        handleFiles(getElement());
        setValue(value, true);
    }

    /**
     * Called to handle the list of files that were dropped.
     * @param element 
     */
    public final native void handleFiles(Element element) /*-{
        if (element.files) {
            var files = element.files;
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                this.@io.apiman.manager.ui.client.local.widgets.LocalFileChooser::addFile(Lcom/google/gwt/core/client/JavaScriptObject;)(file);
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
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<JavaScriptFile> value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }
    
}
