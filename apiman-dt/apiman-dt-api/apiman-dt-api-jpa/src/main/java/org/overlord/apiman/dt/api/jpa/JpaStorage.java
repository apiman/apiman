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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.overlord.apiman.dt.api.beans.apps.ApplicationBean;
import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.contracts.ContractBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.plans.PlanBean;
import org.overlord.apiman.dt.api.beans.plans.PlanVersionBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.services.ServicePlanBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.PlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServicePlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.persist.AlreadyExistsException;
import org.overlord.apiman.dt.api.persist.DoesNotExistException;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.persist.IStorageQuery;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPA implementation of the storage interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class JpaStorage extends AbstractJpaStorage implements IStorage, IStorageQuery {

    private static Logger logger = LoggerFactory.getLogger(JpaStorage.class);

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
     * @see org.overlord.apiman.dt.api.persist.IStorage#get(java.lang.String, java.lang.String, java.lang.Class)
     */
    @Override
    public <T> T get(String organizationId, String id, Class<T> type) throws StorageException,
            DoesNotExistException {
        return super.get(organizationId, id, type);
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorage#find(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean, java.lang.Class)
     */
    @Override
    public <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, Class<T> type) throws StorageException {
        return super.find(criteria, type);
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getOrgs(java.util.Set)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<OrganizationSummaryBean> getOrgs(Set<String> orgIds) throws StorageException {
        List<OrganizationSummaryBean> orgs = new ArrayList<OrganizationSummaryBean>();
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT o from OrganizationBean o WHERE o.id IN :orgs ORDER BY o.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            List<OrganizationBean> qr = (List<OrganizationBean>) query.getResultList();
            for (OrganizationBean bean : qr) {
                OrganizationSummaryBean summary = new OrganizationSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                orgs.add(summary);
            }
            return orgs;
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getApplicationsInOrg(java.lang.String)
     */
    @Override
    public List<ApplicationSummaryBean> getApplicationsInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<String>();
        orgIds.add(orgId);
        return getApplicationsInOrgs(orgIds);
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getApplicationsInOrgs(java.util.Set)
     */
    @Override
    public List<ApplicationSummaryBean> getApplicationsInOrgs(Set<String> orgIds) throws StorageException {
        List<ApplicationSummaryBean> rval = new ArrayList<ApplicationSummaryBean>();
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT a from ApplicationBean a WHERE a.organizationId IN :orgs ORDER BY a.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<ApplicationBean> qr = (List<ApplicationBean>) query.getResultList();
            for (ApplicationBean bean : qr) {
                ApplicationSummaryBean summary = new ApplicationSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                summary.setNumContracts(0);
                OrganizationBean org = entityManager.find(OrganizationBean.class, bean.getOrganizationId());
                summary.setOrganizationId(org.getId());
                summary.setOrganizationName(org.getName());
                rval.add(summary);
            }
            return rval;
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getServicesInOrg(java.lang.String)
     */
    @Override
    public List<ServiceSummaryBean> getServicesInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<String>();
        orgIds.add(orgId);
        return getServicesInOrgs(orgIds);
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getServicesInOrgs(java.util.Set)
     */
    @Override
    public List<ServiceSummaryBean> getServicesInOrgs(Set<String> orgIds) throws StorageException {
        List<ServiceSummaryBean> rval = new ArrayList<ServiceSummaryBean>();
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT s from ServiceBean s WHERE s.organizationId IN :orgs ORDER BY s.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<ServiceBean> qr = (List<ServiceBean>) query.getResultList();
            for (ServiceBean bean : qr) {
                ServiceSummaryBean summary = new ServiceSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                summary.setCreatedOn(bean.getCreatedOn());
                OrganizationBean org = entityManager.find(OrganizationBean.class, bean.getOrganizationId());
                summary.setOrganizationId(org.getId());
                summary.setOrganizationName(org.getName());
                rval.add(summary);
            }
            return rval;
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getServiceVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ServiceVersionBean getServiceVersion(String orgId, String serviceId, String version)
            throws StorageException {
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT v from ServiceVersionBean v JOIN v.service s WHERE s.organizationId = :orgId AND s.id = :serviceId AND v.version = :version"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("serviceId", serviceId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            
            return (ServiceVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getServiceVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ServiceVersionBean> getServiceVersions(String orgId, String serviceId)
            throws StorageException {
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT v from ServiceVersionBean v JOIN v.service s WHERE s.organizationId = :orgId AND s.id = :serviceId ORDER BY v.id DESC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("serviceId", serviceId); //$NON-NLS-1$
            
            return (List<ServiceVersionBean>) query.getResultList();
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getServiceVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ServicePlanSummaryBean> getServiceVersionPlans(String organizationId, String serviceId,
            String version) throws StorageException {
        List<ServicePlanSummaryBean> plans = new ArrayList<ServicePlanSummaryBean>();
        
        ServiceVersionBean versionBean = getServiceVersion(organizationId, serviceId, version);
        Set<ServicePlanBean> servicePlans = versionBean.getPlans();
        for (ServicePlanBean spb : servicePlans) {
            PlanVersionBean planVersion = getPlanVersion(organizationId, spb.getPlanId(), spb.getVersion());
            ServicePlanSummaryBean summary = new ServicePlanSummaryBean();
            summary.setPlanId(planVersion.getPlan().getId());
            summary.setPlanName(planVersion.getPlan().getName());
            summary.setVersion(spb.getVersion());
            plans.add(summary);
        }
        return plans;
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getApplicationVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationVersionBean getApplicationVersion(String orgId, String applicationId, String version)
            throws StorageException {
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT v from ApplicationVersionBean v JOIN v.application s WHERE s.organizationId = :orgId AND s.id = :applicationId AND v.version = :version"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("applicationId", applicationId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            
            return (ApplicationVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getApplicationVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ApplicationVersionBean> getApplicationVersions(String orgId, String applicationId)
            throws StorageException {
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT v from ApplicationVersionBean v JOIN v.application s WHERE s.organizationId = :orgId AND s.id = :applicationId ORDER BY v.id DESC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("applicationId", applicationId); //$NON-NLS-1$
            
            return (List<ApplicationVersionBean>) query.getResultList();
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getApplicationContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ContractBean> getApplicationContracts(String organizationId, String applicationId,
            String version) throws StorageException {
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = 
                    "SELECT c from ContractBean c " +  //$NON-NLS-1$
                    "  JOIN c.application appv " +  //$NON-NLS-1$
                    "  JOIN appv.application app " +  //$NON-NLS-1$
                    " WHERE app.id = :applicationId " +  //$NON-NLS-1$
                    "   AND app.organizationId = :orgId " +  //$NON-NLS-1$
                    "   AND appv.version = :version " +  //$NON-NLS-1$
                    " ORDER BY c.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.setParameter("applicationId", applicationId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            
            return (List<ContractBean>) query.getResultList();
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getPlansInOrg(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<String>();
        orgIds.add(orgId);
        return getPlansInOrgs(orgIds);
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getPlansInOrgs(java.util.Set)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrgs(Set<String> orgIds) throws StorageException {
        List<PlanSummaryBean> rval = new ArrayList<PlanSummaryBean>();
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT p from PlanBean p WHERE p.organizationId IN :orgs ORDER BY p.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<PlanBean> qr = (List<PlanBean>) query.getResultList();
            for (PlanBean bean : qr) {
                PlanSummaryBean summary = new PlanSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                OrganizationBean org = entityManager.find(OrganizationBean.class, bean.getOrganizationId());
                summary.setOrganizationId(org.getId());
                summary.setOrganizationName(org.getName());
                rval.add(summary);
            }
            return rval;
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getPlanVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getPlanVersion(String orgId, String planId, String version)
            throws StorageException {
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT v from PlanVersionBean v JOIN v.plan s WHERE s.organizationId = :orgId AND s.id = :planId AND v.version = :version"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("planId", planId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            
            return (PlanVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.api.persist.IStorageQuery#getPlanVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<PlanVersionBean> getPlanVersions(String orgId, String planId)
            throws StorageException {
        EntityManager entityManager = emf.createEntityManager();
        try {
            String jpql = "SELECT v from PlanVersionBean v JOIN v.plan s WHERE s.organizationId = :orgId AND s.id = :planId ORDER BY v.id DESC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("planId", planId); //$NON-NLS-1$
            
            return (List<PlanVersionBean>) query.getResultList();
        } catch (Throwable t) {
            JpaUtil.rollbackQuietly(entityManager);
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            entityManager.close();
        }
    }
    
}
