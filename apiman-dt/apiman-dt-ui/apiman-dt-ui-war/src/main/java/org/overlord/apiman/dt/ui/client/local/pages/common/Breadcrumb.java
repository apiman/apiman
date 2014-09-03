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
package org.overlord.apiman.dt.ui.client.local.pages.common;

import org.overlord.commons.gwt.client.local.widgets.AnchorPanel;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.OrderedListPanel;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.user.client.ui.InlineLabel;

/**
 * Implements a breadcrumb, which is an ordered list of items
 * styled a particular way.  The markup looks like this:
 * 
 * <pre>
 *   &lt;ol class="breadcrumb" data-field="breadcrumb">
 *     &lt;li>&lt;a href="dash.html">&lt;i class="fa fa-home fa-fw">&lt;/i>&lt;span>Home&lt;/span>&lt;/a>&lt;/li>
 *     &lt;li>&lt;a href="consumer-orgs.html">&lt;i class="fa fa-search fa-fw">&lt;/i>&lt;span>Organizations&lt;/span>&lt;/a>&lt;/li>
 *     &lt;li class="active">&lt;i class="fa fa-shield fa-fw">&lt;/i>&lt;span>JBoss Overlord&lt;/span>&lt;/li>
 *   &lt;/ol>
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public class Breadcrumb extends OrderedListPanel {

    /**
     * Constructor.
     */
    public Breadcrumb() {
    }
    
    /**
     * Adds a item to the breadcrumb.
     * @param url
     * @param icon
     * @param label
     */
    public void addItem(String url, String icon, String label) {
        AnchorPanel ap = new AnchorPanel();
        ap.setHref(url);
        FontAwesomeIcon fai = new FontAwesomeIcon(icon, true);
        ap.add(fai);
        InlineLabel l = new InlineLabel(label);
        l.getElement().setClassName(""); //$NON-NLS-1$
        ap.add(l);
        this.add(ap);
    }
    
    /**
     * Adds an 'active' item to the bradcrumb.
     * @param icon
     * @param label
     */
    public void addActiveItem(String icon, String label) {
        SpanPanel sp = new SpanPanel();
        FontAwesomeIcon fai = new FontAwesomeIcon(icon, true);
        sp.add(fai);
        InlineLabel l = new InlineLabel(label);
        l.getElement().setClassName(""); //$NON-NLS-1$
        sp.add(l);
        this.add(sp);
    }

}
