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
package io.apiman.manager.ui.client.local.pages;

import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.app.AppApiRegistryTable;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Anchor;


/**
 * The "Application" page, with the APIs tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/app-apis.html#page")
@Page(path="app-apis")
@Dependent
public class AppApisPage extends AbstractAppPage {

    private ApiRegistryBean apiRegistry;

    @Inject @DataField
    Anchor downloadAsJson;
    @Inject @DataField
    Anchor downloadAsXml;

    @Inject @DataField
    AppApiRegistryTable apis;

    /**
     * Constructor.
     */
    public AppApisPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractAppPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        // Return an extra 1 because we'll call getApiRegistry() later
        return rval + 1;
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractAppPage#onAppVersionLoaded()
     */
    @Override
    protected void onAppVersionLoaded() {
        String orgId = org;
        String appId = app;
        String appVersion = versionBean.getVersion();
        rest.getApiRegistry(orgId, appId, appVersion, new IRestInvokerCallback<ApiRegistryBean>() {
            @Override
            public void onSuccess(ApiRegistryBean response) {
                apiRegistry = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractUserPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        apis.setValue(apiRegistry);
        downloadAsJson.setHref(buildApiRegistryDownloadUrl("json")); //$NON-NLS-1$
        downloadAsXml.setHref(buildApiRegistryDownloadUrl("xml")); //$NON-NLS-1$
    }

    /**
     * Builds a URL that the user can click on to download the API registry
     * in the given format.
     * @param type
     */
    private String buildApiRegistryDownloadUrl(String type) {
        StringBuilder proxyUrl = new StringBuilder();
        proxyUrl.append(GWT.getHostPageBaseURL());
        if (!proxyUrl.toString().endsWith("/")) { //$NON-NLS-1$
            proxyUrl.append('/');
        }
        proxyUrl.append("proxies/apiman/organizations/"); //$NON-NLS-1$
        proxyUrl.append(org);
        proxyUrl.append("/applications/"); //$NON-NLS-1$
        proxyUrl.append(app);
        proxyUrl.append("/versions/"); //$NON-NLS-1$
        proxyUrl.append(versionBean.getVersion());
        proxyUrl.append("/apiregistry/"); //$NON-NLS-1$
        proxyUrl.append(type);
        
        return proxyUrl.toString();
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_APP_APIS, applicationBean.getName());
    }

}
