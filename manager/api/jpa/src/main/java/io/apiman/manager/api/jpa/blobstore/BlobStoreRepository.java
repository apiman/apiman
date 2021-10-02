package io.apiman.manager.api.jpa.blobstore;

import io.apiman.common.util.Preconditions;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.jpa.AbstractJpaStorage;

import java.util.Objects;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;

import org.jetbrains.annotations.NotNull;

/**
 * Store, retrieve, and delete blobs.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped // TODO -- should be @Alternative?
public class BlobStoreRepository extends AbstractJpaStorage {

    public BlobStoreRepository() {
    }

    /**
     * Create a blob. Caller <em>must</em> set ID themselves.
     * UUID recommended as a component of ID to avoid collisions.
     */
    public void create(@NotNull BlobEntity bean) throws StorageException {
        Preconditions.requireNonBlank(bean.getId(),
             "Caller must set ID for blob before storing to ensure they are able to look it up");
        super.create(bean);
    }

    /**
     * Get a blob by its ID (nb: not name, its ID set when stored).
     */
    public BlobEntity getById(@NotNull String uid) throws StorageException {
        Preconditions.requireNonBlank(uid, "uid must be non-blank");
        return super.get(uid, BlobEntity.class);
    }

    /**
     * Delete by provided ID (nb: not name, its ID set when stored).
     *
     * <p>Does not truly delete the file if there are other (i.e. deduplicated) references to this blob. In this case
     * the reference counter is decremented.
     */
    public void deleteById(@NotNull String uid) throws StorageException {
        Preconditions.requireNonBlank(uid, "uid must be non-blank");
        EntityManager em = super.getActiveEntityManager();
        em.createQuery("DELETE "
                            + "FROM BlobEntity b "
                            + "WHERE b.references <= 1 "
                            + "AND b.id = :uid")
          .setParameter("uid", uid)
          .executeUpdate();

        em.createQuery("UPDATE BlobEntity b "
                            + "SET b.references = b.references-1"
                            + "WHERE b.id = :uid")
          .setParameter("uid", uid)
          .executeUpdate();
    }

    public BlobEntity getByHash(@NotNull String hash) {
        Preconditions.requireNonBlank(hash, "hash must be non-blank");
        return getActiveEntityManager()
             .createQuery("SELECT b "
                               + "FROM BlobEntity b "
                               + "WHERE b.hash = :hashCode", BlobEntity.class)
             .setParameter("hashCode", hash)
             .getSingleResult();
    }

    public BlobEntity getByNaturalId(@NotNull String name, @NotNull String mimeType, @NotNull Long hash) {
        Preconditions.requireNonBlank(name, "name must be non-blank");
        Preconditions.requireNonBlank(mimeType, "mimeType must be non-blank");
        Objects.requireNonNull(hash, "hash must be non-null");
        return getSession().byNaturalId(BlobEntity.class)
                           .using("hash", hash)
                           .using("name", name)
                           .using("mimeType", mimeType)
                           .load();
    }
}