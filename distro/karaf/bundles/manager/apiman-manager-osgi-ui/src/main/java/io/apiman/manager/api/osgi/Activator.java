package io.apiman.manager.api.osgi;

import io.apiman.common.servlet.ResourceCacheControlFilter;
import io.apiman.manager.platform.WarUIConfig;
import io.apiman.manager.platform.servlets.AngularServlet;
import io.apiman.manager.platform.servlets.ConfigurationServlet;
import io.apiman.manager.ui.server.servlets.TranslationServlet;
import io.apiman.manager.ui.server.servlets.LogoutServlet;
import io.apiman.manager.ui.server.servlets.UrlFetchProxyServlet;
import io.apiman.manager.ui.server.servlets.TokenRefreshServlet;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.util.security.Constraint;
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
    private List<ConstraintMapping> constraintMappings;
    private final String jettyWebXmlLocation = "/WEB-INF/jetty-web.xml";
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
        WarUIConfig.setConfig(apimanProps);

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
             * Register Filters :
             * ResourceCacheControl
             */
            webContainer.registerFilter(new ResourceCacheControlFilter(),
                    new String[] { "/apimanui/libs/*", "/apimanui/plugins/*", "/apimanui/dist/*" },
                    // url patterns
                    new String[] { "ResourceCacheControl" }, null, httpContext);
/*
            webContainer.registerFilter(new AuthenticationFilter(),
                    new String[] { "/apimanui/*" },
                    new String[] { "AuthenticationFilter" }, null, httpContext
            );*/

            // Register the AngularServlet
            webContainer.registerServlet(new AngularServlet(), "angularServlet",
                    new String[] { "/apimanui/api-manager/*" }, null, httpContext);

            // Register the Configuration Servlet
            webContainer.registerServlet(new ConfigurationServlet(), "configurationJS",
                    new String[] { "/apimanui/apiman/config.js" }, null, httpContext);

            // Register Translation Servlet
            webContainer.registerServlet(new TranslationServlet(), "translationJS",
                    new String[] { "/apimanui/apiman/translations.js" }, null, httpContext);

            // Register Token Refresh
            webContainer.registerServlet(new TokenRefreshServlet(), "tokenRefresh",
                    new String[] { "/apimanui/rest/tokenRefresh" }, null, httpContext);

            // Register Fetch Proxy
            webContainer.registerServlet(new UrlFetchProxyServlet(), "fetchProxy",
                    new String[] { "/apimanui/proxies/fetch/*" }, null, httpContext);

            // Register Logout Servlet
            webContainer.registerServlet(new LogoutServlet(), "logoutServlet",
                    new String[] { "/apimanui/logout" }, null, httpContext);

            webContainer.registerWelcomeFiles(new String[] { "welcome.html" }, false, httpContext);

            webContainer.registerResources("/apimanui", "/", httpContext);
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
        addJettyWebXml(webContainer, httpContext,
                context); // Configure Jetty with the SecurityHandler to be used (JaasLoginService, ...) using the jetty-web.xml file

        String[] roles = { "admin", "apipublisher" };

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
                httpContext);
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

    protected void addConstraintMapping(WebContainer service, HttpContext httpContext,
            ConstraintMapping constraintMapping) {
        Constraint constraint = constraintMapping.getConstraint();
        String[] roles = constraint.getRoles();
        // name property is unavailable on constraint object :/
        String name = "Constraint-" + new Random().nextInt();

        int dataConstraint = constraint.getDataConstraint();
        String dataConstraintStr;
        switch (dataConstraint) {
        case Constraint.DC_UNSET:
            dataConstraintStr = null;
            break;
        case Constraint.DC_NONE:
            dataConstraintStr = "NONE";
            break;
        case Constraint.DC_CONFIDENTIAL:
            dataConstraintStr = "CONFIDENTIAL";
            break;
        case Constraint.DC_INTEGRAL:
            dataConstraintStr = "INTEGRAL";
            break;
        default:
            logger.info("Unknown data constraint: " + dataConstraint);
            dataConstraintStr = "CONFIDENTIAL";
        }
        List<String> rolesList = Arrays.asList(roles);

        logger.info("Adding security constraint name=" + name + ", url=" + constraintMapping.getPathSpec()
                + ", dataConstraint=" + dataConstraintStr + ", canAuthenticate=" + constraint
                .getAuthenticate() + ", roles=" + rolesList);
        service.registerConstraintMapping(name, constraintMapping.getPathSpec(), null, dataConstraintStr,
                constraint.getAuthenticate(), rolesList, httpContext);
    }

}
