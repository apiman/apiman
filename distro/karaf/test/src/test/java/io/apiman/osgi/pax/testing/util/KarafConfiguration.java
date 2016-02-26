package io.apiman.osgi.pax.testing.util;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;

import java.io.File;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

public class KarafConfiguration {

    protected static final String GROUP_ID = "org.apache.karaf";
    protected static final String ARTIFACT_ID = "apache-karaf";
    protected static final String VERSION = "2.4.4";
    protected static final String APIMAN_VERSION = "1.2.2-SNAPSHOT";

    @Configuration
    public static Option[] config() {
        return new Option[] { karafDistributionConfiguration().frameworkUrl(
                maven().groupId(GROUP_ID).artifactId(ARTIFACT_ID).version(VERSION).type("tar.gz"))
                .karafVersion(VERSION).useDeployFolder(false).name("Karaf").unpackDirectory(
                new File("target/paxexam/unpack")), configureConsole().ignoreLocalConsole(),
                logLevel(LogLevelOption.LogLevel.INFO), keepRuntimeFolder(),
                editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg", "log4j.logger.org.apache.http",
                        "INFO"), editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg",
                "log4j.logger.org.apache.http.wire", "INFO"),
                editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg", "org.apache.karaf.features",
                        "DEBUG"), editConfigurationFilePut("etc/users.properties", "admin", "admin,admin"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.secure.enabled",
                        "true"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port.secure",
                        "8444"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port", "8181"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.enabled",
                        "true"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.keystore",
                        "${karaf.base}/etc/server-keystore.jks"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.password",
                        "apiman"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.keypassword",
                        "apiman"), replaceConfigurationFile("etc/server-keystore.jks",
                new File("src/test/resources/keystore.jks")),
                replaceConfigurationFile("etc/io.apiman.gateway.cfg",
                        new File("src/test/resources/io.apiman.gateway.cfg")),
                replaceConfigurationFile("etc/apiman.properties",
                        new File("src/test/resources/apiman.properties")),
                mavenBundle("org.ops4j.pax.exam","pax-exam-inject","4.5.0"),
                features(
                maven("io.apiman", "apiman-karaf", APIMAN_VERSION).type("xml").classifier("features"),
                "apiman-gateway-test") };
    }

}
