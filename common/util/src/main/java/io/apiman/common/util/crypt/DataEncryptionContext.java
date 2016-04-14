/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.common.util.crypt;

/**
 * Some additional context information available to implementations of the {@link IDataEncrypter}
 * interface.  This context can be used to encrypt data differently depending on, for example, the
 * organization ID.  Note that it is possible for any given piece of this context to be null,
 * depending on the data being encrypted.
 * @author eric.wittmann@gmail.com
 */
public class DataEncryptionContext {

    private final String organizationId;
    private final String entityId;
    private final String entityVersion;
    private final EntityType entityType;

    /**
     * Constructor.
     */
    public DataEncryptionContext() {
        this(null, null, null, null);
    }

    /**
     * Constructor.
     * @param organizationId
     */
    public DataEncryptionContext(String organizationId) {
        this(organizationId, null, null, null);
    }

    /**
     * Constructor.
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param entityType
     */
    public DataEncryptionContext(String organizationId, String entityId, String entityVersion,
            EntityType entityType) {
        this.organizationId = organizationId;
        this.entityId = entityId;
        this.entityVersion = entityVersion;
        this.entityType = entityType;
    }

    /**
     * @return the organizationId
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * @return the entityId
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * @return the entityVersion
     */
    public String getEntityVersion() {
        return entityVersion;
    }

    /**
     * @return the entityType
     */
    public EntityType getEntityType() {
        return entityType;
    }

    public static enum EntityType {
        Organization, ClientApp, Api, Plan
    }
}
