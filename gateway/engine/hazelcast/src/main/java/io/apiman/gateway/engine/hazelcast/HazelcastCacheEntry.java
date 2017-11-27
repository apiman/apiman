/*
 * Copyright 2017 Pete Cornish
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
package io.apiman.gateway.engine.hazelcast;


/**
 * This is what gets stored in the Hazelcast cache.
 *
 * @author Pete Cornish
 */
public class HazelcastCacheEntry<H> {
    private H head;
    private String data;
    private long expiresOn;

    /**
     * Constructor.
     */
    public HazelcastCacheEntry() {
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
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
    public H getHead() {
        return head;
    }

    /**
     * @param head the head to set
     */
    public void setHead(H head) {
        this.head = head;
    }
}
