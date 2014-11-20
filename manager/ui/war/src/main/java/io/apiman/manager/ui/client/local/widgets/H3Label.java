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

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple H3 label.
 *
 * @author eric.wittmann@redhat.com
 */
public class H3Label extends Widget {
    
    /**
     * Constructor.
     */
    public H3Label() {
        setElement(Document.get().createHElement(3));
    }
    
    /**
     * Sets the H3's text.
     * @param text
     */
    public void setText(String text) {
        getElement().setInnerText(text);
    }

}
