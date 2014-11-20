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

import io.apiman.manager.ui.client.local.util.MultimapUtil;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;

/**
 * The default page.  This is responsible for redirecting the user to
 * an appropriate starting page.
 *
 * @author eric.wittmann@redhat.com
 */
@Page(path="user")
@Dependent
public class UserRedirectPage extends AbstractRedirectPage {
    
    @PageState
    protected String user;

    /**
     * Constructor.
     */
    public UserRedirectPage() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractRedirectPage#doRedirect()
     */
    @Override
    protected void doRedirect() {
        nav.goTo(UserOrgsPage.class, MultimapUtil.singleItemMap("user", user)); //$NON-NLS-1$
    }

}
