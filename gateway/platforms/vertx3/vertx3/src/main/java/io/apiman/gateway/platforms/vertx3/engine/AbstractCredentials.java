package io.apiman.gateway.platforms.vertx3.engine;

import java.io.Closeable;
import java.util.Arrays;

public abstract class AbstractCredentials implements AutoCloseable, Closeable {

    public abstract char[] getUsername();
    public abstract char[] getPassword();

    @Override
    public void close() {
        Arrays.fill(getUsername(), '\u0000');
        Arrays.fill(getPassword(), '\u0000');
    }

    @Override
    public String toString() {
        return "";
    }
}