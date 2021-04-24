package io.apiman.gateway.platforms.vertx3.engine;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class UsernamePasswordCredentials extends AbstractCredentials {

    private final char[] password;
    private final String principle;

    public UsernamePasswordCredentials(String username, String password) {
        this.principle = username;
        this.password = password.toCharArray();
    }

    public UsernamePasswordCredentials(String username, char[] password) {
        this.principle = username;
        this.password = password;
    }

    @Override
    public String getPrinciple() {
        return principle;
    }

    public char[] getPassword() {
        return password;
    }

    public String getPasswordAsString() {
        return new String(password);
    }

    @Override
    public void close() throws IOException {
        Arrays.fill(password, '\u0000');
    }

    @Override
    public String toString() {
        return "UsernamePasswordCredentials{" +
            "password=***" +
            ", principle='" + principle + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsernamePasswordCredentials that = (UsernamePasswordCredentials) o;
        return Arrays.equals(password, that.password) && Objects
            .equals(principle, that.principle);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(principle);
        result = 31 * result + Arrays.hashCode(password);
        return result;
    }
}
