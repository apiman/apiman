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

package org.overlord.apiman.dt.api.beans.idm;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models a single user.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
@Entity
@Table(name = "users")
public class UserBean {
    
    @Id
    private String username;
    private String fullName;
    private String email;
    private Date joinedOn;
    
    /**
     * Constructor.
     */
    public UserBean() {
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the joinedOn
     */
    public Date getJoinedOn() {
        return joinedOn;
    }

    /**
     * @param joinedOn the joinedOn to set
     */
    public void setJoinedOn(Date joinedOn) {
        this.joinedOn = joinedOn;
    }

}
