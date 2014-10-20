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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.overlord.apiman.dt.api.beans.orgs.OrgBasedCompositeId;
import org.overlord.apiman.dt.api.beans.search.OrderByBean;
import org.overlord.apiman.dt.api.beans.search.PagingBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaFilterBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.core.exceptions.AlreadyExistsException;
import org.overlord.apiman.dt.api.core.exceptions.ConstraintViolationException;
import org.overlord.apiman.dt.api.core.exceptions.DoesNotExistException;
import org.overlord.apiman.dt.api.core.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class that JPA storage impls can extend.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractJpaStorage {
    
    private static Logger logger = LoggerFactory.getLogger(AbstractJpaStorage.class);

    @Inject
    private IEntityManagerFactoryAccessor emfAccessor;
    
    private static ThreadLocal<EntityManager> activeEM = new ThreadLocal<EntityManager>();

    /**
     * Constructor.
     */
    public AbstractJpaStorage() {
    }

    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#beginTx()
     */
    protected void beginTx() throws StorageException {
        if (activeEM.get() != null) {
            throw new StorageException("Transaction already active.");
        }
        EntityManager entityManager = emfAccessor.getEntityManagerFactory().createEntityManager();
        activeEM.set(entityManager);
        entityManager.getTransaction().begin();
    }
    
    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#commitTx()
     */
    protected void commitTx() throws StorageException {
        if (activeEM.get() == null) {
            throw new StorageException("Transaction not active.");
        }
        
        try {
            activeEM.get().getTransaction().commit();
            activeEM.get().close();
            activeEM.set(null);
        } catch (EntityExistsException e) {
            throw new AlreadyExistsException();
        } catch (RollbackException e) {
            if (JpaUtil.isConstraintViolation(e)) {
                logger.error(e.getMessage(), e);
                throw new ConstraintViolationException(e);
            } else {
                logger.error(e.getMessage(), e);
                throw new StorageException(e);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#rollbackTx()
     */
    protected void rollbackTx() {
        if (activeEM.get() == null) {
            throw new RuntimeException("Transaction not active.");
        }
        try {
            JpaUtil.rollbackQuietly(activeEM.get());
        } finally {
            activeEM.get().close();
            activeEM.set(null);
        }
    }
    
    /**
     * @return the thread's entity manager
     * @throws StorageException
     */
    protected EntityManager getActiveEntityManager() throws StorageException {
        EntityManager entityManager = activeEM.get();
        if (entityManager == null) {
            throw new StorageException("Transaction not active.");
        }
        return entityManager;
    }
    
    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#create(java.lang.Object)
     */
    public <T> void create(T bean) throws StorageException, AlreadyExistsException {
        if (bean == null) {
            return;
        }
        EntityManager entityManager = getActiveEntityManager();
        try {
            entityManager.persist(bean);
        } catch (EntityExistsException e) {
            logger.error(e.getMessage(), e);
            throw new AlreadyExistsException();
        } catch (RollbackException e) {
            if (JpaUtil.isConstraintViolation(e)) {
                logger.error(e.getMessage(), e);
                throw new ConstraintViolationException(e);
            } else {
                logger.error(e.getMessage(), e);
                throw new StorageException(e);
            }
        } catch (PersistenceException e) {
            if (JpaUtil.isConstraintViolation(e)) {
                logger.error(e.getMessage(), e);
                throw new ConstraintViolationException(e);
            } else {
                logger.error(e.getMessage(), e);
                throw new StorageException(e);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#update(java.lang.Object)
     */
    public <T> void update(T bean) throws StorageException, DoesNotExistException {
        EntityManager entityManager = getActiveEntityManager();
        try {
            entityManager.merge(bean);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            throw new DoesNotExistException();
        } catch (RollbackException e) {
            if (JpaUtil.isConstraintViolation(e)) {
                logger.error(e.getMessage(), e);
                throw new ConstraintViolationException(e);
            } else {
                logger.error(e.getMessage(), e);
                throw new StorageException(e);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#delete(java.lang.Object)
     */
    public <T> void delete(T bean) throws StorageException, DoesNotExistException {
        EntityManager entityManager = getActiveEntityManager();
        try {
            entityManager.remove(bean);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            throw new DoesNotExistException();
        } catch (RollbackException e) {
            if (JpaUtil.isConstraintViolation(e)) {
                logger.error(e.getMessage(), e);
                throw new ConstraintViolationException(e);
            } else {
                logger.error(e.getMessage(), e);
                throw new StorageException(e);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#get(java.lang.Long, java.lang.Class)
     */
    public <T> T get(Long id, Class<T> type) throws StorageException, DoesNotExistException {
        T rval = null;
        EntityManager entityManager = getActiveEntityManager();
        try {
            rval = entityManager.find(type, id);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
        if (rval == null)
            throw new DoesNotExistException();
        return rval;
    }
    
    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#get(java.lang.String, java.lang.Class)
     */
    public <T> T get(String id, Class<T> type) throws StorageException, DoesNotExistException {
        T rval = null;
        EntityManager entityManager = getActiveEntityManager();
        try {
            rval = entityManager.find(type, id);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
        if (rval == null)
            throw new DoesNotExistException();
        return rval;
    }

    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#get(java.lang.String, java.lang.String, java.lang.Class)
     */
    public <T> T get(String organizationId, String id, Class<T> type) throws StorageException, DoesNotExistException {
        T rval = null;
        EntityManager entityManager = getActiveEntityManager();
        try {
            Object key = new OrgBasedCompositeId(organizationId, id);
            rval = entityManager.find(type, key);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
        if (rval == null)
            throw new DoesNotExistException();
        return rval;
    }
    
    /**
     * @see org.overlord.apiman.dt.api.core.IStorage#find(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean, java.lang.Class)
     */
    public <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, Class<T> type) throws StorageException {
        SearchResultsBean<T> results = new SearchResultsBean<T>();
        EntityManager entityManager = getActiveEntityManager();
        try {
            // Set some default in the case that paging information was not included in the request.
            PagingBean paging = criteria.getPaging();
            if (paging == null) {
                paging = new PagingBean();
                paging.setPage(1);
                paging.setPageSize(20);
            }
            int page = paging.getPage();
            int pageSize = paging.getPageSize();
            int start = (page - 1) * pageSize;
            
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
            Root<T> from = criteriaQuery.from(type);
            applySearchCriteriaToQuery(criteria, builder, criteriaQuery, from, false);
            TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
            typedQuery.setFirstResult(start);
            typedQuery.setMaxResults(pageSize+1);
            boolean hasMore = false;
            
            // Now query for the actual results
            List<T> resultList = typedQuery.getResultList();
            
            // Check if we got back more than we actually needed.
            if (resultList.size() > pageSize) {
                resultList.remove(resultList.size() - 1);
                hasMore = true;
            }
            
            // If there are more results than we needed, then we will need to do another
            // query to determine how many rows there are in total
            int totalSize = start + resultList.size();
            if (hasMore) {
                totalSize = executeCountQuery(criteria, entityManager, type);
            }
            results.setTotalSize(totalSize);
            results.setBeans(resultList);
            return results;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * Gets a count of the number of rows that would be returned by the search.
     * @param criteria
     * @param entityManager
     * @param type
     */
    protected <T> int executeCountQuery(SearchCriteriaBean criteria, EntityManager entityManager, Class<T> type) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<T> from = countQuery.from(type);
        countQuery.select(builder.count(from));
        applySearchCriteriaToQuery(criteria, builder, countQuery, from, true);
        TypedQuery<Long> query = entityManager.createQuery(countQuery);
        return query.getSingleResult().intValue();
    }
    
    /**
     * Applies the criteria found in the {@link SearchCriteriaBean} to the JPA query.
     * @param criteria
     * @param builder
     * @param query
     * @param from
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T> void applySearchCriteriaToQuery(SearchCriteriaBean criteria, CriteriaBuilder builder,
            CriteriaQuery<?> query, Root<T> from, boolean countOnly) {
        
        List<SearchCriteriaFilterBean> filters = criteria.getFilters();
        if (filters != null && !filters.isEmpty()) {
            List<Predicate> predicates = new ArrayList<Predicate>();
            for (SearchCriteriaFilterBean filter : filters) {
                if (filter.getOperator().intern() == SearchCriteriaFilterBean.OPERATOR_EQ) {
                    Path<Object> path = from.get(filter.getName());
                    Class<?> pathc = path.getJavaType();
                    if (pathc.isAssignableFrom(String.class)) {
                        predicates.add(builder.equal(path, filter.getValue()));
                    } else if (pathc.isEnum()) {
                        predicates.add(builder.equal(path, Enum.valueOf((Class)pathc, filter.getValue())));
                    }
                } else if (filter.getOperator().intern() == SearchCriteriaFilterBean.OPERATOR_BOOL_EQ) {
                    predicates.add(builder.equal(from.<Boolean>get(filter.getName()), Boolean.valueOf(filter.getValue())));
                } else if (filter.getOperator().intern() == SearchCriteriaFilterBean.OPERATOR_GT) {
                    predicates.add(builder.greaterThan(from.<Long>get(filter.getName()), new Long(filter.getValue())));
                } else if (filter.getOperator().intern() == SearchCriteriaFilterBean.OPERATOR_GTE) {
                    predicates.add(builder.greaterThanOrEqualTo(from.<Long>get(filter.getName()), new Long(filter.getValue())));
                } else if (filter.getOperator().intern() == SearchCriteriaFilterBean.OPERATOR_LT) {
                    predicates.add(builder.lessThan(from.<Long>get(filter.getName()), new Long(filter.getValue())));
                } else if (filter.getOperator().intern() == SearchCriteriaFilterBean.OPERATOR_LTE) {
                    predicates.add(builder.lessThanOrEqualTo(from.<Long>get(filter.getName()), new Long(filter.getValue())));
                } else if (filter.getOperator().intern() == SearchCriteriaFilterBean.OPERATOR_NEQ) {
                    predicates.add(builder.notEqual(from.get(filter.getName()), filter.getValue()));
                } else if (filter.getOperator().intern() == SearchCriteriaFilterBean.OPERATOR_LIKE) {
                    predicates.add(builder.like(builder.upper(from.<String>get(filter.getName())), filter.getValue().toUpperCase().replace('*', '%')));
                }
            }
            query.where(predicates.toArray(new Predicate[predicates.size()]));
        }
        OrderByBean orderBy = criteria.getOrderBy();
        if (orderBy != null && !countOnly) {
            if (orderBy.isAscending()) {
                query.orderBy(builder.asc(from.get(orderBy.getName())));
            } else {
                query.orderBy(builder.desc(from.get(orderBy.getName())));
            }
        }
    }

    /**
     * @return the emfAccessor
     */
    public IEntityManagerFactoryAccessor getEmfAccessor() {
        return emfAccessor;
    }

    /**
     * @param emfAccessor the emfAccessor to set
     */
    public void setEmfAccessor(IEntityManagerFactoryAccessor emfAccessor) {
        this.emfAccessor = emfAccessor;
    }

}
