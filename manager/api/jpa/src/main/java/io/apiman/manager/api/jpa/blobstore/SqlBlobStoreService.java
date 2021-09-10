package io.apiman.manager.api.jpa.blobstore;

import io.apiman.manager.api.beans.download.BlobDto;
import io.apiman.manager.api.core.IBlobStore;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.io.FileBackedOutputStream;
import org.hibernate.engine.jdbc.BlobProxy;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped // TODO make @Alternative
public class SqlBlobStoreService implements IBlobStore<SqlBlobStoreService> {
    private BlobStoreRepository blobStoreRepository;
    private BlobMapper mapper;

    @Inject
    public SqlBlobStoreService(BlobStoreRepository blobStoreRepository, BlobMapper mapper) {
        this.blobStoreRepository = blobStoreRepository;
        this.mapper = mapper;
    }

    public SqlBlobStoreService() {}

    @Override
    public String storeBlob(String name, String mimeType, FileBackedOutputStream fbos) {
        String resourceId = UUID.randomUUID().toString();
        try {
        BlobEntity blobEntity = new BlobEntity()
             .setId(resourceId)
             .setName(name)
             .setMimeType(mimeType)
             .setBlob(BlobProxy.generateProxy(fbos.asByteSource().openStream(), fbos.asByteSource().size()));
            blobStoreRepository.create(blobEntity);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        return resourceId;
    }

    @Override
    public String storeBlob(String name, String mimeType, byte[] bytes) {
        String resourceId = UUID.randomUUID().toString();
        BlobEntity blobEntity = new BlobEntity()
             .setId(resourceId)
             .setName(name)
             .setMimeType(mimeType)
             .setBlob(BlobProxy.generateProxy(bytes));
        try {
            blobStoreRepository.create(blobEntity);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
        return resourceId;
    }

    @Override
    public BlobDto getBlob(String id) {
        try {
            return mapper.toDto(blobStoreRepository.getById(id));
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SqlBlobStoreService remove(String fileUuid) {
        try {
            blobStoreRepository.deleteById(fileUuid);
            return this;
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }
}
