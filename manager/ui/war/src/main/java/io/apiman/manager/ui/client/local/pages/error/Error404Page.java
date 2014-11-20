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
package io.apiman.manager.ui.client.local.pages.error;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.Templated;


/**
 * Page is displayed when a 404 error is received during page load.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/error-404.html#error-page")
@Dependent
public class Error404Page extends AbstractErrorPage {
    
    /**
     * Constructor.
     */
    public Error404Page() {
    }
    
}
