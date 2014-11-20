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
package io.apiman.manager.test.server;

import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;

/**
 * Default seeder used by tests.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultTestDataSeeder implements ISeeder {
    
    /**
     * @see io.apiman.manager.test.server.ISeeder#seed(io.apiman.manager.api.core.IIdmStorage, io.apiman.manager.api.core.IStorage)
     */
    @Override
    public void seed(IIdmStorage idmStorage, IStorage storage) throws StorageException {
        for (String [] userInfo : TestUsers.USERS) {
            UserBean userBean = new UserBean();
            userBean.setUsername(userInfo[0]);
            userBean.setFullName(userInfo[2]);
            userBean.setEmail(userInfo[3]);
            idmStorage.createUser(userBean);
        }
    }

}
