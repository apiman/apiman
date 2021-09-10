package io.apiman.manager.api.jpa.blobstore;

import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.jpa.AbstractJpaStorage;

import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped // TODO -- should be @Alternative?
public class BlobStoreRepository extends AbstractJpaStorage {

    public BlobStoreRepository() {
    }

    /**
     * Create a blob. Caller <em>must</em> set ID themselves (UUID recommended).
     */
    public void create(@NotNull BlobEntity bean) throws StorageException {
        if (StringUtils.isBlank(bean.getId())) {
            throw new StorageException("Caller must set ID for blob before storing to ensure they are able to look it up");
        }
        super.create(bean);
    }

    /**
     * Get a blob by its ID (nb: not name, its ID set when stored)
     */
    public BlobEntity getById(@NotNull String id) throws StorageException {
        return super.get(id, BlobEntity.class);
    }

    /**
     * Delete by provided ID (nb: not name, its ID set when stored)
     * @throws StorageException if entity not found
     */
    public void deleteById(@NotNull String id) throws StorageException {
        BlobEntity blob = Optional.ofNullable(getById(id))
                                  .orElseThrow(() -> new StorageException("No blob found for id " + id));
        super.delete(blob);
    }
}
