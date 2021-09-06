package io.apiman.manager.api.notifications.email;

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.common.config.options.Predicates;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

import static io.apiman.common.config.options.Predicates.anyOk;
import static io.apiman.common.config.options.Predicates.greaterThanZeroInt;
import static io.apiman.common.config.options.Predicates.greaterThanZeroMsg;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class SmtpEmailConfiguration extends GenericOptionsParser {
    public static final String PREFIX = "email.smtp.";
    private boolean mock = false;
    private boolean ssl = true;
    private StartTLSEnum startTLSMode = StartTLSEnum.OPTIONAL;
    private String from;
    private String host;
    private int port;
    private String[] authMethods;
    private String username;
    private String password;

    // TODO trust store and keystore
    public SmtpEmailConfiguration(Map<String, String> options) {
        super(options);
    }

    @Override
    protected void parse(Map<String, String> options) {
        super.parse(options);
        this.mock = getBool(keys(PREFIX + "mock"), false);
        this.ssl = getBool(keys(PREFIX + "ssl"), true);
        this.startTLSMode = getEnum(keys(PREFIX + "startTLSMode"), StartTLSEnum.OPTIONAL, StartTLSEnum::toValue);
        this.from = getRequiredString(keys(PREFIX + "from"), anyOk(), "");
        this.host = getRequiredString(keys(PREFIX + "host"), anyOk(), "");
        this.port = getInt(keys(PREFIX + "port"), 587, greaterThanZeroInt(), greaterThanZeroMsg());
        this.username = getString(keys(PREFIX + "username"), null, Predicates.anyOk(), "");
        this.password = getString(keys(PREFIX + "password"), null, Predicates.anyOk(), "");
    }

    public boolean isMock() {
        return mock;
    }

    public SmtpEmailConfiguration setMock(boolean mock) {
        this.mock = mock;
        return this;
    }

    public boolean isSsl() {
        return ssl;
    }

    public SmtpEmailConfiguration setSsl(boolean ssl) {
        this.ssl = ssl;
        return this;
    }

    public StartTLSEnum getStartTLSMode() {
        return startTLSMode;
    }

    public SmtpEmailConfiguration setStartTLSMode(
         StartTLSEnum startTLSMode) {
        this.startTLSMode = startTLSMode;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public SmtpEmailConfiguration setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getHost() {
        return host;
    }

    public SmtpEmailConfiguration setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public SmtpEmailConfiguration setPort(int port) {
        this.port = port;
        return this;
    }

    public String[] getAuthMethods() {
        return authMethods;
    }

    public SmtpEmailConfiguration setAuthMethods(String[] authMethods) {
        this.authMethods = authMethods;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public SmtpEmailConfiguration setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SmtpEmailConfiguration setPassword(String password) {
        this.password = password;
        return this;
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
             .add("mock=" + mock)
             .add("ssl=" + ssl)
             .add("startTLSMode=" + startTLSMode)
             .add("from='" + from + "'")
             .add("host='" + host + "'")
             .add("port=" + port)
             .add("authMethods=" + Arrays.toString(authMethods))
             .add("username='" + username + "'")
             .add("password='***'")
             .toString();
    }
}
