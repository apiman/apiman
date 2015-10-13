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

import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;

/**
 * Interface implemented by classes that seed the apiman database with data on
 * startup.
 * 
 * @author eric.wittmann@redhat.com
 */
public interface ISeeder {
    
    public static final String SYSTEM_PROPERTY = "apiman-manager.seeder.class"; //$NON-NLS-1$
    
    /**
     * @param storage the storage
     * @throws StorageException when storage attempt fails
     */
    public void seed(IStorage storage) throws StorageException;

}
