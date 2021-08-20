package io.apiman.manager.api.beans.events.dto;

import java.time.OffsetDateTime;
import java.util.StringJoiner;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NewAccountCreatedDto {
    private OffsetDateTime time;
    private String userId;
    private String emailAddress;
    private String firstName;
    private String surname;

    public NewAccountCreatedDto() {
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public NewAccountCreatedDto setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public NewAccountCreatedDto setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public NewAccountCreatedDto setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public NewAccountCreatedDto setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public NewAccountCreatedDto setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NewAccountCreatedDto.class.getSimpleName() + "[", "]")
             .add("time=" + time)
             .add("userId='" + userId + "'")
             .add("emailAddress='" + emailAddress + "'")
             .add("firstName='" + firstName + "'")
             .add("surname='" + surname + "'")
             .toString();
    }
}
