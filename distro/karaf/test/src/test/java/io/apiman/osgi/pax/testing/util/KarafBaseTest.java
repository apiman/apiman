package io.apiman.osgi.pax.testing.util;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.karaf.features.FeaturesService;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
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

	protected DefaultHttpClient httpclient;

	protected WebListener webListener;

	public static final String GROUP_ID = "org.apache.karaf";
    public static final String ARTIFACT_ID = "apache-karaf";
    public static final String VERSION = "2.4.4";

	@Inject
	protected FeaturesService featuresService;

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
				KarafDistributionOption.editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg","log4j.logger.org.apache.http","INFO"),
				KarafDistributionOption.editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg","log4j.logger.org.apache.http.wire","INFO"),
				KarafDistributionOption.editConfigurationFilePut("etc/users.properties", "admin", "admin,admin"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.secure.enabled","true"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port.secure","8444"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port","8181"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.enabled","true"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.keystore","${karaf.base}/etc/server-keystore.jks"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.password","apiman"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.keypassword","apiman"),
				KarafDistributionOption.replaceConfigurationFile("etc/server-keystore.jks", new File("src/test/resources/keystore.jks")),
				KarafDistributionOption.replaceConfigurationFile("etc/io.apiman.gateway.cfg", new File("src/test/resources/io.apiman.gateway.cfg")),
				KarafDistributionOption.replaceConfigurationFile("etc/apiman.properties", new File("src/test/resources/apiman.properties")),
				features(
						maven().groupId("io.apiman")
								.artifactId("apiman-karaf").type("xml")
								.classifier("features").version("1.2.2-SNAPSHOT"),
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