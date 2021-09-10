package io.apiman.manager.api.beans.download;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate String fields containing a UID reference to the blobstore. A JAX-RS interceptor may rewrite the blob
 * reference into a real resolvable URI just before the field is serialized at the presentation layer.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BlobReference {
}
