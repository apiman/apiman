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
package org.overlord.apiman.dt.ui.client.local.pages;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.Templated;


/**
 * The "User" page, with the Organizations tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/user-orgs.html#page")
@Page(path="user-orgs")
@Dependent
public class UserOrgsPage extends AbstractUserPage {

    /**
     * Constructor.
     */
    public UserOrgsPage() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        return rval;
    }

}
