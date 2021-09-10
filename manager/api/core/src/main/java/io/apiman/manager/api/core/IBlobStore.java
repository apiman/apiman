package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.download.BlobDto;

import java.sql.Blob;
import java.sql.Clob;

import com.google.common.io.FileBackedOutputStream;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface IBlobStore<SELF extends IBlobStore<SELF>> {

    /**
     * Store blob, a UUID is returned that the caller should store to retrieve the file later.
     *
     * <p>Use this method if the file is large as it will spill over onto disk if the file is large.
     *
     * @param name descriptive name (not used for lookups)
     * @param fileExt
     * @param blob the file to store
     * @return the UUID of the file. Use it to look up the file.
     */
    String storeBlob(String name, String fileExt, FileBackedOutputStream blob);

    /**
     * Store blob, a UUID is returned that the caller should store to retrieve the file later.
     *
     * <p>Use this method if the file is smaller.
     *
     * @param name descriptive name (not used for lookups)
     * @param fileExt
     * @param blob the file to store
     * @return the UUID of the file. Use it to look up the file.
     */
    String storeBlob(String name, String fileExt, byte[] blob);

    /**
     * Get a blob by its ID (as provided when stored).
     *
     * @param id the file's unique ID
     * @return the blob.
     */
    BlobDto getBlob(String id);

    /**
     * Remove a file by its unique ID
     *
     * @param id the file's unique ID
     * @return this
     */
    SELF remove(String id);
}
