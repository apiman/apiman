package io.apiman.manager.api.notifications.email;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class SmtpEmailConfiguration {
    private String from;
    private String host;
    private String port;
    private String[] authMethods;
    private String username;
    private String password;

    // TODO trust store and keystore
    public SmtpEmailConfiguration() {
    }

    private SmtpEmailConfiguration(String from,
         String host,
         String port,
         String[] authMethods,
         String username,
         String password
    ) {
        this.from = from;
        this.host = host;
        this.port = port;
        this.authMethods = authMethods;
        this.username = username;
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String[] getAuthMethods() {
        return authMethods;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public SmtpEmailConfiguration setFrom(String from) {
        this.from = from;
        return this;
    }

    public SmtpEmailConfiguration setHost(String host) {
        this.host = host;
        return this;
    }

    public SmtpEmailConfiguration setPort(String port) {
        this.port = port;
        return this;
    }

    public SmtpEmailConfiguration setAuthMethods(String[] authMethods) {
        this.authMethods = authMethods;
        return this;
    }

    public SmtpEmailConfiguration setUsername(String username) {
        this.username = username;
        return this;
    }

    public SmtpEmailConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }
}
