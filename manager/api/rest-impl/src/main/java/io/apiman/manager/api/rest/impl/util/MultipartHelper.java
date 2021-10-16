package io.apiman.manager.api.rest.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import javax.ws.rs.core.MediaType;

import com.google.common.io.FileBackedOutputStream;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jetbrains.annotations.NotNull;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class MultipartHelper {

    private static final int COPY_BUFF_SIZE_BYTES = 8192;
    private static final int FBOS_THRESHOLD_BYTES = 4 * 1024 * 1024;

    public MultipartHelper() {
    }

    public static MultipartUploadHolder getRequiredImage(@NotNull MultipartFormDataInput multipartInput,
         @NotNull String partName, long sizeLimitBytes)
         throws IOException {
        InputPart imgPart =  MultipartHelper.getRequiredPart(multipartInput, partName, new MediaType("image", "*"));
        MediaType imgMediaType = imgPart.getMediaType();
        ContentDisposition imgContentDisposition = MultipartHelper.getContentDisposition(imgPart);
        String filename = Optional.ofNullable(imgContentDisposition.getFilename()).orElse("");
        FileBackedOutputStream fbos = MultipartHelper.transferToFbos(imgPart, sizeLimitBytes); // TODO(msavy): add threshold to config
        return new MultipartUploadHolder(filename, fbos, imgMediaType);
    }

    public static InputPart getRequiredPart(@NotNull MultipartFormDataInput formDataInput, @NotNull String partName, @NotNull MediaType mediaType) {
        Map<String, List<InputPart>> formData = formDataInput.getFormDataMap();
        return Optional.ofNullable(formData.get(partName))
                .filter(parts -> !parts.isEmpty())
                .map(parts -> parts.get(0))
                .filter(part -> part.getMediaType().isCompatible(mediaType))
                .orElseThrow(() -> new IllegalArgumentException("Must provide '" + partName + "' with Content-Type: " + mediaType));
    }

    public static ContentDisposition getContentDisposition(@NotNull InputPart part) {
        return Optional.ofNullable(part.getHeaders().getFirst("Content-Disposition"))
                       .map(ContentDisposition::parse)
                       .orElseThrow(() -> new IllegalArgumentException("Must have Content-Disposition header for part " + part));
    }

    public static FileBackedOutputStream transferToFbos(@NotNull InputPart part, long sizeLimitBytes)
         throws IOException {
        var fbos = new FileBackedOutputStream(FBOS_THRESHOLD_BYTES);
        transferData(part.getBody(InputStream.class, null), fbos, sizeLimitBytes);
        return fbos;
    }

    private static void transferData(InputStream is, OutputStream os, long sizeLimitBytes) {
        try (is) {
            byte[] buff = new byte[COPY_BUFF_SIZE_BYTES];
            long bytesTransferred = 0;
            int bytesRead;
            while ((bytesRead = is.read(buff, 0, COPY_BUFF_SIZE_BYTES)) >= 0) {
                os.write(buff, 0, bytesRead);
                bytesTransferred += bytesRead;
                if (bytesTransferred > sizeLimitBytes) {
                    RestArgumentVerifier.checkSizeMax(bytesTransferred, sizeLimitBytes, "Provided file is larger than the limit of " + sizeLimitBytes + "b");
                }
            }
            // If blob is very small, may be an error or DOS/spam
            RestArgumentVerifier.checkArgument(bytesTransferred < 10, "Provided file too small");
        } catch (IOException ioe) {
            IOUtils.closeQuietly(os);
            throw new UncheckedIOException(ioe);
        }
    }

    public static final class MultipartUploadHolder {
        private final String filename;
        private final FileBackedOutputStream fileBackedOutputStream;
        private final MediaType mediaType;

        public MultipartUploadHolder(final String filename, final FileBackedOutputStream fileBackedOutputStream,
             final MediaType mediaType) {
            this.filename = filename;
            this.fileBackedOutputStream = fileBackedOutputStream;
            this.mediaType = mediaType;
        }

        public String getFilename() {
            return filename;
        }

        public FileBackedOutputStream getFileBackedOutputStream() {
            return fileBackedOutputStream;
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", MultipartUploadHolder.class.getSimpleName() + "[", "]")
                 .add("filename='" + filename + "'")
                 .add("fileBackedOutputStream=" + fileBackedOutputStream)
                 .add("mediaType=" + mediaType)
                 .toString();
        }
    }
}
