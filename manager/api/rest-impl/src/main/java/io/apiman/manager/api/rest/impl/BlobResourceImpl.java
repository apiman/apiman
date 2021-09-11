package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.download.BlobDto;
import io.apiman.manager.api.core.IBlobStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import org.jboss.resteasy.annotations.cache.Cache;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("blobs")
@Api(tags = "Blobs")
public class BlobResourceImpl {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(BlobResourceImpl.class);
    private IBlobStore blobStore;

    @Inject
    public BlobResourceImpl(IBlobStore blobStore) {
        this.blobStore = blobStore;
    }

    public BlobResourceImpl() {}

    @GET
    @Path("{uid}")
    public Response getBlob(@PathParam("uid") String uid) {
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
}
