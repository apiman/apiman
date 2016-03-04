/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.manager.api.es.util;

/**
 * Mimics the ES class of the same name.  Originally we used ES code in various
 * places (before we switched to using Jest as the client).  Some of the ES code
 * remained (for various reasons) but sadly results in *all* of elasticsearch.jar
 * being dragged into the project.
 * 
 * As part of the approach to removing it, we are replacing the ES bits we used
 * with custom impls with the same basic names and methods to make the transition
 * away from ES bits less painful.
 * 
 * @author ewittman
 */
public final class XContentFactory {
    
    /**
     * Constructor.
     */
    private XContentFactory() {
    }

    public static XContentBuilder jsonBuilder() {
        return new XContentBuilder();
    }

}
