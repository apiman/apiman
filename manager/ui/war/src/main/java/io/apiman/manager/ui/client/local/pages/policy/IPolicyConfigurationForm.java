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
package io.apiman.manager.ui.client.local.pages.policy;

import io.apiman.manager.ui.client.local.events.IsFormValidEvent.HasIsFormValidHandlers;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * All policy configuration forms must implement this interface.  The idea is
 * that different policy definition types will potentially have different 
 * JSON configuration forms.  This allows each policy type to have a UI form
 * that's specific to that type.
 * 
 * By default there will be a simple text-editor style form for those policies
 * which do not have a type-specific UI form.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IPolicyConfigurationForm extends IsWidget, HasValue<String>, HasIsFormValidHandlers {

}
