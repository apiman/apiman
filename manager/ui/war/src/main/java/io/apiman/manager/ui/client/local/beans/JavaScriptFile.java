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
package io.apiman.manager.ui.client.local.beans;

import io.apiman.manager.ui.client.local.widgets.DropZone;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A file dropped onto a drop target (e.g. {@link DropZone}).
 * 
 * @author eric.wittmann@redhat.com
 */
public class JavaScriptFile {

    private JavaScriptObject jsFile;

    /**
     * Constructor.
     * 
     * @param jsFile
     */
    public JavaScriptFile(JavaScriptObject jsFile) {
        this.jsFile = jsFile;
    }

    /**
     * Reads the file as text.
     * @param handler
     */
    public native void readAsText(IDataReadHandler handler) /*-{
        var reader = new FileReader();
        reader.onload = function(e) {
            var value = e.target.result;
            handler.@io.apiman.manager.ui.client.local.beans.JavaScriptFile.IDataReadHandler::onDataLoaded(Ljava/lang/String;)(value);
        }
        reader.readAsText(this.@io.apiman.manager.ui.client.local.beans.JavaScriptFile::jsFile);
    }-*/;

    /**
     * @return the file name
     */
    public String getName() {
        return name(jsFile);
    }
    
    /**
     * @return the file size
     */
    public int getSize() {
        return size(jsFile);
    }
    
    /**
     * @return the file type
     */
    public String getType() {
        return type(jsFile);
    }

    private native String name(JavaScriptObject file) /*-{
        return file.name;
    }-*/;
    
    private native int size(JavaScriptObject file) /*-{
        return file.size;
    }-*/;
    
    private native String type(JavaScriptObject file) /*-{
        return file.type;
    }-*/;
    
    /**
     * Called when the file contents are read.
     */
    public static interface IDataReadHandler {
        
        /**
         * @param data
         */
        public void onDataLoaded(String data);
        
    }

}
