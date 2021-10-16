package io.apiman.manager.api.rest.interceptors;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.download.BlobReference;
import io.apiman.manager.api.rest.IBlobResource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.reflections.ReflectionUtils;

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
 *   String fileRef = "a2321-4124-foo.jpeg"; // <-- Must be a plain blobstore UID
 *
 *   ... Interceptor rewrites reference to use JAX-RS download endpoint.
 *
 *   @BlobReference
 *   String fileRef = "/apiman/blobs/a2321-4124-foo.jpeg"; // <-- Resolved URL
 * }
 * </pre>
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Provider
@ServerInterceptor
public class BlobResourceInterceptorProvider implements WriterInterceptor {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(BlobResourceInterceptorProvider.class);

    public BlobResourceInterceptorProvider() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        try {
            rewrite(context);
            context.proceed();
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    private void rewrite(WriterInterceptorContext context) throws IllegalAccessException {
        Object rootEntity = context.getEntity();
        List<FieldAndEntity> blobRefs = searchRecursively(rootEntity);
        
        for (FieldAndEntity fieldAndEntity : blobRefs) {
            Object entity = fieldAndEntity.entity;
            Field blobRef = fieldAndEntity.field;
            String existingValue = (String) blobRef.get(entity);
            if (existingValue == null || existingValue.isBlank()) {
                LOGGER.debug("Null or blank @BlobRef {0}@{1}", blobRef.getName(), entity.getClass().getCanonicalName());
            } else {
                String resolvedRef = UriBuilder.fromResource(IBlobResource.class).path(existingValue).build()
                                               .toString();
                LOGGER.debug("Rewriting response POJO field annotated with resolved @BlobReference: {0} -> {1}", existingValue, resolvedRef);
                blobRef.set(entity, resolvedRef);
            }
        }
    }

    /**
     * Chase object graph for all @BlobReference strings.
     * <ul>
     *   <li>Starting with root and until no entries remain: pop entry off stack, this becomes <code>currentNode</code></li>
     *   <li>Iterate through every field in <code>currentNode</code>.</li>
     *   <li>If a field is a <code>String</code> and annotated with <code>BlobReference</code>, it is captured.</li>
     *   <li>Any non-String type that begins with <code>io.apiman</code> is pushed onto the stack. All other types are ignored.</li>
     * </ul>
     * TODO(msavy): Add cache or use Jandex to avoid repeated expensive reflection and iteration.
     * Simple!
     */
    @SuppressWarnings("unchecked")
    private List<FieldAndEntity> searchRecursively(Object root) throws IllegalAccessException {
        List<FieldAndEntity> results = new ArrayList<>();
        Deque<Object> nodesToSearch = new ArrayDeque<>();
        nodesToSearch.push(root);
        while (!nodesToSearch.isEmpty()) {
            Object currentNode = nodesToSearch.pop();
            for (Field f : ReflectionUtils.getAllFields(currentNode.getClass())) {
                f.setAccessible(true);
                if (f.isAnnotationPresent(BlobReference.class) && f.getType().equals(String.class)) {
                    results.add(new FieldAndEntity(f, currentNode));
                } else {
                    if (!f.getType().isEnum() && f.getType().getCanonicalName().startsWith("io.apiman")) {
                        Object value = f.get(currentNode);
                        if (value != null) {
                            nodesToSearch.push(value);
                        }
                    }
                }
            }
        }
        return results;
    }

    private static final class FieldAndEntity {
        public Field field;
        public Object entity;

        public FieldAndEntity(Field field, Object entity) {
            this.field = field;
            this.entity = entity;
        }
    }
}

