package io.apiman.gateway.api.osgi;

import io.apiman.gateway.api.rest.impl.ApiResourceImpl;
import io.apiman.gateway.api.rest.impl.ClientResourceImpl;
import io.apiman.gateway.api.rest.impl.SystemResourceImpl;
import io.apiman.gateway.api.rest.impl.mappers.RestExceptionMapper;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class GatewayOSGIApplication extends Application {

    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> classes = new HashSet<>();

    /**
     * Constructor.
     */
    public GatewayOSGIApplication() {
        classes.add(MessageRestService.class);
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
