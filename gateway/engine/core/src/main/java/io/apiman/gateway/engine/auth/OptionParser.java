package io.apiman.gateway.engine.auth;

import java.util.Map;

public interface OptionParser<T> {
    void parseOptions(Map<String, String> options);
    OptionParser<T> newInstance(Map<String, String> options);
}
