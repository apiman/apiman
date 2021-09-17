package io.apiman.manager.api.beans.events;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface ApimanEventBuilderMixin {
    Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    default <T> void beanValidate(T target) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(target);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
