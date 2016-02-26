package io.apiman.osgi.pax.testing.util;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.web.service.spi.WebListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.inject.Inject;
import java.io.File;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

public class KarafBaseTest {

	protected WebListener webListener;

	protected static final String GROUP_ID = "org.apache.karaf";
    protected static final String ARTIFACT_ID = "apache-karaf";
    protected static final String VERSION = "2.4.4";
	protected static final String APIMAN_VERSION = "1.2.2-SNAPSHOT";

	@Inject
	protected BundleContext bundleContext;

	public Option[] baseConfig() {
		return new Option[] {
				karafDistributionConfiguration().frameworkUrl(
						maven().groupId(GROUP_ID).artifactId(ARTIFACT_ID).version(VERSION).type("tar.gz"))
						.karafVersion(VERSION).useDeployFolder(false).name("Karaf")
						.unpackDirectory(new File("target/paxexam/unpack")),
				configureConsole().ignoreLocalConsole(),
				logLevel(LogLevel.INFO),
				keepRuntimeFolder(),
				editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg","log4j.logger.org.apache.http","INFO"),
				editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg","log4j.logger.org.apache.http.wire","INFO"),
				editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg","org.apache.karaf.features","DEBUG"),
				editConfigurationFilePut("etc/users.properties", "admin", "admin,admin"),
				editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.secure.enabled","true"),
				editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port.secure","8444"),
				editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port","8181"),
				editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.enabled","true"),
				editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.keystore","${karaf.base}/etc/server-keystore.jks"),
				editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.password","apiman"),
				editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.keypassword","apiman"),
				replaceConfigurationFile("etc/server-keystore.jks", new File("src/test/resources/keystore.jks")),
				replaceConfigurationFile("etc/io.apiman.gateway.cfg", new File("src/test/resources/io.apiman.gateway.cfg")),
				replaceConfigurationFile("etc/apiman.properties", new File("src/test/resources/apiman.properties")),
				features(maven("io.apiman","apiman-karaf",APIMAN_VERSION).type("xml").classifier("features"),
					"apiman-gateway-test")
		        };
	}

	protected void initWebListener() {
		webListener = new WebListenerImpl();
		bundleContext.registerService(WebListener.class, webListener, null);
	}

	protected void waitForWebListener() throws InterruptedException {
		new WaitCondition("webapp startup") {
			@Override
			protected boolean isFulfilled() {
				return ((WebListenerImpl)webListener).gotEvent();
			}
		}.waitForCondition();
	}

	protected Bundle installAndStartBundle(String bundlePath)
			throws BundleException, InterruptedException {
		final Bundle bundle = bundleContext.installBundle(bundlePath);
		bundle.start();
		new WaitCondition("bundle startup") {
			@Override
			protected boolean isFulfilled() {
				return bundle.getState() == Bundle.ACTIVE;
			}
		}.waitForCondition();
		return bundle;
	}

}