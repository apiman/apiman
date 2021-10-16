package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.Preconditions;
import io.apiman.manager.api.beans.download.BlobDto;
import io.apiman.manager.api.beans.download.BlobRef;
import io.apiman.manager.api.core.IBlobStore;
import io.apiman.manager.api.rest.IBlobResource;
import io.apiman.manager.api.rest.impl.util.MultipartHelper;
import io.apiman.manager.api.rest.impl.util.MultipartHelper.MultipartUploadHolder;
import io.apiman.manager.api.security.ISecurityContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Implementation of the Blob REST API.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class BlobResourceImpl implements IBlobResource {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(BlobResourceImpl.class);
    private static final Pattern IMAGE_SUBTYPES_ALLOWED = Pattern.compile("^(apng|png|jpg|jpeg|jfif|pjpeg|pjp|svg|webp|gif|avif)$",  CASE_INSENSITIVE);

    private IBlobStore blobStore;
    private ISecurityContext securityContext;

    @Inject
    public BlobResourceImpl(IBlobStore blobStore, ISecurityContext securityContext) {
        this.blobStore = blobStore;
        this.securityContext = securityContext;
    }

    public BlobResourceImpl() {}

    /**
     * {@inheritDoc}
     *
     * <p>Serves the blob as a stream
     *
     * @param uid file's unique ID.
     * @return the file in response.
     */
    @Override
    public Response getBlob(String uid) {
        BlobDto blob = blobStore.getBlob(uid);
        if (blob == null) {
            LOGGER.trace("Blob requested but not found: {0}", uid);
            return Response.status(Status.NOT_FOUND).build();
        } else {
            try {
                LOGGER.trace("Blob requested: {0}", blob);
                InputStream bis = blob.getBlob().asByteSource().openBufferedStream();
                return Response.ok()
                               .header("Content-Type", blob.getMimeType())
                               .entity(bis)
                               .build();
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }
    }

    @Override
    public Response uploadBlob(MultipartFormDataInput multipartInput) throws IOException {
        Preconditions.checkState(securityContext.getCurrentUser() != null, "Must be logged in!");
        // Try to do image first as it is probably more likely to fail.
        MultipartUploadHolder image = MultipartHelper.getRequiredImage(multipartInput, "image", 1_048_576);
        /*
         We'll ignore the filename and generate our own to make security a bit easier.
         Preconditions.checkArgument(image.getFilename().length() > 254, "Filename too long");
         Preconditions.checkArgument(checkExtension(image.getFilename()), "Must have a recognised image extension");
        */
        Preconditions.checkArgument(checkExtension(image.getMediaType().getSubtype()), "Must have a recognised image content type (" + IMAGE_SUBTYPES_ALLOWED + ")");
        String filename = securityContext.getCurrentUser()  + System.currentTimeMillis() + "." + image.getMediaType().getSubtype();
        // Blob is unreferenced and will be deleted if nobody attaches to it.
        BlobRef blobRef = blobStore.storeBlob(filename, image.getMediaType().toString(), image.getFileBackedOutputStream(), 0);
        // Where the blob can be resolved
        URI location = UriBuilder.fromResource(IBlobResource.class).path(blobRef.getId()).build();
        return Response
                .created(location)
                .entity(blobRef)
                .build();
    }

    private boolean checkExtension(String ext) {
        return IMAGE_SUBTYPES_ALLOWED.matcher(ext).matches();
    }
}
