package io.apiman.manager.api.rest.interceptors;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.download.BlobReference;
import io.apiman.manager.api.rest.IDownloadResource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.reflections.Reflections;

/**
 * Rewrites a blob's UID into a URL that can be resolved by the browser.
 *
 * <p>This approach avoids hard-coding any absolute references, as it only runs just before JAX-RS serializes the
 * object.
 *
 * <p>For example:
 * <pre>
 * {@code
 *   @BlobReference
 *   String fileRef = "foo.jpeg"; // <-- Must be a plain blobstore UID
 *
 *   ... Interceptor rewrites reference to use JAX-RS download endpoint.
 *
 *   @BlobReference
 *   String fileRef = "/downloads/assets/foo.jpeg";
 * }
 * </pre>
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Provider
public class BlobResourceInterceptorProvider implements WriterInterceptor {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(BlobResourceInterceptorProvider.class);

    // @Inject
    // public BlobResourceInterceptorProvider() {
    // }

    public BlobResourceInterceptorProvider() {
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        try {
            rewrite(context);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void rewrite(WriterInterceptorContext context) throws IllegalAccessException {
        Object entity = context.getEntity();
        List<Field> blobRefs = new Reflections(entity.getClass())
             .getFieldsAnnotatedWith(BlobReference.class)
             .stream()
             .filter(f -> f.getType().equals(String.class))
             .collect(Collectors.toList());

        for (Field blobRef : blobRefs) {
            String existingValue = (String) blobRef.get(entity);
            String resolvedRef = UriBuilder.fromResource(IDownloadResource.class).path(existingValue).build()
                                           .toString();
            LOGGER.debug("Rewriting response POJO field annotated with resolved @BlobReference: {0} -> {1}",
                 existingValue, resolvedRef);
            blobRef.set(entity, resolvedRef);
        }
    }
}
