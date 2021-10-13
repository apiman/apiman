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
package io.apiman.manager.api.jpa;

import io.apiman.manager.api.beans.orgs.OrganizationBasedCompositeId;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.search.OrderByBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.hibernate.Session;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class that JPA storage impls can extend.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractJpaStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJpaStorage.class);

    @Inject
    private EntityManagerFactoryAccessor emf;

    /**
     * Constructor.
     */
    public AbstractJpaStorage() {
    }

    protected Jdbi getJdbi() {
        return Jdbi.create(lookupDS("java:/apiman/datasources/apiman-manager"));
    }

    /**
     * @return the thread's entity manager
     */
    public EntityManager getActiveEntityManager() {
        return emf.getEntityManager();
    }

    public Session getSession() {
        return getActiveEntityManager().unwrap(Session.class);
    }

    private static javax.sql.DataSource lookupDS(String dsJndiLocation) {
        javax.sql.DataSource ds;
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(dsJndiLocation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (ds == null) {
            throw new RuntimeException("Datasource not found: " + dsJndiLocation); //$NON-NLS-1$
        }
        return ds;
    }

    /**
     * @param bean the bean to create
     * @throws StorageException if a storage problem occurs while storing a bean
     */
    public <T> void create(T bean) throws StorageException {
        if (bean == null) {
            return;
        }
        EntityManager entityManager = getActiveEntityManager();
        try {
            entityManager.persist(bean);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @param bean the bean to update
     * @throws StorageException if a storage problem occurs while storing a bean
     */
    public <T> void update(T bean) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();
        try {
            if (!entityManager.contains(bean)) {
                entityManager.merge(bean);
            }
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * Delete using bean
     *
     * @param bean the bean to delete
     * @throws StorageException if a storage problem occurs while storing a bean
     */
    public <T> void delete(T bean) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();
        try {
            //entityManager.merge(bean);
            entityManager.remove(bean);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * Get object of type T
     *
     * @param id identity key
     * @param type class of type T
     * @return Instance of type T
     * @throws StorageException if a storage problem occurs while storing a bean
     */
    public <T> T get(Long id, Class<T> type) throws StorageException {
        T rval;
        EntityManager entityManager = getActiveEntityManager();
        try {
            rval = entityManager.find(type, id);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
        return rval;
    }

    /**
     * Get object of type T
     *
     * @param id identity key
     * @param type class of type T
     * @return Instance of type T
     * @throws StorageException if a storage problem occurs while storing a bean
     */
    public <T> T get(String id, Class<T> type) throws StorageException {
        T rval;
        EntityManager entityManager = getActiveEntityManager();
        try {
            rval = entityManager.find(type, id);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
        return rval;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T> Iterator<T> getAll(Class<T> type, Query query) throws StorageException {
        return new EntityIterator(type, query);
    }

    /**
     * Get object of type T
     *
     * @param organizationId org id
     * @param id identity
     * @param type class of type T
     * @return Instance of type T
     * @throws StorageException if a storage problem occurs while storing a bean
     */
    public <T> T get(String organizationId, String id, Class<T> type) throws StorageException {
        T rval;
        EntityManager entityManager = getActiveEntityManager();
        try {
            OrganizationBean orgBean = entityManager.find(OrganizationBean.class, organizationId);
            Object key = new OrganizationBasedCompositeId(orgBean, id);
            rval = entityManager.find(type, key);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
        return rval;
    }

    /**
     * Get a list of entities based on the provided criteria and entity type.
     * @param criteria
     * @param type
     * @throws StorageException if a storage problem occurs while storing a bean
     */
    protected <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, Class<T> type) throws StorageException {
        SearchResultsBean<T> results = new SearchResultsBean<>();
        EntityManager entityManager = getActiveEntityManager();
        try {
            // Set some default in the case that paging information was not included in the request.
            PagingBean paging = criteria.getPaging();
            if (paging == null) {
                paging = PagingBean.create(1, 20);
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
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * Gets a count of the number of rows that would be returned by the search.
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
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T> void applySearchCriteriaToQuery(SearchCriteriaBean criteria, CriteriaBuilder builder,
            CriteriaQuery<?> query, Root<T> from, boolean countOnly) {

        List<SearchCriteriaFilterBean> filters = criteria.getFilters();
        if (filters != null && !filters.isEmpty()) {
            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteriaFilterBean filter : filters) {
                if (filter.getOperator() == SearchCriteriaFilterOperator.eq) {
                    Path<Object> path = from.get(filter.getName());
                    Class<?> pathc = path.getJavaType();
                    if (pathc.isAssignableFrom(String.class)) {
                        predicates.add(builder.equal(path, filter.getValue()));
                    } else if (pathc.isEnum()) {
                        predicates.add(builder.equal(path, Enum.valueOf((Class)pathc, filter.getValue())));
                    }
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.bool_eq) {
                    predicates.add(builder.equal(from.<Boolean>get(filter.getName()), Boolean.valueOf(filter.getValue())));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.gt) {
                    predicates.add(builder.greaterThan(from.<Long>get(filter.getName()), Long.valueOf(filter.getValue())));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.gte) {
                    predicates.add(builder.greaterThanOrEqualTo(from.<Long>get(filter.getName()), Long.valueOf(filter.getValue())));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.lt) {
                    predicates.add(builder.lessThan(from.<Long>get(filter.getName()), Long.valueOf(filter.getValue())));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.lte) {
                    predicates.add(builder.lessThanOrEqualTo(from.<Long>get(filter.getName()), Long.valueOf(filter.getValue())));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.neq) {
                    predicates.add(builder.notEqual(from.get(filter.getName()), filter.getValue()));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.like) {
                    predicates.add(builder.like(builder.upper(from.<String>get(filter.getName())), filter.getValue().toUpperCase().replace('*', '%')));
                }
            }
            query.where(predicates.toArray(new Predicate[0]));
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

    @SuppressWarnings("unchecked")
    protected <T> Optional<T> getOne(Query query) {
        List<T> resultList = (List<T>) query.getResultList();

        if (resultList.size() > 1) {
            throw new IllegalStateException("More than one result for query");
        }

        if (resultList.size() == 0) {
            return Optional.empty();
        }

        return Optional.of(resultList.get(0));
    }

    /**
     * Allows iterating over all entities of a given type.
     * @author eric.wittmann@redhat.com
     */
    private class EntityIterator<T> implements Iterator<T> {

        private Query query;
        private int pageIndex = 0;
        private int pageSize = 100;

        private int resultIndex;
        private List<T> results;

        /**
         * Constructor.
         * @param query the query
         * @throws StorageException if a storage problem occurs while storing a bean.
         */
        public EntityIterator(Class<T> type, Query query) throws StorageException {
            this.query = query;
            fetch();
        }

        /**
         * Initialize the search.
         */
        private void fetch() {
            if (results != null && results.size() < pageSize) {
                results = new ArrayList<>();
            } else {
                query.setFirstResult(pageIndex);
                query.setMaxResults(pageSize);
                results = query.getResultList();
            }
            resultIndex = 0;
            pageIndex += pageSize;
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return resultIndex < results.size();
        }

        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public T next() {
            T rval = results.get(resultIndex++);
            if (resultIndex >= results.size()) {
                fetch();
            }
            return rval;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
