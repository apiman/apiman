package io.apiman.manager.api.jpa.blobstore;

import io.apiman.manager.api.beans.blobs.BlobEntity;
import io.apiman.manager.api.beans.download.BlobDto;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

import com.google.common.io.ByteStreams;
import com.google.common.io.FileBackedOutputStream;
import org.mapstruct.Mapper;

/**
 * Map a blob JPA entity blob DTO
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper(componentModel = "cdi")
public interface BlobMapper {
    int THRESHOLD_BYTES = 4 * 1024 * 1024;

    BlobDto toDto(BlobEntity blobEntity);

    default FileBackedOutputStream toFbos(Blob blob) throws SQLException, IOException {
        FileBackedOutputStream fbos = new FileBackedOutputStream(THRESHOLD_BYTES);
        ByteStreams.copy(blob.getBinaryStream(), fbos);
        return fbos;
    }
}
