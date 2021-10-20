package io.apiman.manager.api.rest.interceptors;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.download.BlobReference;
import io.apiman.manager.api.rest.IBlobResource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.ClassUtils;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;

import static org.apache.commons.lang3.reflect.FieldUtils.getAllFields;

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
@Transactional // TODO(msavy): Need this while we still return managed beans at the presentation layer, could consider removing when we go full DTO
public class BlobResourceInterceptorProvider implements WriterInterceptor {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(BlobResourceInterceptorProvider.class);
    // @Inject EntityManagerFactoryAccessor emf;

    public BlobResourceInterceptorProvider() {
    }

    // private EntityManager getEm() {
    //     emf.createEntityManager();
    // }

    /**
     * {@inheritDoc}
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        try {
            // boolean startedOwnTx = false;
            // if (!getEm().getTransaction().isActive()) {
            //     getEm().getTransaction().begin();
            //     startedOwnTx = true;
            // }
            rewrite(context);
            // if (startedOwnTx) {
            //     getEm().getTransaction().commit();
            // }
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
                String resolvedRef = UriBuilder.fromResource(IBlobResource.class)
                        .path(existingValue)
                        .build()
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
        Set<Object> objectsSeen = new HashSet<>();
        Deque<Object> nodesToSearch = new ArrayDeque<>();
        resolveValue(root).forEach(nodesToSearch::push);
        while (!nodesToSearch.isEmpty()) {
            Object currentNode = nodesToSearch.pop();

            // If is collection-like, then don't scan fields. Just get the values and continue.
            if (isCollectionLike(currentNode)) {
                resolveValue(currentNode).forEach(nodesToSearch::add);
                continue;
            }

            // If not collection-like, then we'll inspect the fields and see whether there's anything worth looking at.
            // Must not have seen this before (i.e. simple cycle detection).
            if (!currentNode.getClass().isEnum() && !objectsSeen.contains(currentNode)) {
                for (Field f : getAllFields(currentNode.getClass())) {
                    boolean isAccessible = f.trySetAccessible();
                    if (f.isAnnotationPresent(BlobReference.class) && f.getType().equals(String.class)) {
                        results.add(new FieldAndEntity(f, currentNode));
                    }
                    if (isAccessible && !shouldBeIgnored(f, currentNode)) {
                        Object value = f.get(currentNode);
                        if (value != null) {
                            resolveValue(value).forEach(nodesToSearch::push);
                        }
                    }
                }
                objectsSeen.add(currentNode);

            }
        }
        return results;
    }

    private boolean shouldBeIgnored(Field f, Object currentNode) {
        int modifiers = f.getModifiers();
        return Modifier.isStatic(modifiers)
                || ClassUtils.isPrimitiveOrWrapper(f.getType())
                || f.getType().isAnnotation()
                || Modifier.isAbstract(modifiers)
                || Modifier.isInterface(modifiers)
                || Modifier.isTransient(modifiers)
                || f.isEnumConstant()
                || !f.canAccess(currentNode)
                || f.isAnnotationPresent(JsonIgnore.class);
    }

    private boolean isCollectionLike(Object obj) {
        return obj instanceof Map || obj instanceof Collection;
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> resolveValue(Object obj) {
        if (obj == null) {
            return Stream.empty();
        } else if (obj instanceof Map) { // TODO not sure if we also need to inspect the keys. Seem a tad unlikely.
            return ((Map<Object, Object>) obj).values().stream();
        } else if (obj instanceof Collection) {
            return ((Collection<Object>) obj).stream();
        } else if (obj instanceof Object[]) {
            return Arrays.stream((Object[])obj);
        } else {
            return Stream.of(obj);
        }
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

