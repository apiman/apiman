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

package io.apiman.manager.api.beans.apis;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A single item in the API status.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class StatusItemBean {

    private String id;
    private String name;
    private boolean done;
    private boolean optional;
    private String remediation;

    /**
     * Constructor.
     */
    public StatusItemBean() {
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the done
     */
    public boolean isDone() {
        return done;
    }

    /**
     * @param done the done to set
     */
    public void setDone(boolean done) {
        this.done = done;
    }

    /**
     * @return the remediation
     */
    public String getRemediation() {
        return remediation;
    }

    /**
     * @param remediation the remediation to set
     */
    public void setRemediation(String remediation) {
        this.remediation = remediation;
    }

    /**
     * @return the optional
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @param optional the optional to set
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

}
