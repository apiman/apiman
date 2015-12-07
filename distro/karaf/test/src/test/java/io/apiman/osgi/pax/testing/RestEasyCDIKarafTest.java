package io.apiman.osgi.pax.testing;

import org.apache.karaf.features.FeaturesService;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.cdi.spi.CdiContainer;
import org.ops4j.pax.cdi.spi.CdiContainerFactory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class RestEasyCDIKarafTest {

    private static final transient Logger logger = LoggerFactory.getLogger(RestEasyCDIKarafTest.class);

    @Inject
    BundleContext context;

    @Inject
    private CdiContainerFactory factory;

    /* JBoss fuse 6.2.1 */
    public static final String GROUP_ID = "org.jboss.fuse";
    public static final String ARTIFACT_ID = "jboss-fuse-full";
    public static final String VERSION = "6.2.1.redhat-083";


/*    public static final String GROUP_ID = "org.apache.karaf";
    public static final String ARTIFACT_ID = "apache-karaf";
    public static final String VERSION = "2.4.3";*/

    public static final String HTTP_PORT = "8282";
    public static final String RMI_REG_PORT = "54444";
    public static final String RMI_SERVER_PORT = "5099";
    public static final String SSH_PORT = "8109";

    @Configuration public Option[] config() {

        //MavenArtifactUrlReference karafUrl = maven().groupId("org.apache.karaf").artifactId("apache-karaf").versionAsInProject().type("zip");
        return new Option[] {
                // KarafDistributionOption.debugConfiguration("8889", true),
                karafDistributionConfiguration().frameworkUrl(
                        maven().groupId(GROUP_ID).artifactId(ARTIFACT_ID).version(VERSION).type("zip"))
                        .karafVersion("2.4.0").useDeployFolder(false).name("JBoss Fuse").unpackDirectory(
                        new File("target/paxexam/unpack")), configureConsole().ignoreLocalConsole(),
                editConfigurationFilePut("etc/users.properties", "admin", "admin,admin"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port", HTTP_PORT),
                editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", RMI_REG_PORT),
                editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", RMI_SERVER_PORT),
                editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", SSH_PORT),
                // enable JMX RBAC security, thanks to the KarafMBeanServerBuilder
                configureSecurity().disableKarafMBeanServerBuilder(), keepRuntimeFolder(),
                configureConsole().ignoreLocalConsole(),
                logLevel(LogLevelOption.LogLevel.INFO),
                // Load the features
                //loadApimanFeatures("keycloak","apiman-lib","swagger/1.5.4","elasticsearch/1.7.2","apiman-common","apiman-gateway","apiman-manager-api-es","manager-osgi")
                loadApimanFeatures("apiman-all")
        };
    }

    @Test
    public void test() throws Exception {
        assertTrue(true);
    }

    @Test
    public void EstoreInjected() throws Exception {
        Bundle bundle = getBundle(context,"io.apiman.manager-api-es");
        assertNotNull(bundle);
        CdiContainer container = factory.getContainer(bundle);
        assertNotNull(container);
        BeanManager bm = container.getBeanManager();
        assertNotNull(bm);
    }


    public static MavenArtifactProvisionOption getFeaturesUrl(String groupId, String artifactId, String version) {
        MavenArtifactProvisionOption mapo = mavenBundle().groupId(groupId).artifactId(artifactId);
        mapo.type("xml");
        mapo.classifier("features");

        if (version == null) {
            mapo.versionAsInProject();
        } else {
            mapo.version(version);
        }

        logger.info("Features URL: " + mapo.getURL());

        return mapo;
    }

    public static Option loadApimanFeatures(String... features) {
        MavenArtifactProvisionOption repo = getFeaturesUrl("io.apiman","apiman-karaf","1.2.0-SNAPSHOT");
        return features(repo, features);
    }

    public static Option loadApimanFeatures(List<String> features) {
        return loadApimanFeatures(features.toArray(new String[features.size()]));
    }

    Bundle getBundle(BundleContext bundleContext, String symbolicName) {
        Bundle result = null;
        for (Bundle candidate : bundleContext.getBundles()) {
            if (candidate.getSymbolicName().equals(symbolicName)) {
                if (result == null || result.getVersion().compareTo(candidate.getVersion()) < 0) {
                    result = candidate;
                }
            }
        }
        return result;
    }

}


