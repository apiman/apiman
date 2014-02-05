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
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.overlord.apiman.dt.api.persist.AlreadyExistsException;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPA implementation of the storage interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class JpaStorage implements IStorage {
    
    private static Logger logger = LoggerFactory.getLogger(JpaStorage.class);

    @Inject
    private EntityManagerFactory emf;

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
        EntityManager entityManager = emf.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(bean);
            entityManager.getTransaction().commit();
        } catch (EntityExistsException e) {
            entityManager.getTransaction().rollback();
            throw new AlreadyExistsException();
        } catch (Throwable t) {
            entityManager.getTransaction().rollback();
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#update(java.lang.Object)
     */
    @Override
    public <T> void update(T bean) throws StorageException, DoesNotExistException {
        EntityManager entityManager = emf.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(bean);
            entityManager.getTransaction().commit();
        } catch (IllegalArgumentException e) {
            entityManager.getTransaction().rollback();
            throw new DoesNotExistException();
        } catch (Throwable t) {
            entityManager.getTransaction().rollback();
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#delete(java.lang.Object)
     */
    @Override
    public <T> void delete(T bean) throws StorageException, DoesNotExistException {
        EntityManager entityManager = emf.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(bean);
            entityManager.remove(bean);
            entityManager.getTransaction().commit();
        } catch (IllegalArgumentException e) {
            entityManager.getTransaction().rollback();
            throw new DoesNotExistException();
        } catch (Throwable t) {
            entityManager.getTransaction().rollback();
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#get(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> T get(String id, Class<T> type) throws StorageException, DoesNotExistException {
        T rval = null;
        try {
            EntityManager entityManager = emf.createEntityManager();
            rval = entityManager.find(type, id);
            entityManager.close();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
        if (rval == null)
            throw new DoesNotExistException();
        return rval;
    }

}
