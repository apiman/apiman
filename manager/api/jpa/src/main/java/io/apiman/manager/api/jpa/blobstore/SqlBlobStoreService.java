package io.apiman.manager.api.jpa.blobstore;

import io.apiman.manager.api.beans.download.BlobDto;
import io.apiman.manager.api.core.IBlobStore;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.google.common.io.FileBackedOutputStream;
import net.jpountz.xxhash.StreamingXXHash64;
import net.jpountz.xxhash.XXHashFactory;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.jdbc.BlobProxy;
import org.jetbrains.annotations.NotNull;

/**
 * Stores blob data in a SQL database.
 *
 * This implementation returns an OID in the format: <samp>44cf-a164-607dd7fcd820/foo.jpeg</samp>
 *
 * This lets us preserve the filename and extension (if one is provided), whilst avoiding name collisions.
 * Will make it easier to identify the type of file in certain situations (e.g. where MIME not available).
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped // TODO make @Alternative
public class SqlBlobStoreService implements IBlobStore {
    private BlobStoreRepository blobStoreRepository;
    private BlobMapper mapper;

    @Inject
    public SqlBlobStoreService(BlobStoreRepository blobStoreRepository, BlobMapper mapper) {
        this.blobStoreRepository = blobStoreRepository;
        this.mapper = mapper;
    }

    public SqlBlobStoreService() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public String storeBlob(@NotNull String name, @NotNull String mimeType, @NotNull FileBackedOutputStream fbos) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Blob name must not be blank");
        String resourceId = calculateOid(name);
        try {
        BlobEntity blobEntity = new BlobEntity()
             .setId(resourceId)
             .setName(name)
             .setMimeType(mimeType)
             .setBlob(BlobProxy.generateProxy(fbos.asByteSource().openStream(), fbos.asByteSource().size()))
             .setHash(hashBlob(fbos));
            blobStoreRepository.create(blobEntity);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        return resourceId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String storeBlob(@NotNull String name, @NotNull String mimeType, byte[] bytes) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Blob name must not be blank");
        String resourceId = calculateOid(name);
        BlobEntity blobEntity = new BlobEntity()
             .setId(resourceId)
             .setName(name)
             .setMimeType(mimeType)
             .setBlob(BlobProxy.generateProxy(bytes))
             .setHash(hashBlob(bytes));
        try {
            blobStoreRepository.create(blobEntity);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
        return resourceId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlobDto getBlob(@NotNull String oid) {
        try {
            return mapper.toDto(blobStoreRepository.getById(oid));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SqlBlobStoreService remove(@NotNull String oid) {
        try {
            blobStoreRepository.deleteById(oid);
            return this;
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * OID for 'name' of foo.jpeg will be in format: <samp>44cf-a164-607dd7fcd820/foo.jpeg</samp>.
     */
    private String calculateOid(String name) {
        String safeName = name.replaceAll("[\\P{Print}\\W]+", "_");
        return UUID.randomUUID().toString().substring(14) + "/" + safeName;
    }

    // Doesn't seem to be a standardised way of doing a digest in SQL, unfortunately
    private long hashBlob(byte[] bytes) {
        return XXHashFactory.fastestInstance().hash64().hash(bytes, 0, bytes.length, 0);
    }

    private long hashBlob(FileBackedOutputStream fbos) {
        try {
            StreamingXXHash64 streamingHash = XXHashFactory.fastestInstance().newStreamingHash64(0);
            InputStream fbosStream = fbos.asByteSource().openBufferedStream();
            byte[] hashBuffer = new byte[8192];
            int len;
            while ((len = fbosStream.read(hashBuffer)) != -1) {
                // Need len to ensure we don't read trash from previous iteration if it's less than full array size
                streamingHash.update(hashBuffer, 0, len);
            }
            return streamingHash.getValue();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
