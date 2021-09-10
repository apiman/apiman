package io.apiman.manager.api.beans.download;

import java.time.OffsetDateTime;
import java.util.StringJoiner;

import com.google.common.io.FileBackedOutputStream;

/**
 * Blob store representation
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class BlobDto {
    private String id;
    private String name;
    private String mimeType;
    private OffsetDateTime createdOn;
    private OffsetDateTime modifiedOn;
    private FileBackedOutputStream blob;

    public String getId() {
        return id;
    }

    public BlobDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public BlobDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public BlobDto setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public BlobDto setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public OffsetDateTime getModifiedOn() {
        return modifiedOn;
    }

    public BlobDto setModifiedOn(OffsetDateTime modifiedOn) {
        this.modifiedOn = modifiedOn;
        return this;
    }

    public FileBackedOutputStream getBlob() {
        return blob;
    }

    public BlobDto setBlob(FileBackedOutputStream blob) {
        this.blob = blob;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BlobDto.class.getSimpleName() + "[", "]")
             .add("id='" + id + "'")
             .add("name='" + name + "'")
             .add("mimeType='" + mimeType + "'")
             .add("createdOn=" + createdOn)
             .add("modifiedOn=" + modifiedOn)
             .add("blob=<binary>")
             .toString();
    }
}
