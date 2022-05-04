package io.apiman.manager.api.notifications.email;

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.common.config.options.Predicates;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static io.apiman.common.config.options.Predicates.anyOk;
import static io.apiman.common.config.options.Predicates.greaterThanZeroInt;
import static io.apiman.common.config.options.Predicates.greaterThanZeroMsg;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class SmtpEmailConfiguration extends GenericOptionsParser {
    public static final String PREFIX = "smtp.";
    private boolean enabled;
    private boolean mock;
    private boolean ssl;
    private StartTLSEnum startTLSMode = StartTLSEnum.OPTIONAL;
    private String fromName;
    private String fromEmail;
    private String host;
    private int port;
    private String[] authMethods;
    private String username;
    private String password;

    // TODO trust store and keystore?
    public SmtpEmailConfiguration(Map<String, String> options) {
        super(options);
    }

    @Override
    protected void parse(Map<String, String> options) {
        super.parse(options);
        this.enabled = getBool(keys("enable"), false);
        if (enabled) {
            this.mock = getBool(keys(PREFIX + "mock"), false);
            this.ssl = getBool(keys(PREFIX + "ssl"), true);
            this.startTLSMode = getEnum(keys(PREFIX + "startTLSMode"), StartTLSEnum.OPTIONAL, StartTLSEnum::toValue);
            this.fromName = getRequiredString(keys(PREFIX + "fromName"), anyOk(), "");
            this.fromEmail = getRequiredString(keys(PREFIX + "fromEmail"), anyOk(), "");
            this.host = getRequiredString(keys(PREFIX + "host"), anyOk(), "");
            this.port = getInt(keys(PREFIX + "port"), 587, greaterThanZeroInt(), greaterThanZeroMsg());
            this.username = getString(keys(PREFIX + "username"), null, Predicates.anyOk(), "");
            this.password = getString(keys(PREFIX + "password"), null, Predicates.anyOk(), "");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public SmtpEmailConfiguration setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isMock() {
        return mock;
    }

    public boolean isSsl() {
        return ssl;
    }

    public StartTLSEnum getStartTLSMode() {
        return startTLSMode;
    }

    public String getFromName() {
        return fromName;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
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

    public enum StartTLSEnum {
        DISABLED, OPTIONAL, REQUIRED;

        public static StartTLSEnum toValue(String in) {
            for (StartTLSEnum value : values()) {
                if (value.name().equalsIgnoreCase(in)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(
                 MessageFormat.format("Argument {0} not a recognised value from {1}", in, values()));
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SmtpEmailConfiguration.class.getSimpleName() + "[", "]")
                .add("enabled=" + enabled)
                .add("mock=" + mock)
                .add("ssl=" + ssl)
                .add("startTLSMode=" + startTLSMode)
                .add("fromName='" + fromName + "'")
                .add("fromEmail='" + fromEmail + "'")
                .add("host='" + host + "'")
                .add("port=" + port)
                .add("authMethods=" + Arrays.toString(authMethods))
                .add("username='xxxx'")
                .add("password='xxxx'")
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
        SmtpEmailConfiguration that = (SmtpEmailConfiguration) o;
        return enabled == that.enabled && mock == that.mock && ssl == that.ssl && port == that.port && startTLSMode == that.startTLSMode && Objects.equals(fromName,
                that.fromName) && Objects.equals(fromEmail, that.fromEmail) && Objects.equals(host, that.host) && Arrays.equals(authMethods,
                that.authMethods) && Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(enabled, mock, ssl, startTLSMode, fromName, fromEmail, host, port, username, password);
        result = 31 * result + Arrays.hashCode(authMethods);
        return result;
    }
}
