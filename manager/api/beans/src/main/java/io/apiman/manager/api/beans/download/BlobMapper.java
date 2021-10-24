package io.apiman.manager.api.beans.download;

import io.apiman.manager.api.beans.blobs.BlobEntity;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

import com.google.common.io.ByteStreams;
import com.google.common.io.FileBackedOutputStream;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Map a blob JPA entity blob DTO
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Mapper
public interface BlobMapper {
    int THRESHOLD_BYTES = 4 * 1024 * 1024;

    BlobMapper INSTANCE = Mappers.getMapper(BlobMapper.class);

    BlobDto toDto(BlobEntity blobEntity);

    ExportedBlobDto toExportedDto(BlobEntity blobEntity);

    default FileBackedOutputStream toFbos(Blob blob) throws SQLException, IOException {
        FileBackedOutputStream fbos = new FileBackedOutputStream(THRESHOLD_BYTES);
        ByteStreams.copy(blob.getBinaryStream(), fbos);
        return fbos;
    }
}
