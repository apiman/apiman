package io.apiman.osgi.pax.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.karaf.features.FeaturesService;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.web.service.spi.WebListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

@RunWith(PaxExam.class)
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
				KarafDistributionOption.editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg","log4j.logger.org.apache.http","DEBUG"),
				KarafDistributionOption.editConfigurationFileExtend("etc/org.ops4j.pax.logging.cfg","log4j.logger.org.apache.http.wire","ERROR"),
				KarafDistributionOption.editConfigurationFilePut("etc/users.properties", "admin", "admin,admin"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.secure.enabled","true"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port.secure","8444"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port","8181"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.enabled","true"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.keystore","/Users/chmoulli/Code/jboss/apiman/apiman-core-forked/distro/karaf/test/src/test/resources/keystore.jks"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.password","apiman"),
				KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.ops4j.pax.web.ssl.keypassword","apiman"),
				KarafDistributionOption.replaceConfigurationFile("etc/io.apiman.gateway.cfg", new File("src/test/resources/io.apiman.gateway.cfg")),
				KarafDistributionOption.replaceConfigurationFile("etc/apiman.properties", new File("src/test/resources/apiman.properties")),
				features(
						maven().groupId("io.apiman")
								.artifactId("apiman-karaf").type("xml")
								.classifier("features").version("1.2.2-SNAPSHOT"),
						"apiman-gateway-test")
				/*wrappedBundle(mavenBundle("org.apache.httpcomponents",
						"httpcore").version("4.4.4")),
				wrappedBundle(mavenBundle("org.apache.httpcomponents",
						"httpmime", "4.1")),
				wrappedBundle(mavenBundle("org.apache.httpcomponents",
						"httpclient").version("4.4.1")),
				mavenBundle().groupId("commons-beanutils")
						.artifactId("commons-beanutils").version("1.8.3"),
				mavenBundle().groupId("commons-collections")
						.artifactId("commons-collections")
						.version("3.2.1"),
				mavenBundle().groupId("commons-codec")
						.artifactId("commons-codec").version("1.10"),
				mavenBundle()
						.groupId("org.apache.servicemix.bundles")
						.artifactId("org.apache.servicemix.bundles.commons-digester")
						.version("1.8_4"),
				mavenBundle().groupId("org.apache.geronimo.bundles").artifactId("commons-discovery").version("0.4_1"),
				mavenBundle()
						.groupId("org.apache.servicemix.specs")
						.artifactId(
								"org.apache.servicemix.specs.jsr303-api-1.0.0")
						.version("1.8.0") */ };
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