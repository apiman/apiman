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

    @Column(name = "file_ext", nullable = false)
    @NotBlank
    private String fileExt;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(name = "modified_on", nullable = false)
    @NotNull
    private OffsetDateTime createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    @Column(name = "modified_on", nullable = false)
    @NotNull
    private OffsetDateTime modifiedOn;

    @Lob
    @NotNull
    @Column(name = "mrblobby", nullable = false)
    private Blob blob;

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

    public String getFileExt() {
        return fileExt;
    }

    public BlobEntity setFileExt(String fileExt) {
        this.fileExt = fileExt;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", BlobEntity.class.getSimpleName() + "[", "]")
             .add("id='" + id + "'")
             .add("name='" + name + "'")
             .add("fileExt='" + fileExt + "'")
             .add("createdOn=" + createdOn)
             .add("modifiedOn=" + modifiedOn)
             .add("blob=<binary>")
             .toString();
    }
}
