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
package io.apiman.manager.api.jpa.roles;

import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.jpa.AbstractJpaStorage;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPA implementation of the role storage interface {@link IIdmStorage}.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class JpaIdmStorage extends AbstractJpaStorage implements IIdmStorage {
    
    private static Logger logger = LoggerFactory.getLogger(JpaIdmStorage.class);

    /**
     * Constructor.
     */
    public JpaIdmStorage() {
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void createUser(UserBean user) throws StorageException {
        user.setJoinedOn(new Date());
        beginTx();
        try {
            super.create(user);
            commitTx();
        } catch (StorageException e) {
            rollbackTx();
            throw e;
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUser(java.lang.String)
     */
    @Override
    public UserBean getUser(String userId) throws StorageException {
        beginTx();
        try {
            return super.get(userId, UserBean.class);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#updateUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void updateUser(UserBean user) throws StorageException {
        beginTx();
        try {
            super.update(user);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#findUsers(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException {
        beginTx();
        try {
            return super.find(criteria, UserBean.class);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void createRole(RoleBean role) throws StorageException {
        beginTx();
        try {
            super.create(role);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#updateRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void updateRole(RoleBean role) throws StorageException {
        beginTx();
        try {
            super.update(role);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void deleteRole(RoleBean role) throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            
            RoleBean prole = get(role.getId(), RoleBean.class);
            
            // First delete all memberships in this role
            Query query = entityManager.createQuery("DELETE from RoleMembershipBean m WHERE m.roleId = :roleId" ); //$NON-NLS-1$
            query.setParameter("roleId", role.getId()); //$NON-NLS-1$
            query.executeUpdate();

            // Then delete the role itself.
            super.delete(prole);
            
            commitTx();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            rollbackTx();
            throw new StorageException(t);
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getRole(java.lang.String)
     */
    @Override
    public RoleBean getRole(String roleId) throws StorageException {
        beginTx();
        try {
            return getRoleInternal(roleId);
        } finally {
            commitTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#findRoles(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException {
        beginTx();
        try {
            return super.find(criteria, RoleBean.class);
        } finally {
            commitTx();
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createMembership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void createMembership(RoleMembershipBean membership) throws StorageException {
        beginTx();
        try {
            super.create(membership);
        } finally {
            commitTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            Query query = entityManager.createQuery("DELETE FROM RoleMembershipBean m WHERE m.roleId = :roleId AND m.userId = :userId AND m.organizationId = :orgId" ); //$NON-NLS-1$
            query.setParameter("roleId", roleId); //$NON-NLS-1$
            query.setParameter("userId", userId); //$NON-NLS-1$
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.executeUpdate();
            commitTx();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            rollbackTx();
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            Query query = entityManager.createQuery("DELETE FROM RoleMembershipBean m WHERE m.userId = :userId AND m.organizationId = :orgId" ); //$NON-NLS-1$
            query.setParameter("userId", userId); //$NON-NLS-1$
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.executeUpdate();
            commitTx();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            rollbackTx();
            throw new StorageException(t);
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUserMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException {
        Set<RoleMembershipBean> memberships = new HashSet<>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(builder.equal(from.get("userId"), userId)); //$NON-NLS-1$
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            memberships.addAll(resultList);
            commitTx();
            return memberships;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            rollbackTx();
            throw new StorageException(t);
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUserMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId) throws StorageException {
        Set<RoleMembershipBean> memberships = new HashSet<>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(
                    builder.equal(from.get("userId"), userId), //$NON-NLS-1$
                    builder.equal(from.get("organizationId"), organizationId) ); //$NON-NLS-1$
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            memberships.addAll(resultList);
            commitTx();
            return memberships;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            rollbackTx();
            throw new StorageException(t);
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getOrgMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException {
        Set<RoleMembershipBean> memberships = new HashSet<>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(builder.equal(from.get("organizationId"), organizationId)); //$NON-NLS-1$
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            memberships.addAll(resultList);
            commitTx();
            return memberships;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            rollbackTx();
            throw new StorageException(t);
        }
    }
    
    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getPermissions(java.lang.String)
     */
    @Override
    public Set<PermissionBean> getPermissions(String userId) throws StorageException {
        Set<PermissionBean> permissions = new HashSet<>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(builder.equal(from.get("userId"), userId)); //$NON-NLS-1$
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            typedQuery.setMaxResults(500);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            for (RoleMembershipBean membership : resultList) {
                RoleBean role = getRoleInternal(membership.getRoleId());
                String qualifier = membership.getOrganizationId();
                for (PermissionType permission : role.getPermissions()) {
                    PermissionBean p = new PermissionBean();
                    p.setName(permission);
                    p.setOrganizationId(qualifier);
                    permissions.add(p);
                }
            }
            commitTx();
            return permissions;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            rollbackTx();
            throw new StorageException(t);
        }
    }

    /**
     * @param roleId
     * @return a role by id
     * @throws StorageException
     * @throws DoesNotExistException
     */
    protected RoleBean getRoleInternal(String roleId) throws StorageException {
        return super.get(roleId, RoleBean.class);
    }
    
}
