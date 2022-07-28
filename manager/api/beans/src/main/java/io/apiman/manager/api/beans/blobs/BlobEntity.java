/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.beans.blobs;

import java.sql.Blob;
import java.time.OffsetDateTime;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A blob storage entity.
 *
 * See IBlobStore for more detail.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Table(name = "blob_store")
@Entity
public class BlobEntity {
    @Id
    @Column(name = "id", unique = true)
    @NotBlank
    private String id;

    @Column(name = "name", nullable = false)
    @NotBlank
    @NaturalId
    private String name;

    @Column(name = "mime_type", nullable = false)
    @NotBlank
    @NaturalId
    private String mimeType;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private OffsetDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Lob
    @Column(name = "mrblobby", nullable = false, length = Integer.MAX_VALUE)
    @NotNull
    private Blob blob;

    @Column(name = "hash", nullable = false)
    @NotNull
    @NaturalId
    private Long hash;

    @Column(name = "ref_count", nullable = false)
    private int refCount = -1;

    public BlobEntity() {
    }

    public String getId() {
        return id;
    }

    public BlobEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public BlobEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public BlobEntity setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public BlobEntity setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public BlobEntity setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
        return this;
    }

    public Blob getBlob() {
        return blob;
    }

    public BlobEntity setBlob(Blob blob) {
        this.blob = blob;
        return this;
    }

    public Long getHash() {
        return hash;
    }

    public BlobEntity setHash(Long hash) {
        this.hash = hash;
        return this;
    }

    public int getRefCount() {
        return refCount;
    }

    public BlobEntity setRefCount(int references) {
        this.refCount = references;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BlobEntity.class.getSimpleName() + "[", "]")
             .add("id='" + id + "'")
             .add("name='" + name + "'")
             .add("mimeType='" + mimeType + "'")
             .add("createdOn=" + createdOn)
             .add("modifiedOn=" + modifiedOn)
             .add("blob=<binary>")
             .add("hash=" + hash)
             .toString();
    }
}
