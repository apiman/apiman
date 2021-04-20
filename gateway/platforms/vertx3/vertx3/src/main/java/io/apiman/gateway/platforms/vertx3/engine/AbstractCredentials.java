package io.apiman.gateway.platforms.vertx3.engine;

import java.io.Closeable;

public abstract class AbstractCredentials implements AutoCloseable, Closeable {

    public abstract String getPrinciple();

    public abstract String getPasswordAsString();

    public abstract char[] getPassword();

    @Override
    public String toString() {
        return "AbstractCredentials{principle=" + getPrinciple()  +"}";
    }
}