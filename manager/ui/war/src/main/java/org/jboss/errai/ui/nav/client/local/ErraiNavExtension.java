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
package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.common.collect.Multimap;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Slight extension to Errai.  Must be in this package to gain visibility to code.
 *
 * @author eric.wittmann@redhat.com
 */
public class ErraiNavExtension {
    
    /**
     * Generates an HREF to a specific page in the app.
     * @param nav
     * @param toPage
     * @param state
     */
    public static <T extends IsWidget> String createHrefToPage(Navigation nav, Class<T> toPage, Multimap<String, String> state) {
        PageNode<T> toPageInstance = nav.getNavGraph().getPage(toPage);
        HistoryToken token = HistoryToken.of(toPageInstance.name(), state);
        String href = "#" + token.toString(); //$NON-NLS-1$
        return href;
    }

}
