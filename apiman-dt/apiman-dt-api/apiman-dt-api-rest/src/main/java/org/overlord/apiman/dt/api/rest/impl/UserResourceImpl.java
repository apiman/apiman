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

package org.overlord.apiman.dt.api.rest.impl;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;

import org.overlord.apiman.dt.api.beans.users.UserBean;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;

/**
 * Implementation of the User API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class UserResourceImpl implements IUserResource {
    
    /**
     * Constructor.
     */
    public UserResourceImpl() {
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#getUser(java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public UserBean getUser(String username) throws UserNotFoundException {
        if (username.equals("ewittman")) {
            System.out.println("Returning user 'ewittman'");
            UserBean user = new UserBean();
            user.setEmail("eric.wittmann@redhat.com");
            user.setFullName("Eric Wittmann");
            user.setJoinedOn(new Date());
            user.setUsername(username);
            return user;
        } else {
            System.out.println("Throwing user not found.");
            throw UserNotFoundException.create(username);
        }
    }
}
