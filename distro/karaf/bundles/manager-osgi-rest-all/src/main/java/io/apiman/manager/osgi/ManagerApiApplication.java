package io.apiman.manager.osgi;

import io.apiman.manager.api.config.Version;
import io.apiman.manager.api.rest.impl.ActionResourceImpl;
import io.apiman.manager.api.rest.impl.ApiManagerApplication;
import io.apiman.manager.api.rest.impl.CurrentUserResourceImpl;
import io.apiman.manager.api.rest.impl.DownloadResourceImpl;
import io.apiman.manager.api.rest.impl.GatewayResourceImpl;
import io.apiman.manager.api.rest.impl.OrganizationResourceImpl;
import io.apiman.manager.api.rest.impl.PermissionsResourceImpl;
import io.apiman.manager.api.rest.impl.PluginResourceImpl;
import io.apiman.manager.api.rest.impl.PolicyDefinitionResourceImpl;
import io.apiman.manager.api.rest.impl.RoleResourceImpl;
import io.apiman.manager.api.rest.impl.SearchResourceImpl;
import io.apiman.manager.api.rest.impl.SystemResourceImpl;
import io.apiman.manager.api.rest.impl.UserResourceImpl;
import io.apiman.manager.api.rest.impl.mappers.RestExceptionMapper;
import io.swagger.jaxrs.config.BeanConfig;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Useful if jax-rs is not supported by the runtime platform.
 *
 */
@ApplicationPath("/")
public class ManagerApiApplication extends Application {

    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> classes = new HashSet<>();

    /**
     * Constructor.
     */
    public ManagerApiApplication() {

        //add swagger 2.0 config
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(new Version().getVersionString());
        beanConfig.setBasePath("/apiman"); //$NON-NLS-1$
        beanConfig.setResourcePackage("io.apiman.manager.api.rest.contract"); //$NON-NLS-1$
        //TODO set more info in the beanConfig (title,description, host, port, etc)
        beanConfig.setScan(true);

        classes.add(SystemResourceImpl.class);
        classes.add(SearchResourceImpl.class);
        classes.add(RoleResourceImpl.class);
        classes.add(UserResourceImpl.class);
        classes.add(CurrentUserResourceImpl.class);
        classes.add(PermissionsResourceImpl.class);
        classes.add(OrganizationResourceImpl.class);
        classes.add(PolicyDefinitionResourceImpl.class);
        classes.add(GatewayResourceImpl.class);
        classes.add(PluginResourceImpl.class);
        classes.add(ActionResourceImpl.class);
        classes.add(DownloadResourceImpl.class);
        /*

        //add swagger 2.0 resource
        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        classes.add(RestExceptionMapper.class);*/
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
