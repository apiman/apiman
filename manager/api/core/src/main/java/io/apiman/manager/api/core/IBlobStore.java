package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.download.BlobDto;

import com.google.common.io.FileBackedOutputStream;

/**
 * Blob store for storing, well, blobs.
 *
 * <p>When storing a file, provide a name, mime type, and the file's contents. A UID will be returned by the
 * implementation, which is what <em>must</em> be relied upon for looking the file up later. The name provided by the
 * user during storage is purely informational, and multiple files with the same name can exist. Therefore,
 * <strong>users should rely only upon the returned UID for retrieval</strong>.
 *
 * <p>Users are responsible for deleting files they no longer need, via the UID returned when the file was stored.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface IBlobStore<SELF extends IBlobStore<SELF>> {

    /**
     * Store blob, a UUID is returned that the caller should store to retrieve the file later.
     *
     * <p>Use this method if the file is large as it will spill over onto disk if the file is large.
     *
     * @param name descriptive name (not used for lookups)
     * @param mimeType mime type of the file (useful when serving externally)
     * @param blob the file to store
     * @return the UUID of the file. Use it to look up the file.
     */
    String storeBlob(String name, String mimeType, FileBackedOutputStream blob);

    /**
     * Store blob, a UUID is returned that the caller should store to retrieve the file later.
     *
     * <p>Use this method if the file is smaller.
     *
     * @param name descriptive name (not used for lookups)
     * @param mimeType mime type of the file (useful when serving externally)
     * @param blob the file to store
     * @return the UUID of the file. Use it to look up the file.
     */
    String storeBlob(String name, String mimeType, byte[] blob);

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
