package io.apiman.manager.api.beans.download;

import java.time.OffsetDateTime;

/**
 * Blob summary.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class BlobRef {
    private String id;
    private String name;
    private String mimeType;
    private OffsetDateTime createdOn;
    private OffsetDateTime modifiedOn;
    private long hash;

    public BlobRef() {
    }

    public String getId() {
        return id;
    }

    public BlobRef setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public BlobRef setName(String name) {
        this.name = name;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public BlobRef setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public BlobRef setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public long getHash() {
        return hash;
    }

    public BlobRef setHash(long hash) {
        this.hash = hash;
        return this;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public BlobRef setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
        return this;
    }
}
