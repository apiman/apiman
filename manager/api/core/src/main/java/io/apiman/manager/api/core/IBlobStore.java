package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.download.BlobDto;

import com.google.common.io.FileBackedOutputStream;
import org.jetbrains.annotations.NotNull;

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
public interface IBlobStore {

    /**
     * Store blob, a UID is returned that the caller should store to retrieve the file later.
     *
     * <p>Use this method if the file is large as it will spill over onto disk when it exceeds a reasonable threshold.
     *
     * <p>Any upload that is hash, name, and mime identical will be deduplicated.
     *
     * @param name descriptive name (not used for lookups)
     * @param mimeType mime type of the file (useful when serving externally)
     * @param blob the file to store
     * @return the UID of the file. Use it to look up the file.
     */
    String storeBlob(@NotNull String name, @NotNull String mimeType, @NotNull FileBackedOutputStream blob);

    /**
     * Store blob, a UID is returned that the caller should store to retrieve the file later.
     *
     * <p>Use this method if the file is smaller.
     *
     * <p>Any upload that is hash, name, and mime identical will be deduplicated.
     *
     * @param name descriptive name (not used for lookups)
     * @param mimeType mime type of the file (useful when serving externally)
     * @param blob the file to store
     * @return the UID of the file. Use it to look up the file.
     */
    String storeBlob(@NotNull String name, @NotNull String mimeType, byte[] blob);

    /**
     * Get a blob by its ID (as provided when stored).
     *
     * @param uid the file's UID
     * @return the blob with metadata.
     */
    BlobDto getBlob(@NotNull String uid);

    /**
     * Remove a file by its UID.
     *
     * <p>As blobs are deduplicated, the file may still exist after deletion because the reference counter has not
     * been decremented to zero (which triggers actual DB deletion).
     *
     * @param uid the file's UID
     * @return this
     */
    IBlobStore remove(@NotNull String uid);
}
