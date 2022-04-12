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
import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
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

    @Inject
    private ApiManagerConfig config;

    @Inject
    private CriteriaBuilderFactory criteriaBuilderFactory;

    /**
     * Constructor.
     */
    public AbstractJpaStorage() {
    }

    protected Jdbi getJdbi() {
        return Jdbi.create(lookupDS(config.getHibernateDataSource()));
    }

    protected CriteriaBuilderFactory getCriteriaBuilderFactory() {
        return criteriaBuilderFactory;
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
        try {
            EntityManager entityManager = getActiveEntityManager();
            OrganizationBean orgBean = entityManager.find(OrganizationBean.class, organizationId);
            Object key = new OrganizationBasedCompositeId(orgBean, id);
            return entityManager.find(type, key);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    protected <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, List<OrderByBean> uniqueOrderIdentifiers, Class<T> type) throws StorageException {
        return find(criteria, uniqueOrderIdentifiers, (criteriaBuilder) -> {}, type, type.getSimpleName());
    }

    /**
     * Get a list of entities based on the provided criteria and entity type.
     * @param criteria
     * @param type
     * @throws StorageException if a storage problem occurs while storing a bean
     */
    protected <T> SearchResultsBean<T> find(SearchCriteriaBean criteria,
                                            List<OrderByBean> uniqueOrderIdentifiers,
                                            Consumer<CriteriaBuilder<T>> builderCallback,
                                            Class<T> type,
                                            String typeAlias) throws StorageException {
        try {
            // Set some default in the case that paging information was not included in the request.
            PagingBean paging = criteria.getPaging();
            if (paging == null) {
                paging = PagingBean.create(1, 20);
            }
            int page = paging.getPage();
            int pageSize = paging.getPageSize();
            int start = (page - 1) * pageSize;

            CriteriaBuilder<T> cb = criteriaBuilderFactory
                            .create(getActiveEntityManager(), type)
                            .from(type, typeAlias);

            // Apply filters from user-provided criteria.
            cb = applySearchCriteriaToQuery(typeAlias, criteria, cb, false);

            // Allow caller to modify the query, for example to add permissions constraints.
            builderCallback.accept(cb);

            if (ApimanH2Dialect.class.getCanonicalName().equalsIgnoreCase(getDialect())) {
                // Pagination sometimes generates #in SQL statements that H2 currently does not support due to composite key
                //    (x,y) IN (select x,y ... subquery) which works in all DBs except H2
                for (OrderByBean order : uniqueOrderIdentifiers) {
                    cb = cb.orderBy(order.getName(), order.isAscending());
                }
                List<T> resultList = cb.getResultList();
                return new SearchResultsBean<T>()
                        .setTotalSize(Math.toIntExact(resultList.size()))
                        .setBeans(resultList);
            } else {
                PaginatedCriteriaBuilder<T> paginatedCb = cb.page(start, pageSize);
                /*
                 * Add an orderBy of unique identifiers *last* in the query; this is required for pagination to work properly.
                 *
                 * The tuple formed by the fields in this orderBy clause MUST be unique, otherwise BlazePersistence will throw an exception.
                 *
                 * Without a unique tuple, the ordering may be unstable, which can cause pagination to behave unpredictably.
                 */
                for (OrderByBean order : uniqueOrderIdentifiers) {
                    paginatedCb = paginatedCb.orderBy(order.getName(), order.isAscending());
                }

                PagedList<T> resultList = paginatedCb.getResultList();

                return new SearchResultsBean<T>()
                        .setTotalSize(Math.toIntExact(resultList.getTotalSize()))
                        .setBeans(resultList);
            }
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * Applies the criteria found in the {@link SearchCriteriaBean} to the JPA query.
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T> CriteriaBuilder<T> applySearchCriteriaToQuery(String rootAlias, SearchCriteriaBean criteria, CriteriaBuilder<T> cb, boolean countOnly) {
        List<SearchCriteriaFilterBean> filters = criteria.getFilters();
        if (filters != null && !filters.isEmpty()) {
            for (SearchCriteriaFilterBean filter : filters) {
                final String name = cb.getPath(filter.getName()).getPath();
                if (filter.getOperator() == SearchCriteriaFilterOperator.eq) {
                    com.blazebit.persistence.Path path = cb.getPath(filter.getName());
                    Class<?> pathKlazz = path.getJavaType();
                    if (pathKlazz.isEnum()) {
                        cb = cb.where(name).eq(Enum.valueOf((Class) pathKlazz, filter.getValue()));
                    } else {
                        cb = cb.where(name).eq(filter.getValue());
                    }
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.bool_eq) {
                    cb = cb.where(name).eq(Boolean.valueOf(filter.getValue()));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.gt) {
                    cb = cb.where(name).gt(Long.valueOf(filter.getValue()));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.gte) {
                    cb = cb.where(name).ge(Long.valueOf(filter.getValue()));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.lt) {
                    cb = cb.where(name).lt(Long.valueOf(filter.getValue()));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.lte) {
                    cb = cb.where(name).le(Long.valueOf(filter.getValue()));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.neq) {
                    cb = cb.where(name).notEq(Long.valueOf(filter.getValue()));
                } else if (filter.getOperator() == SearchCriteriaFilterOperator.like) {
                    cb = cb.where(name).like(false).value(filter.getValue().toUpperCase().replace('*', '%')).noEscape();
                }
            }
        }

        OrderByBean orderBy = criteria.getOrderBy();
        if (orderBy != null && !countOnly) {
            if (orderBy.isAscending()) {
                cb = cb.orderByAsc(orderBy.getName());
            } else {
                cb = cb.orderByDesc(orderBy.getName());
            }
        }

        return cb;
    }

    @SuppressWarnings("unchecked")
    protected <T> Optional<T> getOne(TypedQuery<T> query) {
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
    private static class EntityIterator<T> implements Iterator<T> {

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

    public String getDialect() {
        return (String) getActiveEntityManager().getEntityManagerFactory().getProperties().get("hibernate.dialect"); //$NON-NLS-1$
    }
}
