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
package org.overlord.apiman.dt.ui.client.local.util;

/**
 * Some helper methods for doing base64 related tasks.
 *
 * @author eric.wittmann@redhat.com
 */
public final class Base64Util {
    
    /**
     * Base64 encoding.
     * @param data
     */
    public static final native String b64encode(String data) /*-{
        return $wnd.btoa(data);
    }-*/;

    /**
     * Base64 decoding.
     * @param data
     */
    public static final native String b64decode(String data) /*-{
        return $wnd.atob(data);
    }-*/;

}
