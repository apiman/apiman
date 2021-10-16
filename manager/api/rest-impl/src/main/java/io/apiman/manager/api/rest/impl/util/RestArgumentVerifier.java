package io.apiman.manager.api.rest.impl.util;

import io.apiman.manager.api.rest.exceptions.InputTooLargeException;
import io.apiman.manager.api.rest.exceptions.InvalidParameterException;

import java.util.Objects;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class RestArgumentVerifier {
    static void checkArgument(boolean predicate, Object message) {
        if (!predicate) {
            throw new InvalidParameterException(Objects.toString(message));
        }
    }

    static void checkSizeMax(long actual, long max, Object message) {
        if (actual > max) {
            throw new InputTooLargeException(Objects.toString(message));
        }
    }
}
