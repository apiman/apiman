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
package io.apiman.manager.api.beans.download;

import java.time.OffsetDateTime;
import java.util.StringJoiner;

import com.google.common.io.FileBackedOutputStream;

/**
 * <p>Blob store representation.</p>
 *
 * <p>Uses {@link FileBackedOutputStream} (FBOS) to store binary blobs. This writes the contents to disk once the buffer size exceeds a size.
 * Consumers of this class should take care to handle this properly</p>
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
    private long hash;

    public BlobDto() {
    }

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

    public long getHash() {
        return hash;
    }

    public BlobDto setHash(long hash) {
        this.hash = hash;
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
                .add("hash=" + hash)
                .toString();
    }
}
