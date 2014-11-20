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
package io.apiman.manager.api.rest.impl.audit;

/**
 * Models a change in an entity field's value for auditing purposes.
 *
 * @author eric.wittmann@redhat.com
 */
public class EntityFieldChange {
    
    private String before;
    private String after;
    
    /**
     * Constructor.
     */
    public EntityFieldChange() {
    }

    /**
     * Constructor.
     * @param before
     * @param after
     */
    public EntityFieldChange(String before, String after) {
        this.setBefore(before);
        this.setAfter(after);
    }

    /**
     * @return the before
     */
    public String getBefore() {
        return before;
    }

    /**
     * @param before the before to set
     */
    public void setBefore(String before) {
        this.before = before;
    }

    /**
     * @return the after
     */
    public String getAfter() {
        return after;
    }

    /**
     * @param after the after to set
     */
    public void setAfter(String after) {
        this.after = after;
    }

}
