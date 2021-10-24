package io.apiman.manager.api.beans.download;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ExportedBlobDto extends BlobDto {
    private int refCount;

    public ExportedBlobDto() {}

    public int getRefCount() {
        return refCount;
    }

    public ExportedBlobDto setRefCount(int refCount) {
        this.refCount = refCount;
        return this;
    }
}
