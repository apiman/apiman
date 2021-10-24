package io.apiman.manager.api.beans.download;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ExportedBlobDto extends BlobDto {
    private int references;

    public ExportedBlobDto() {}

    public int getReferences() {
        return references;
    }

    public ExportedBlobDto setReferences(int references) {
        this.references = references;
        return this;
    }
}
