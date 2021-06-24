/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.common.config.options.exceptions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Thrown to indicate that a provided configuration option is invalid in some way, for example because it
 * could not be parsed or failed a constraint check.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}.
 */
public class BadOptionConfigurationException extends IllegalArgumentException {

    private String expectedType;
    private String optionName;
    private String actualValue;
    private String constraintFailureMessage;

    /**
     * Constructs an {@link BadOptionConfigurationException}.
     */
    public BadOptionConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Indicate a parsing failure.
     *
     * @param optionName   option name/key being parsed.
     * @param expectedType the anticipated type (e.g. boolean).
     * @param actualValue  the actual found.
     * @param cause        the cause of this issue, if it is an exception (e.g. NumberFormatException).
     */
    public static BadOptionConfigurationException parseFailure(String optionName, String expectedType,
        String actualValue, Throwable cause) {

        String msg = "Expected '" + optionName + "' to be of type " + expectedType + " but provided value '"
            + actualValue + "' could not be parsed";
        return new BadOptionConfigurationException(msg, cause)
            .setOptionName(optionName)
            .setExpectedType(expectedType)
            .setActualValue(actualValue);
    }

    /**
     * Indicate a parsing failure.
     *
     * @param optionName   option name/key being parsed.
     * @param expectedType the anticipated type (e.g. boolean).
     * @param actualValue  the actual found.
     */
    public static BadOptionConfigurationException parseFailure(String optionName, String expectedType,
        String actualValue) {
        String msg = "Expected '" + optionName + "' to be of type " + expectedType + " but provided value '"
            + actualValue + "' could not be parsed";
        return new BadOptionConfigurationException(msg, null)
            .setOptionName(optionName)
            .setExpectedType(expectedType)
            .setActualValue(actualValue);
    }

    /**
     * Indicate a constraint failure.
     *
     * @param optionName               option name/key being parsed.
     * @param expectedType             the anticipated type (e.g. boolean).
     * @param actualValue              the actual found.
     * @param constraintFailureMessage a human-readable message to display in the case of a constraint failure
     *                                 (e.g. port should be greater than 0).
     */
    public static BadOptionConfigurationException constraintFailure(String optionName, String expectedType,
        String actualValue, String constraintFailureMessage) {
        String msg = "Option '" + optionName + "' of type " + expectedType + " failed a validation check: "
            + constraintFailureMessage;
        return new BadOptionConfigurationException(msg, null)
            .setOptionName(optionName)
            .setExpectedType(expectedType)
            .setActualValue(actualValue)
            .setConstraintFailureMessage(constraintFailureMessage);
    }

    /**
     * Indicate a failure to provide a required value.
     *
     * @param keyAliases   the key aliases. The first key will be used. List must not be empty.
     * @param expectedType the anticipated type (e.g. boolean).
     */
    public static BadOptionConfigurationException requiredValue(List<String> keyAliases,
        String expectedType) {

        String optionName = keyAliases.get(0);
        String msg = "A value of type " + expectedType + " must be provided for '" + optionName + "'";
        return new BadOptionConfigurationException(msg, null)
            .setOptionName(optionName)
            .setExpectedType(expectedType);
    }

    /**
     * Get the expected type.
     */
    public Optional<String> getExpectedType() {
        return Optional.of(expectedType);
    }

    private BadOptionConfigurationException setExpectedType(String expectedType) {
        this.expectedType = expectedType;
        return this;
    }

    /**
     * Get the option name/key.
     */
    public Optional<String> getOptionName() {
        return Optional.ofNullable(optionName);
    }

    private BadOptionConfigurationException setOptionName(String optionName) {
        this.optionName = optionName;
        return this;
    }

    /**
     * Get the actual value parsed.
     */
    public Optional<String> getActualValue() {
        return Optional.ofNullable(actualValue);
    }

    private BadOptionConfigurationException setActualValue(String actualValue) {
        this.actualValue = actualValue;
        return this;
    }

    /**
     * Get the constraint failure message, if there was a constraint failure.
     */
    public Optional<String> getConstraintFailureMessage() {
        return Optional.ofNullable(constraintFailureMessage);
    }

    private BadOptionConfigurationException setConstraintFailureMessage(
        String constraintFailureMessage) {
        this.constraintFailureMessage = constraintFailureMessage;
        return this;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BadOptionConfigurationException that = (BadOptionConfigurationException) o;
        return Objects.equals(expectedType, that.expectedType) && Objects
            .equals(optionName, that.optionName) && Objects.equals(actualValue, that.actualValue)
            && Objects.equals(constraintFailureMessage, that.constraintFailureMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedType, optionName, actualValue, constraintFailureMessage);
    }
}
