package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.blobs.BlobEntity;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.Iterator;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface IBlobStoreRepository {

    /**
     * Intended only for use by import-export
     */
    Iterator<BlobEntity> getAll() throws StorageException;
}
