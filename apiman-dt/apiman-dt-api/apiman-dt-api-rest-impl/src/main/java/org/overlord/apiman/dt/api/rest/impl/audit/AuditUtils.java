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
package org.overlord.apiman.dt.api.rest.impl.audit;

import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;
import org.overlord.apiman.dt.api.beans.audit.AuditEntityType;
import org.overlord.apiman.dt.api.beans.audit.AuditEntryBean;
import org.overlord.apiman.dt.api.beans.audit.AuditEntryType;
import org.overlord.apiman.dt.api.beans.audit.data.EntityUpdatedData;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.security.ISecurityContext;

/**
 * Contains a number of methods useful to create and manage audit entries for
 * actions taken by users in the management layer REST API.
 * 
 * @author eric.wittmann@redhat.com
 */
public class AuditUtils {
    
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Returns true only if the value changed.
     * @param before
     * @param after
     */
    public static boolean valueChanged(String before, String after) {
        if (before == null && after != null) {
            return true;
        }
        if (before != null && after == null) {
            return true;
        }
        return !before.trim().equals(after.trim());
    }

    /**
     * Creates an {@link AuditEntryBean} for the 'organization created' event.
     * @param bean
     * @param securityContext
     */
    public static AuditEntryBean organizationCreated(OrganizationBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getId(), AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'organization updated' event.
     * @param bean
     * @param auditData
     * @param securityContext
     */
    public static AuditEntryBean organizationUpdated(OrganizationBean bean, EntityUpdatedData auditData,
            ISecurityContext securityContext) {
        if (auditData.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getId(), AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        try {
            entry.setData(mapper.writeValueAsString(auditData));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entry;
    }

    /**
     * Creates an audit entry.
     * @param orgId
     * @param type
     * @param securityContext
     */
    private static AuditEntryBean newEntry(String orgId, AuditEntityType type, ISecurityContext securityContext) {
        AuditEntryBean entry = new AuditEntryBean();
        entry.setOrganizationId(orgId);
        entry.setEntityType(type);
        entry.setWhen(new Date());
        entry.setWho(securityContext.getCurrentUser());
        return entry;
    }

}
