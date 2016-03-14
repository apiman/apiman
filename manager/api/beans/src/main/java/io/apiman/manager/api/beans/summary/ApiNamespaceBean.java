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

package io.apiman.manager.api.beans.summary;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author eric.wittmann@gmail.com
 */
@JsonInclude(Include.NON_NULL)
public class ApiNamespaceBean implements Serializable {

    private static final long serialVersionUID = 2550133294375757051L;
    
    private String name;
    private boolean ownedByUser;
    private boolean current;
    
    /**
     * Constructor.
     */
    public ApiNamespaceBean() {
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
     * @return the ownedByUser
     */
    public boolean isOwnedByUser() {
        return ownedByUser;
    }

    /**
     * @param ownedByUser the ownedByUser to set
     */
    public void setOwnedByUser(boolean ownedByUser) {
        this.ownedByUser = ownedByUser;
    }

    /**
     * @return the current
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * @param current the current to set
     */
    public void setCurrent(boolean current) {
        this.current = current;
    }

}
