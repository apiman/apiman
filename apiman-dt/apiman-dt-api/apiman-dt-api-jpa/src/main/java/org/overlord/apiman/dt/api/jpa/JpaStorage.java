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
package org.overlord.apiman.dt.api.jpa;

import javax.enterprise.context.ApplicationScoped;

import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.persist.AlreadyExistsException;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.persist.StorageException;

/**
 * A JPA implementation of the storage interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class JpaStorage extends AbstractJpaStorage implements IStorage {

    /**
     * Constructor.
     */
    public JpaStorage() {
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#create(java.lang.Object)
     */
    @Override
    public <T> void create(T bean) throws StorageException, AlreadyExistsException {
        super.create(bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#update(java.lang.Object)
     */
    @Override
    public <T> void update(T bean) throws StorageException, DoesNotExistException {
        super.update(bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#delete(java.lang.Object)
     */
    @Override
    public <T> void delete(T bean) throws StorageException, DoesNotExistException {
        super.delete(bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#get(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> T get(String id, Class<T> type) throws StorageException, DoesNotExistException {
        return super.get(id, type);
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#find(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean, java.lang.Class)
     */
    @Override
    public <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, Class<T> type) throws StorageException {
        return super.find(criteria, type);
    }

}
