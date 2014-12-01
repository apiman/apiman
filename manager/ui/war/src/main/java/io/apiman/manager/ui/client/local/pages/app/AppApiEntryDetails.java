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
package io.apiman.manager.ui.client.local.pages.app;

import io.apiman.manager.api.beans.summary.ApiEntryBean;
import io.apiman.manager.ui.client.local.AppMessages;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Used to display the additional details of a single entry in an application's
 * api registry.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/app-apis.html#apiDetails")
public class AppApiEntryDetails extends Composite implements TakesValue<ApiEntryBean> {

    @Inject
    protected TranslationService i18n;

    @Inject @DataField
    private TextBox apiKey;
    @Inject @DataField
    private TextBox httpEndpoint;
    @Inject @DataField
    private Button copyButton;
    
    private ApiEntryBean value;
    
    /**
     * Constructor.
     */
    public AppApiEntryDetails() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        copyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String fullUrl = value.getHttpEndpoint();
                if (fullUrl.contains("?")) { //$NON-NLS-1$
                    fullUrl += "&apikey=" + value.getApiKey(); //$NON-NLS-1$
                } else {
                    fullUrl += "?apikey=" + value.getApiKey(); //$NON-NLS-1$
                }
                Window.prompt(i18n.format(AppMessages.APIS_REGISTRY_PLEASE_COPY), fullUrl);
            }
        });
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(ApiEntryBean value) {
        this.value = value;
        
        apiKey.setValue(value.getApiKey());
        httpEndpoint.setValue(value.getHttpEndpoint());
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#getValue()
     */
    @Override
    public ApiEntryBean getValue() {
        return value;
    }
    
}
