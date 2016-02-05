package io.apiman.gateway.api.osgi;

import io.apiman.common.servlet.*;
import io.apiman.gateway.platforms.war.filters.HttpRequestThreadLocalFilter;
import io.apiman.gateway.platforms.war.listeners.WarGatewayBootstrapper;
import io.apiman.gateway.platforms.war.servlets.WarGatewayServlet;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpContext;

import java.util.Dictionary;
import java.util.Hashtable;

public class Activator implements BundleActivator {

    /**
     * OSGI Service references.
     */
    private ServiceReference<WebContainer> serviceReference;
    private ServiceReference<ConfigurationAdmin> configAdminReference;

    protected static Dictionary apimanProps;

    /**
     * Called when the OSGi framework starts our bundle.
     */
    public void start(BundleContext context) throws Exception {

        /**
         * Register the OSGI Config Admin Service to retrieve the Apiman.properties
         */
        configAdminReference = context.getServiceReference(ConfigurationAdmin.class);
        while (configAdminReference == null) {
            configAdminReference = context.getServiceReference(ConfigurationAdmin.class);
        }
        ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
        Configuration configuration = configurationAdmin.getConfiguration("io.apiman.gateway", null);
        apimanProps = configuration.getProperties();

        /**
         * Register the WebContainer with the Servlet config
         */
        serviceReference = context.getServiceReference(WebContainer.class);

        while (serviceReference == null) {
            serviceReference = context.getServiceReference(WebContainer.class);
        }

        WebContainer webContainer = (WebContainer) context.getService(serviceReference);
        Dictionary<String, Object> initParamsFilter = null;

        if (webContainer != null) {

            // create a default context to share between registrations
            final HttpContext httpContext = webContainer.createDefaultHttpContext();

            // set a session timeout of 2 minutes
            webContainer.setSessionTimeout(2, httpContext);

            /*
             * Register Apiman listeners : BootStrap & RestEasy
              */
            webContainer.registerEventListener(new WarGatewayBootstrapper(), // registered
                    httpContext // http context
            );

            webContainer.registerEventListener(new ResteasyBootstrap(), // registered
                    httpContext // http context
            );

            /*
             * Register Filters :
             * HttpRequestThreadLocalFilter, LocaleFilter, CorsFilter, CachingFilter,
             * Authentication, RootResource, JAX-RS
             */
            webContainer.registerFilter(new HttpRequestThreadLocalFilter(),
                    new String[] { "/*" }, // url patterns
                    new String[] { "HttpRequestThreadLocalFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            webContainer.registerFilter(new LocaleFilter(),
                    new String[] { "/*" }, // url patterns
                    new String[] { "LocalFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            webContainer.registerFilter(new ApimanCorsFilter(),
                    new String[] { "/*" }, // url patterns
                    new String[] { "CorsFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            webContainer.registerFilter(new DisableCachingFilter(),
                    new String[] { "/*" }, // url patterns
                    new String[] { "DisableCachingFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
/*            webContainer.registerFilter(new AuthenticationFilter(),
                    new String[] { "*//*" }, // url patterns
                    new String[] { "AuthenticationFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );*/
            webContainer.registerFilter(new RootResourceFilter(),
                    new String[] { "/*" }, // url patterns
                    new String[] { "RootResourceFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );


            // Register the RestEasyServlet
            initParamsFilter = new Hashtable<String, Object>();
            initParamsFilter.put("javax.ws.rs.Application", "io.apiman.gateway.api.osgi.GatewayOSGIApplication");
            webContainer.registerServlet(new HttpServletDispatcher(),
                    "ResteasyServlet",
                    new String[] { "/*" }, // url patterns
                    initParamsFilter, // init params
                    httpContext // http context
            );

            // Register static htmls
            webContainer.registerResources("/", "/", httpContext);

        }
    }

    /**
     * Called when the OSGi framework stops our bundle
     */
    public void stop(BundleContext bc) throws Exception {
        if (serviceReference != null) {
            bc.ungetService(serviceReference);
        }
    }

    /*
     * Return Apiman Properties retrieved from OSGI Config Admin (= io.apiman.gateway.cfg file)
     */
    public static Dictionary config() {
        return apimanProps;
    }

    /*
     * Set Apiman Properties (temporary workaround)
     */
/*    public void init() {
        Properties props = new Properties();
        props.setProperty("apiman.es.protocol", "http");
        props.setProperty("apiman.es.host", "localhost");
        props.setProperty("apiman.es.port", "9200");
        props.setProperty("apiman.es.username", "");
        props.setProperty("apiman.es.password", "");
        props.setProperty("apiman.es.timeout", "10000");
        props.setProperty("apiman-gateway.registry", "io.apiman.gateway.engine.es.PollCachingESRegistry");
        props.setProperty("apiman-gateway.registry.client.type", "jest");
        props.setProperty("apiman-gateway.registry.client.protocol", "${apiman.es.protocol}");
        props.setProperty("apiman-gateway.registry.client.host", "${apiman.es.host}");
        props.setProperty("apiman-gateway.registry.client.port", "${apiman.es.port}");
        props.setProperty("apiman-gateway.registry.client.initialize", "true");
        props.setProperty("apiman-gateway.registry.client.username", "${apiman.es.username}");
        props.setProperty("apiman-gateway.registry.client.password", "${apiman.es.password}");
        props.setProperty("apiman-gateway.registry.client.timeout", "${apiman.es.timeout}");
        props.setProperty("apiman-gateway.plugin-registry",
                "io.apiman.gateway.engine.impl.DefaultPluginRegistry");
        props.setProperty("apiman-gateway.connector-factory",
                "io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory");
        props.setProperty("apiman-gateway.policy-factory",
                "io.apiman.gateway.engine.policy.PolicyFactoryImpl");
        props.setProperty("apiman-gateway.components.IPolicyFailureFactoryComponent",
                "io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent");
        props.setProperty("apiman-gateway.components.IBufferFactoryComponent",
                "io.apiman.gateway.engine.impl.ByteBufferFactoryComponent");

        props.setProperty("apiman.hibernate.connection.datasource", "java:jboss/datasources/apiman-manager");
        props.setProperty("apiman.hibernate.dialect", "io.apiman.manager.api.jpa.ApimanH2Dialect");
        props.setProperty("apiman.hibernate.hbm2ddl.auto", "validate");
        props.setProperty("apiman-gateway.connector-factory.http.timeouts.read", "30");
        props.setProperty("apiman-gateway.connector-factory.http.timeouts.write", "30");
        props.setProperty("apiman-gateway.connector-factory.http.timeouts.connect", "10");
        props.setProperty("apiman-gateway.connector-factory.tls.devMode", "true");
        props.setProperty("apiman-gateway.metrics", "io.apiman.gateway.engine.es.ESMetrics");
        props.setProperty("apiman-gateway.metrics.client.type", "jest");
        props.setProperty("apiman-gateway.metrics.client.protocol", "${apiman.es.protocol}");
        props.setProperty("apiman-gateway.metrics.client.host", "${apiman.es.host}");
        props.setProperty("apiman-gateway.metrics.client.port", "${apiman.es.port}");
        props.setProperty("apiman-gateway.metrics.client.initialize", "true");
        props.setProperty("apiman-gateway.metrics.client.username", "${apiman.es.username}");
        props.setProperty("apiman-gateway.metrics.client.password", "${apiman.es.password}");
        props.setProperty("apiman-gateway.metrics.client.timeout", "${apiman.es.timeout}");
        props.setProperty("apiman-gateway.components.ISharedStateComponent",
                "io.apiman.gateway.engine.es.ESSharedStateComponent");
        props.setProperty("apiman-gateway.components.ISharedStateComponent.client.type", "jest");
        props.setProperty("apiman-gateway.components.ISharedStateComponent.client.protocol",
                "${apiman.es.protocol}");
        props.setProperty("apiman-gateway.components.ISharedStateComponent.client.host", "${apiman.es.host}");
        props.setProperty("apiman-gateway.components.ISharedStateComponent.client.port", "${apiman.es.port}");
        props.setProperty("apiman-gateway.components.ISharedStateComponent.client.initialize", "true");
        props.setProperty("apiman-gateway.components.ISharedStateComponent.client.username",
                "${apiman.es.username}");
        props.setProperty("apiman-gateway.components.ISharedStateComponent.client.password",
                "${apiman.es.password}");
        props.setProperty("apiman-gateway.components.ISharedStateComponent.client.timeout",
                "${apiman.es.timeout}");
        props.setProperty("apiman-gateway.components.IRateLimiterComponent",
                "io.apiman.gateway.engine.es.ESRateLimiterComponent");
        props.setProperty("apiman-gateway.components.IRateLimiterComponent.client.type", "jest");
        props.setProperty("apiman-gateway.components.IRateLimiterComponent.client.protocol",
                "${apiman.es.protocol}");
        props.setProperty("apiman-gateway.components.IRateLimiterComponent.client.host", "${apiman.es.host}");
        props.setProperty("apiman-gateway.components.IRateLimiterComponent.client.port", "${apiman.es.port}");
        props.setProperty("apiman-gateway.components.IRateLimiterComponent.client.initialize", "true");
        props.setProperty("apiman-gateway.components.IRateLimiterComponent.client.username",
                "${apiman.es.username}");
        props.setProperty("apiman-gateway.components.IRateLimiterComponent.client.password",
                "${apiman.es.password}");
        props.setProperty("apiman-gateway.components.IRateLimiterComponent.client.timeout",
                "${apiman.es.timeout}");
        props.setProperty("apiman-gateway.components.ICacheStoreComponent",
                "io.apiman.gateway.engine.es.ESCacheStoreComponent");
        props.setProperty("apiman-gateway.components.ICacheStoreComponent.client.type", "jest");
        props.setProperty("apiman-gateway.components.ICacheStoreComponent.client.protocol",
                "${apiman.es.protocol}");
        props.setProperty("apiman-gateway.components.ICacheStoreComponent.client.host", "${apiman.es.host}");
        props.setProperty("apiman-gateway.components.ICacheStoreComponent.client.port", "${apiman.es.port}");
        props.setProperty("apiman-gateway.components.ICacheStoreComponent.client.initialize", "true");
        props.setProperty("apiman-gateway.components.ICacheStoreComponent.client.username",
                "${apiman.es.username}");
        props.setProperty("apiman-gateway.components.ICacheStoreComponent.client.password",
                "${apiman.es.password}");
        props.setProperty("apiman-gateway.components.ICacheStoreComponent.client.timeout",
                "${apiman.es.timeout}");
        props.setProperty("apiman-gateway.components.IJdbcComponent",
                "io.apiman.gateway.engine.impl.DefaultJdbcComponent");
        props.setProperty("apiman-gateway.components.ILdapComponent",
                "io.apiman.gateway.engine.impl.DefaultLdapComponent");
        props.setProperty("apiman-gateway.public-endpoint", "https://localhost:8443/apiman-gateway");

        System.setProperties(props);
    }*/
}
