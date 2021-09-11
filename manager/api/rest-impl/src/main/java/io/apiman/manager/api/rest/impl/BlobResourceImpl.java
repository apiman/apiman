package io.apiman.manager.api.rest.impl;

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
import org.apache.commons.io.FilenameUtils;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Path("blobs")
@Api(tags = "Blobs")
public class BlobResourceImpl {

    private IBlobStore blobStore;

    @Inject
    public BlobResourceImpl(IBlobStore blobStore) {
        this.blobStore = blobStore;
    }

    public BlobResourceImpl() {}

    @GET
    @Path("{filename}")
    public Response getBlob(@PathParam("filename") String filename) {
        String uid = FilenameUtils.removeExtension(filename);
        BlobDto blob = blobStore.getBlob(uid);
        if (blob == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            try {
                InputStream bis = blob.getBlob().asByteSource().openBufferedStream();
                return Response.accepted()
                               .header("Content-Type", blob.getMimeType())
                               .entity(bis)
                               .build();
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }
    }
}
