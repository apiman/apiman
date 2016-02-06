package io.apiman.gateway.api.osgi;

import io.apiman.gateway.api.rest.impl.ApiResourceImpl;
import io.apiman.gateway.api.rest.impl.ClientResourceImpl;
import io.apiman.gateway.api.rest.impl.GatewayApiApplication;
import io.apiman.gateway.api.rest.impl.SystemResourceImpl;
import io.apiman.gateway.api.rest.impl.mappers.RestExceptionMapper;

import java.util.HashSet;
import java.util.Set;

public class GatewayOSGIApplication extends GatewayApiApplication {

    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> classes = new HashSet<>();

    /**
     * Constructor.
     */
    public GatewayOSGIApplication() {
        classes.add(SystemResourceImpl.class);
        classes.add(ApiResourceImpl.class);
        classes.add(ClientResourceImpl.class);

        classes.add(RestExceptionMapper.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
