package io.apiman.gateway.api.osgi;

import io.apiman.common.servlet.ApimanCorsFilter;
import io.apiman.common.servlet.AuthenticationFilter;
import io.apiman.common.servlet.DisableCachingFilter;
import io.apiman.common.servlet.LocaleFilter;
import io.apiman.common.servlet.RootResourceFilter;
import io.apiman.gateway.platforms.war.filters.HttpRequestThreadLocalFilter;
import io.apiman.gateway.platforms.war.listeners.WarGatewayBootstrapper;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.util.security.Constraint;
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

import java.net.URL;
import java.util.*;

public class Activator implements BundleActivator {

    /**
     * OSGI Service references.
     */
    private ServiceReference<WebContainer> serviceReference;
    private ServiceReference<ConfigurationAdmin> configAdminReference;
    private final static Logger logger = LoggerFactory.getLogger(Activator.class);
    private final String jettyWebXmlLocation = "/WEB-INF/jetty-web.xml";
    private List<ConstraintMapping> constraintMappings;

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
        Dictionary<String, Object> ctxParams = null;

        if (webContainer != null) {

            // create a default context to share between registrations
            final HttpContext httpContext = webContainer.createDefaultHttpContext();

            // set a session timeout of 2 minutes
            webContainer.setSessionTimeout(2, httpContext);

            /*
             * Configure the Security Constraints and Authentication mode (BASIC) to be used
             * Issue : The request is well authenticated but RESTeasy can't reteive the resource. So we will use instead the AuthenticationFilter
             * setupSecurity(webContainer, httpContext, context);
             */


            /*
             * Configure Jetty with the SecurityHandler to be used (JaasLoginService, Basic Authentication) using the jetty-web.xml file
             */
            addJettyWebXml(webContainer, httpContext, context);

            /*
             * Define the Context Parameters of the Servlet
             */
            ctxParams = new Hashtable<String, Object>();
            ctxParams.put("resteasy.servlet.mapping.prefix","/apiman-gateway-api");
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
            logger.info(">> Register HttpRequestThreadLocalFilter");
            webContainer.registerFilter(new HttpRequestThreadLocalFilter(),
                    new String[] { "/apiman-gateway-api/*" }, // url patterns
                    new String[] { "HttpRequestThreadLocalFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            logger.info(">> Register LocaleFilter");
            webContainer.registerFilter(new LocaleFilter(),
                    new String[] { "/apiman-gateway-api/*" }, // url patterns
                    new String[] { "LocalFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            logger.info(">> Register ApimanCorsFilter");
            webContainer.registerFilter(new ApimanCorsFilter(),
                    new String[] { "/apiman-gateway-api/*" }, // url patterns
                    new String[] { "CorsFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            logger.info(">> Register DisableCachingFilter");
            webContainer.registerFilter(new DisableCachingFilter(),
                    new String[] { "/apiman-gateway-api/*" }, // url patterns
                    new String[] { "DisableCachingFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            logger.info(">> Register AuthenticationFilter");
            webContainer.registerFilter(new AuthenticationFilter(),
                    new String[] { "/apiman-gateway-api/*" }, // url patterns
                    new String[] { "AuthenticationFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );
            logger.info(">> Register RootResourceFilter");
            webContainer.registerFilter(new RootResourceFilter(),
                    new String[] { "/apiman-gateway-api/*" }, // url patterns
                    new String[] { "RootResourceFilter" }, // servlet names
                    initParamsFilter, // init params
                    httpContext // http context
            );


            // Register the RestEasyServlet
            initParamsFilter = new Hashtable<String, Object>();
            initParamsFilter.put("javax.ws.rs.Application", "io.apiman.gateway.api.osgi.GatewayOSGIApplication");
            webContainer.registerServlet(new HttpServletDispatcher(),
                    "resteasy",
                    new String[] { "/apiman-gateway-api/*" }, // url patterns
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

    protected void setupSecurity(WebContainer webContainer, HttpContext httpContext, BundleContext context) {
        addJettyWebXml(webContainer, httpContext, context); // Configure Jetty with the SecurityHandler to be used (JaasLoginService, ...) using the jetty-web.xml file

        String[] roles = {"admin","apipublisher"};

        Constraint ct = new Constraint();
        ct.setName("apiman"); // Realm Name to be used
        ct.setAuthenticate(true);
        ct.setDataConstraint(0);
        ct.setRoles(roles);

        ConstraintMapping ctMapping = new ConstraintMapping();
        ctMapping.setConstraint(ct);
        ctMapping.setPathSpec("/apiman-gateway-api/*"); // Path to be secured

        addConstraintMapping(webContainer, httpContext, ctMapping);

        webContainer.registerLoginConfig("BASIC", // Authentication mode
                "apiman", // Realm name
                "", // No Form LoginPage
                "", // No FormError Page
                httpContext
        );
    }

    protected void addJettyWebXml(WebContainer service, HttpContext httpContext, BundleContext context) {
        String jettyWebXmlLoc;
        if (this.jettyWebXmlLocation == null) {
            jettyWebXmlLoc = "/WEB-INF/jetty-web.xml";
        } else {
            jettyWebXmlLoc = this.jettyWebXmlLocation;
        }

        URL jettyWebXml = context.getBundle().getResource(jettyWebXmlLoc);
        if (jettyWebXml != null) {
            logger.info("Found jetty-web XML configuration on bundle classpath on " + jettyWebXmlLoc);
            service.registerJettyWebXml(jettyWebXml, httpContext);
        } else {
            logger.info("Not found jetty-web XML configuration on bundle classpath on " + jettyWebXmlLoc);
        }
    }

    protected void addConstraintMapping(WebContainer service, HttpContext httpContext, ConstraintMapping constraintMapping) {
        Constraint constraint = constraintMapping.getConstraint();
        String[] roles = constraint.getRoles();
        // name property is unavailable on constraint object :/
        String name = "Constraint-" + new Random().nextInt();

        int dataConstraint = constraint.getDataConstraint();
        String dataConstraintStr;
        switch (dataConstraint) {
        case Constraint.DC_UNSET: dataConstraintStr = null; break;
        case Constraint.DC_NONE: dataConstraintStr = "NONE"; break;
        case Constraint.DC_CONFIDENTIAL: dataConstraintStr = "CONFIDENTIAL"; break;
        case Constraint.DC_INTEGRAL: dataConstraintStr = "INTEGRAL"; break;
        default:
            logger.info("Unknown data constraint: " + dataConstraint);
            dataConstraintStr = "CONFIDENTIAL";
        }
        List<String> rolesList = Arrays.asList(roles);

        logger.info("Adding security constraint name=" + name + ", url=" + constraintMapping.getPathSpec() + ", dataConstraint=" + dataConstraintStr + ", canAuthenticate="
                + constraint.getAuthenticate() + ", roles=" + rolesList);
        service.registerConstraintMapping(name, constraintMapping.getPathSpec(), null, dataConstraintStr, constraint.getAuthenticate(), rolesList, httpContext);
    }

}
