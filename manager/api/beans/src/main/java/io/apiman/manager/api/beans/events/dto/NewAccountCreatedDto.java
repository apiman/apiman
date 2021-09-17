package io.apiman.manager.api.beans.events.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * A new account has been created.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class NewAccountCreatedDto {
    private OffsetDateTime time;
    private String userId;
    private String username;
    private String emailAddress;
    private String firstName;
    private String surname;
    private Set<String> roles;
    private Map<String, List<String>> attributes;
    private boolean approvalRequired;

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

    public String getUsername() {
        return username;
    }

    public NewAccountCreatedDto setUsername(String username) {
        this.username = username;
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

    public Set<String> getRoles() {
        return roles;
    }

    public NewAccountCreatedDto setRoles(Set<String> roles) {
        this.roles = roles;
        return this;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public NewAccountCreatedDto setAttributes(
         Map<String, List<String>> attributes) {
        this.attributes = attributes;
        return this;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public NewAccountCreatedDto setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NewAccountCreatedDto.class.getSimpleName() + "[", "]")
             .add("time=" + time)
             .add("userId='" + userId + "'")
             .add("username='" + username + "'")
             .add("emailAddress='" + emailAddress + "'")
             .add("firstName='" + firstName + "'")
             .add("surname='" + surname + "'")
             .add("roles=" + roles)
             .add("attributes=" + attributes)
             .add("approvalRequired=" + approvalRequired)
             .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NewAccountCreatedDto that = (NewAccountCreatedDto) o;
        return approvalRequired == that.approvalRequired && Objects.equals(time, that.time)
             && Objects.equals(userId, that.userId) && Objects.equals(username, that.username)
             && Objects.equals(emailAddress, that.emailAddress) && Objects.equals(firstName,
             that.firstName) && Objects.equals(surname, that.surname) && Objects.equals(roles,
             that.roles) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, userId, username, emailAddress, firstName, surname, roles, attributes,
             approvalRequired);
    }
}
