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

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple label using a pre tag.
 *
 * @author eric.wittmann@redhat.com
 */
public class PreLabel extends Widget implements HasText {
    
    /**
     * Constructor.
     */
    public PreLabel() {
        setElement(Document.get().createPreElement());
    }
    
    /**
     * Sets the pre tag's text.
     * @param text
     */
    public void setText(String text) {
        getElement().setInnerText(text);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#getText()
     */
    @Override
    public String getText() {
        return getElement().getInnerText();
    }

}
