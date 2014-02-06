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
import javax.persistence.RollbackException;

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
            rollbackQuietly(entityManager);
            throw new AlreadyExistsException();
        } catch (RollbackException e) {
            if (isConstraintViolation(e)) {
                throw new AlreadyExistsException();
            } else {
                rollbackQuietly(entityManager);
                logger.error(e.getMessage(), e);
                throw new StorageException(e);
            }
        } catch (Throwable t) {
            rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }

    /**
     * Returns true if the given exception is a unique constraint violation.  This
     * is useful to detect whether someone is trying to persist an entity that 
     * already exists.  It allows us to simply assume that persisting a new entity
     * will work, without first querying the DB for the existence of that entity.
     * 
     * Note that my understanding is that JPA is supposed to throw an {@link EntityExistsException}
     * when the row already exists.  However, this is not always the case, based on
     * experience.  Or perhaps it only throws the exception if the entity is already
     * loaded from the DB and exists in the {@link EntityManager}.
     * @param e
     */
    protected boolean isConstraintViolation(RollbackException e) {
        Throwable cause = e;
        while (cause != cause.getCause() && cause.getCause() != null) {
            if (cause.getClass().getSimpleName().equals("ConstraintViolationException"))
                return true;
            cause = cause.getCause();
        }
        return false;
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
            rollbackQuietly(entityManager);
            throw new DoesNotExistException();
        } catch (Throwable t) {
            rollbackQuietly(entityManager);
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
            rollbackQuietly(entityManager);
            throw new DoesNotExistException();
        } catch (Throwable t) {
            rollbackQuietly(entityManager);
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

    /**
     * @param entityManager
     */
    protected void rollbackQuietly(EntityManager entityManager) {
        if (entityManager.getTransaction().isActive() && entityManager.getTransaction().getRollbackOnly()) {
            try {
                entityManager.getTransaction().rollback();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
