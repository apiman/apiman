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
package org.overlord.apiman.dt.api.beans;

/**
 * Some simple bean utils.
 *
 * @author eric.wittmann@redhat.com
 */
public class BeanUtils {
    
    /**
     * Creates a bean id from the given bean name.  This essentially removes any
     * non "word" characters from the name.
     * @param name
     */
    public static final String idFromName(String name) {
        return name.replaceAll("\\W", "");
    }

}
