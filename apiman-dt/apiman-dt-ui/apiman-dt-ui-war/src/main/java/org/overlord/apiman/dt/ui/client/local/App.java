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
package org.overlord.apiman.dt.ui.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.overlord.apiman.dt.ui.client.local.pages.common.PageHeader;
import org.overlord.apiman.dt.ui.client.local.services.LoggerService;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The main entry point into the UI app.
 *
 * @author eric.wittmann@redhat.com
 */
@EntryPoint
@Bundle("messages.json")
public class App {

	@Inject
	private RootPanel rootPanel;
	@Inject
	private Navigation navigation;
	@Inject
	private LoggerService logger;
	@Inject
	private PageHeader pageHeader;

	@PostConstruct
	public void buildUI() {
        rootPanel.add(navigation.getContentPanel());
        rootPanel.addDomHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() == '`' && event.isControlKeyDown()) {
                    logger.toggleViewer();
                }
            }
        }, KeyPressEvent.getType());
        // Add the page header singleton.  It's shared by all pages, obviously.
        RootPanel.get("apiman-header").add(pageHeader); //$NON-NLS-1$
	}

}
