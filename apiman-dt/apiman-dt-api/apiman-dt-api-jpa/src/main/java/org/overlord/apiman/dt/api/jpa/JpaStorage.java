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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.overlord.apiman.dt.api.beans.search.OrderByBean;
import org.overlord.apiman.dt.api.beans.search.PagingBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaFilterBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
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
        EntityManager entityManager = emf.createEntityManager();
        try {
            rval = entityManager.find(type, id);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
        if (rval == null)
            throw new DoesNotExistException();
        return rval;
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#find(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean, java.lang.Class)
     */
    @Override
    public <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, Class<T> type) throws StorageException {
        SearchResultsBean<T> results = new SearchResultsBean<T>();
        EntityManager entityManager = emf.createEntityManager();
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
        } finally {
            entityManager.close();
        }
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
    protected <T> void applySearchCriteriaToQuery(SearchCriteriaBean criteria, CriteriaBuilder builder,
            CriteriaQuery<?> query, Root<T> from, boolean countOnly) {
        List<SearchCriteriaFilterBean> filters = criteria.getFilters();
        if (filters != null && !filters.isEmpty()) {
            List<Predicate> predicates = new ArrayList<Predicate>();
            for (SearchCriteriaFilterBean filter : filters) {
                if (filter.getOperator().intern() == SearchCriteriaFilterBean.OPERATOR_EQ) {
                    predicates.add(builder.equal(from.get(filter.getName()), filter.getValue()));
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
                    predicates.add(builder.like(from.<String>get(filter.getName()), filter.getValue().replace('*', '%')));
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

}
