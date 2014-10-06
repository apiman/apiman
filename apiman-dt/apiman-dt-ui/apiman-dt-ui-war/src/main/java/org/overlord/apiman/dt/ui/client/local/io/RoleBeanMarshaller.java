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
package org.overlord.apiman.dt.ui.client.local.io;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJArray;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.AbstractNullableMarshaller;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;

/**
 * A custom client-side marshaller for the {@link RoleBean} class.
 * 
 * @author eric.wittmann@redhat.com
 */
@ClientMarshaller(RoleBean.class)
public class RoleBeanMarshaller extends AbstractNullableMarshaller<RoleBean> {

    private RoleBean[] EMPTY_ARRAY = new RoleBean[0];
    private Marshaller<String> java_lang_String = null;
    private Marshaller<Date> java_util_Date = null;
    private Marshaller<Boolean> java_lang_Boolean = null;

    /**
     * Constructor.
     */
    public RoleBeanMarshaller() {
    }

    /**
     * @see org.jboss.errai.marshalling.client.api.Marshaller#getEmptyArray()
     */
    @Override
    public RoleBean[] getEmptyArray() {
        return EMPTY_ARRAY;
    }

    /**
     * @see org.jboss.errai.marshalling.client.marshallers.AbstractNullableMarshaller#doNotNullDemarshall(org.jboss.errai.marshalling.client.api.json.EJValue, org.jboss.errai.marshalling.client.api.MarshallingSession)
     */
    @Override
    public RoleBean doNotNullDemarshall(EJValue jsonValue, MarshallingSession session) {
        EJObject obj = jsonValue.isObject();
        if (obj == null) {
            return null;
        }
        lazyInit();
        String objId = obj.get("^ObjectID").isString().stringValue(); //$NON-NLS-1$
        if (session.hasObject(objId)) {
            return session.getObject(RoleBean.class, objId);
        }
        RoleBean entity = new RoleBean();
        session.recordObject(objId, entity);
        if ((obj.containsKey("id")) && (!obj.get("id").isNull())) { //$NON-NLS-1$ //$NON-NLS-2$
            entity.setId(java_lang_String.demarshall(obj.get("id"), session)); //$NON-NLS-1$
        }
        if ((obj.containsKey("name")) && (!obj.get("name").isNull())) { //$NON-NLS-1$ //$NON-NLS-2$
            entity.setName(java_lang_String.demarshall(obj.get("name"), session)); //$NON-NLS-1$
        }
        if ((obj.containsKey("description")) && (!obj.get("description").isNull())) { //$NON-NLS-1$ //$NON-NLS-2$
            entity.setDescription(java_lang_String.demarshall(obj.get("description"), session)); //$NON-NLS-1$
        }
        if ((obj.containsKey("createdBy")) && (!obj.get("createdBy").isNull())) { //$NON-NLS-1$ //$NON-NLS-2$
            entity.setCreatedBy(java_lang_String.demarshall(obj.get("createdBy"), session)); //$NON-NLS-1$
        }
        if ((obj.containsKey("createdOn")) && (!obj.get("createdOn").isNull())) { //$NON-NLS-1$ //$NON-NLS-2$
            entity.setCreatedOn(java_util_Date.demarshall(obj.get("createdOn"), session)); //$NON-NLS-1$
        }
        if ((obj.containsKey("autoGrant")) && (!obj.get("autoGrant").isNull())) { //$NON-NLS-1$ //$NON-NLS-2$
            entity.setAutoGrant(java_lang_Boolean.demarshall(obj.get("autoGrant"), session)); //$NON-NLS-1$
        }
        if (obj.containsKey("permissions")) { //$NON-NLS-1$
            Set<PermissionType> permissions = new HashSet<PermissionType>();
            EJValue permObj = obj.get("permissions"); //$NON-NLS-1$
            if (!permObj.isNull()) {
                EJArray array = permObj.isArray();
                if (array == null) {
                    array = permObj.isObject().get("^Value").isArray(); //$NON-NLS-1$
                }
                
                for (int i = 0; i < array.size(); i++) {
                    final EJValue elem = array.get(i);
                    if (!elem.isNull()) {
                        String permStr = java_lang_String.demarshall(elem, session);
                        if (permStr != null) {
                            permissions.add(PermissionType.valueOf(permStr));
                        }
                    }
                }
            }
            entity.setPermissions(permissions);
        }
        return entity;
    }

    /**
     * @see org.jboss.errai.marshalling.client.marshallers.AbstractNullableMarshaller#doNotNullMarshall(java.lang.Object, org.jboss.errai.marshalling.client.api.MarshallingSession)
     */
    @Override
    public String doNotNullMarshall(RoleBean roleBean, MarshallingSession session) {
        if (roleBean == null) {
            return "null"; //$NON-NLS-1$
        }
        lazyInit();
        final StringBuilder json = new StringBuilder("{"); //$NON-NLS-1$
        json.append("\"id\":").append(java_lang_String.marshall(roleBean.getId(), session)).append(",") //$NON-NLS-1$ //$NON-NLS-2$
                .append("\"name\":").append(java_lang_String.marshall(roleBean.getName(), session)).append(",") //$NON-NLS-1$ //$NON-NLS-2$
                .append("\"description\":").append(java_lang_String.marshall(roleBean.getDescription(), session)).append(",") //$NON-NLS-1$ //$NON-NLS-2$
                .append("\"autoGrant\":").append(java_lang_Boolean.marshall(roleBean.getAutoGrant(), session)).append(",") //$NON-NLS-1$ //$NON-NLS-2$
                .append("\"permissions\":["); //$NON-NLS-1$
        if (roleBean.getPermissions() != null) {
            boolean first = true;
            for (PermissionType permission : roleBean.getPermissions()) {
                if (!first) {
                    json.append(","); //$NON-NLS-1$
                }
                json.append(java_lang_String.marshall(permission.name(), session));
                first = false;
            }
        }
        return json.append("]}").toString(); //$NON-NLS-1$
    }

    /**
     * Init the dependent marshallers.
     */
    protected void lazyInit() {
        if (java_lang_String == null) {
            java_lang_String = Marshalling.getMarshaller(String.class);
        }
        if (java_util_Date == null) {
            java_util_Date = Marshalling.getMarshaller(Date.class);
        }
        if (java_lang_Boolean == null) {
            java_lang_Boolean = Marshalling.getMarshaller(Boolean.class);
        }
    }

}
