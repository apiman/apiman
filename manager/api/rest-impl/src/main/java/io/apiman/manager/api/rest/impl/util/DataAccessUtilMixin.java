package io.apiman.manager.api.rest.impl.util;

import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.exceptions.AbstractRestException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface DataAccessUtilMixin {
    /**
     * Try an action that may result in an exception
     */
    default <T> T tryAction(StorageSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (AbstractRestException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * Try an action that may result in an exception
     */
    default void tryAction(StorageCaller supplier) {
        try {
            supplier.call();
        } catch (AbstractRestException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    @FunctionalInterface
    interface StorageSupplier<T> {
        T get() throws StorageException, Exception;
    }

    @FunctionalInterface
    interface StorageCaller {
        void call() throws StorageException, Exception;
    }
}
