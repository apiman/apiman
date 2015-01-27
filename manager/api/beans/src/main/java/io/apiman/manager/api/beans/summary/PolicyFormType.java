/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.beans.summary;

/**
 * The different types of policy forms supported by apiman.  The UI will handle
 * displaying a configuration form for the policy differently depending on the
 * type of policy form.  For example, if the type is Default, then the UI is 
 * expected to know how to create a form using only the policy definition ID.
 * For the built-in apiman policies, this should work well (e.g. IP Whitelisting,
 * Rate Limiting, etc).  However, if the policy definition came from a plugin,
 * then the plugin may have also provided a JsonSchema that defines the 
 * configuration format.  In this case, the UI should use a JsonSchema form
 * generator to show the form.
 * 
 * If the type of a policy def form is Default but the UI does not know how to
 * build a specific form for the policy, a default form (text area) will be 
 * used.
 *
 * @author eric.wittmann@redhat.com
 */
public enum PolicyFormType {
    
    Default, JsonSchema;

}
