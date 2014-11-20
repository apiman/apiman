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
package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.exceptions.AlreadyExistsException;
import io.apiman.manager.api.core.exceptions.DoesNotExistException;
import io.apiman.manager.api.core.exceptions.StorageException;

/**
 * Represents the persistent storage interface for Apiman DT.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IStorage {
    
    /**
     * Starts a transaction for the current thread.
     * @throws StorageException
     */
    public void beginTx() throws StorageException;
    
    /**
     * Commits the currently active transaction.
     * @throws StorageException
     */
    public void commitTx() throws StorageException;
    
    /**
     * Rolls back the currently active transaction.
     * @throws StorageException
     */
    public void rollbackTx();

    /**
     * Creates (stores) an entity.
     * @param bean
     * @throws StorageException
     * @throws AlreadyExistsException
     */
    public <T> void create(T bean) throws StorageException, AlreadyExistsException;

    /**
     * Updates an entity in the storage layer.
     * @param bean
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public <T> void update(T bean) throws StorageException, DoesNotExistException;

    /**
     * Delets an entity.
     * @param bean
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public <T> void delete(T bean) throws StorageException, DoesNotExistException;

    /**
     * Gets an entity by its unique id.
     * @param id
     * @param type
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public <T> T get(Long id, Class<T> type) throws StorageException, DoesNotExistException;

    /**
     * Gets an entity by its unique id.
     * @param id
     * @param type
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public <T> T get(String id, Class<T> type) throws StorageException, DoesNotExistException;

    /**
     * Gets an entity by its organization ID and unique ID.  Use this form when asking
     * for an entity that cannot be uniquely identified by its ID field alone.
     * @param organizationId
     * @param id
     * @param type
     * @throws StorageException
     * @throws DoesNotExistException
     */
    public <T> T get(String organizationId, String id, Class<T> type) throws StorageException, DoesNotExistException;

    /**
     * Finds entities by provided criteria.
     * @param criteria
     * @param type
     * @throws StorageException
     */
    public <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, Class<T> type) throws StorageException;
    
    /**
     * Called to store an audit entry for the given bean.
     * @param bean
     * @param entry
     * @throws StorageException
     */
    public void createAuditEntry(AuditEntryBean entry) throws StorageException;
    
    /**
     * Gets the audit log for an entity.
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param type
     * @param paging
     * @throws StorageException
     */
    public <T> SearchResultsBean<AuditEntryBean> auditEntity(String organizationId, String entityId,
            String entityVersion, Class<T> type, PagingBean paging) throws StorageException;

    /**
     * Gets the audit log for a user.
     * @param userId
     * @param paging
     * @throws StorageException
     */
    public <T> SearchResultsBean<AuditEntryBean> auditUser(String userId, PagingBean paging) throws StorageException;
}
