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
package io.apiman.manager.ui.client.local.services;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;

/**
 * A service that is capable of marshalling and unmarshalling @Portable beans
 * to/from JSON.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class BeanMarshallingService {

    /**
     * Constructor.
     */
    public BeanMarshallingService() {
    }

    /**
     * Marshal a bean to a JSON string.
     * @param bean
     */
    public <T> String marshal(T bean) {
        return MarshallingWrapper.toJSON(bean);
    }
    
    /**
     * Unmarshal from a JSON string into a bean.
     * @param jsonString
     * @param clazz
     */
    public <T> T unmarshal(String jsonString, Class<T> clazz) {
        return MarshallingWrapper.fromJSON(jsonString, clazz);
    }

}
