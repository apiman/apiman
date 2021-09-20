package io.apiman.manager.api.jpa.blobstore;

import java.sql.Blob;
import java.time.OffsetDateTime;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A blob entity.
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
    private String name;

    @Column(name = "mime_type", nullable = false)
    @NotBlank
    private String mimeType;

    // @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(name = "created_on", nullable = false)
    @NotNull
    private OffsetDateTime createdOn;

    // @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    @Column(name = "modified_on", nullable = false)
    @NotNull
    private OffsetDateTime modifiedOn;

    @Lob
    @Column(name = "mrblobby", nullable = false)
    @NotNull
    private Blob blob;

    @Column(name = "hash", nullable = false)
    @NotNull
    private Long hash;

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
