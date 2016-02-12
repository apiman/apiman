package io.apiman.manager.osgi;

import io.apiman.common.servlet.ApimanCorsFilter;
import io.apiman.common.servlet.DisableCachingFilter;
import io.apiman.common.servlet.LocaleFilter;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class Activator implements BundleActivator {

    /**
     * OSGI Service references.
     */
    private ServiceReference<WebContainer> serviceReference;
    private ServiceReference<ConfigurationAdmin> configAdminReference;
    private final static Logger logger = LoggerFactory.getLogger(Activator.class);

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
        Configuration configuration = configurationAdmin.getConfiguration("io.apiman.manager", null);
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
        Dictionary<String, Object> ctxParams = null;

        if (webContainer != null) {

            // create a default context to share between registrations
            final HttpContext httpContext = webContainer.createDefaultHttpContext();

            // set a session timeout of 2 minutes
            webContainer.setSessionTimeout(2, httpContext);
            
                        /*
             * Define the Context Parameters of the Servlet
             */
            ctxParams = new Hashtable<String, Object>();
            ctxParams.put("resteasy.servlet.mapping.prefix","/apiman");
            ctxParams.put("resteasy.providers","org.jboss.resteasy.plugins.providers.DataSourceProvider,\n"
                    + "            org.jboss.resteasy.plugins.providers.DocumentProvider,\n"
                    + "            org.jboss.resteasy.plugins.providers.DefaultTextPlain,\n"
                    + "            org.jboss.resteasy.plugins.providers.StringTextStar,\n"
                    + "            org.jboss.resteasy.plugins.providers.InputStreamProvider,\n"
                    + "            org.jboss.resteasy.plugins.providers.ByteArrayProvider,\n"
                    + "            org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider,\n"
                    + "            org.jboss.resteasy.plugins.providers.FileProvider,\n"
                    + "            org.jboss.resteasy.plugins.providers.StreamingOutputProvider,\n"
                    + "            org.jboss.resteasy.plugins.providers.IIOImageProvider,\n"
                    + "            org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider,\n"
                    + "            org.jboss.resteasy.plugins.providers.jackson.JacksonJsonpInterceptor,\n"
                    + "            org.jboss.resteasy.plugins.interceptors.CacheControlInterceptor,\n"
                    + "            org.jboss.resteasy.plugins.interceptors.encoding.AcceptEncodingGZIPInterceptor,\n"
                    + "            org.jboss.resteasy.plugins.interceptors.encoding.ClientContentEncodingHeaderInterceptor,\n"
                    + "            org.jboss.resteasy.plugins.interceptors.encoding.GZIPDecodingInterceptor,\n"
                    + "            org.jboss.resteasy.plugins.interceptors.encoding.GZIPEncodingInterceptor,\n"
                    + "            org.jboss.resteasy.plugins.interceptors.encoding.ServerContentEncodingHeaderInterceptor");
            webContainer.setContextParam(ctxParams, httpContext);

            webContainer.registerEventListener(new ResteasyBootstrap(), // registered
                    httpContext // http context
            );

            webContainer.registerFilter(new LocaleFilter(),
                    new String[] { "/apiman/*" }, // url patterns
                    new String[] { "LocalFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            logger.info(">> Register ApimanCorsFilter");
            webContainer.registerFilter(new ApimanCorsFilter(),
                    new String[] { "/apiman/*" }, // url patterns
                    new String[] { "CorsFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            logger.info(">> Register DisableCachingFilter");
            webContainer.registerFilter(new DisableCachingFilter(),
                    new String[] { "/apiman/*" }, // url patterns
                    new String[] { "DisableCachingFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );

            // Register the RestEasyServlet
            initParamsFilter = new Hashtable<String, Object>();
            initParamsFilter.put("javax.ws.rs.Application", "io.apiman.gateway.api.osgi.GatewayOSGIApplication");
            webContainer.registerServlet(new HttpServletDispatcher(),
                    "resteasy",
                    new String[] { "/apiman/*" }, // url patterns
                    initParamsFilter, // init params
                    httpContext // http context
            );

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
}
