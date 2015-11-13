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

package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.core.exceptions.StorageException;

/**
 * This class is used to bootstrap new users.  This bootstrapper is used
 * whenever a new user logs into the API Manager UI for the first time.
 * The idea is to do any user initialization necessary.  This component
 * can be customized by users so that they can provide specific user
 * behavior (such as, for example, auto-granting roles or creating a new
 * organization).
 *
 * @author eric.wittmann@redhat.com
 */
public interface INewUserBootstrapper {
    
    /**
     * Called to bootstrap a user.
     * @param user
     * @param storage
     * @throws StorageException
     */
    public void bootstrapUser(UserBean user, IStorage storage) throws StorageException;

}
