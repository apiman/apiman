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
package io.apiman.gateway.engine.ispn;

import io.apiman.gateway.engine.components.ICacheStoreComponent;

import java.io.Serializable;

/**
 * A single entry in the ISPN implementation of the {@link ICacheStoreComponent}.
 *
 * @author eric.wittmann@redhat.com
 */
public class InfinispanCacheEntry implements Serializable {

    private static final long serialVersionUID = 7699200317440546470L;

    private Object head;
    private byte [] data;
    private long expiresOn;

    /**
     * Constructor.
     */
    public InfinispanCacheEntry() {
    }

    /**
     * @return the expiresOn
     */
    public long getExpiresOn() {
        return expiresOn;
    }

    /**
     * @param expiresOn the expiresOn to set
     */
    public void setExpiresOn(long expiresOn) {
        this.expiresOn = expiresOn;
    }

    /**
     * @return the head
     */
    public Object getHead() {
        return head;
    }

    /**
     * @param head the head to set
     */
    public void setHead(Object head) {
        this.head = head;
    }

    /**
     * @return the data
     */
    public byte [] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte [] data) {
        this.data = data;
    }

}
