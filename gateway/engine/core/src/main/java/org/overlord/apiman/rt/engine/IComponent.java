/*
 * Copyright 2013 JBoss Inc
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

package org.overlord.apiman.rt.engine;

/**
 * All components provided to policies during their runtime must implement this interface.  A
 * component in APIMan is anything provided by the APIMan system/platform that can be used by
 * a policy implementation at runtime.  Examples of components include the Shared State Component
 * and the HTTP Client Component.
 * 
 * Components are provided for ease of use and consistency amongst policy implementations.  In
 * addition, the components provided to the policies should be async when appropriate and should
 * be superior in function or performance.
 * 
 * Normally this would be called a "service" but this name is not used to avoid confusion with
 * the APIMan concept of the same name.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IComponent {

}
