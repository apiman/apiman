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
package io.apiman.gateway.engine.es.beans;

/**
 * A simple bean used to store the last updated time for the ES registry store
 * as a whole.  In other words, whenever the ES registry index is updated
 *
 * @author eric.wittmann@redhat.com
 */
public class DataVersionBean {

    private long updatedOn;

    /**
     * Constructor.
     */
    public DataVersionBean() {
    }

    /**
     * @return the updatedOn
     */
    public long getUpdatedOn() {
        return updatedOn;
    }

    /**
     * @param updatedOn the updatedOn to set
     */
    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

}
