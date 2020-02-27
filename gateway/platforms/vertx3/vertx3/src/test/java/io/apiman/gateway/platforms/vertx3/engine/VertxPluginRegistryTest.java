package io.apiman.gateway.platforms.vertx3.engine;

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.plugin.PluginUtils;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.PathResource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.core.Is.is;

/**
 * Unit test for {@link VertxPluginRegistry}.
 *
 * @author Jérémy HAURAY
 */
@RunWith(VertxUnitRunner.class)
public class VertxPluginRegistryTest {

    //Jetty Server for Maven Repo mocking
    private static Server mavenServer;
    private static URI mavenServerUri;

    //Plugin Coordinates used for test
    private static String testPluginCoordinates = "plugin:io.apiman.test:testPlugin:1.0.0.Final/io.apiman.test.PolicyImpl";
    private static String RealPluginCoordinates = "plugin:io.apiman.plugins:apiman-plugins-simple-header-policy:1.5.1.Final/io.apiman.plugins.PolicyImpl";

    /**
     * Build and start a fake local Maven Repository server based on Jetty
     *
     * @throws Exception
     */
    @BeforeClass
    public static void BuildLocalMavenRepo() throws Exception {
        //This function create a Localhost fake Maven Repository, hosting a single Test Plugin

        //Get Test plugin file
        File sourcePlugin = new File("src/test/resources/io/apiman/gateway/platforms/vertx3/engine/plugin-with-policyDefs.war");
        if (!sourcePlugin.exists()) {
            throw new Exception("Failed to find test plugin war at: " + sourcePlugin.getAbsolutePath());
        }

        //Create Local Maven Repository folder
        File repoFolder = Files.createTempDirectory("MockedMavenRepo").toFile();

        //Define Test plugin coordinates
        PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(testPluginCoordinates);

        //Build Test Plugin path in local Maven Repository folder
        File PluginFile = new File(repoFolder, PluginUtils.getMavenPath(coordinates));
        PluginFile.getParentFile().mkdirs();

        //Copy Test Plugin war into repository
        FileUtils.copyFile(sourcePlugin, PluginFile);

        //Create local Maven Repository Web Server
        mavenServer = new Server();
        ServerConnector connector = new ServerConnector(mavenServer);
        // auto-bind to available port
        connector.setPort(0);
        connector.setHost("0.0.0.0");
        mavenServer.addConnector(connector);

        //Add classic ressource handler
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        PathResource pathResource = new PathResource(repoFolder);
        resourceHandler.setBaseResource(pathResource);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resourceHandler, new DefaultHandler()});
        mavenServer.setHandler(handlers);

        //Start local Maven Repository Server
        mavenServer.start();

        // Determine Base URI for Server
        String host = connector.getHost();
        if (host == null || host == "0.0.0.0") host = "127.0.0.1";
        int port = connector.getLocalPort();
        mavenServerUri = new URI(String.format("http://%s:%d/", host, port));
    }

    /**
     * Test with custom configuration (pluginRepositories and pluginsDir) to download a real plugin from Maven Central
     *
     * @param context
     * @throws java.io.IOException
     */
    @Test
    public void getRealPluginFromCustomRegistry(TestContext context) throws java.io.IOException {

        Async waitForPlugin = context.async();

        //Preparing JSON config Object
        List<String> pluginRepositories = Arrays.asList("https://repo1.maven.org/maven2/");
        String pluginsDir = "/tmp/plugins-test1";
        JsonObject jsonObject = new JsonObject(getJsonConfig(pluginRepositories, pluginsDir));

        //Delete temp folder
        File TempDir = new File(pluginsDir);
        if (TempDir.exists()) FileUtils.deleteDirectory(TempDir);

        //Referenced values to test
        Map<String, String> expected = new LinkedHashMap<String, String>() {{
            put("pluginRepositories", String.join(",", pluginRepositories));
            put("pluginsDir", pluginsDir);
        }};

        //Loading VertX configuration
        VertxEngineConfig config = new VertxEngineConfig(jsonObject);
        Map<String, String> pluginRegistryConfig = config.getPluginRegistryConfig();

        //Assert that JSON config object contains the rights parameters
        Assert.assertThat(pluginRegistryConfig, is(expected));

        //Create a fake engine for test plugins loading
        TestVerticle v = new TestVerticle(config);

        //Get pluginRegistry from engine
        IPluginRegistry pluginRegistry = v.createPluginRegistry();

        //Define simple header policy plugin coordinates
        PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(RealPluginCoordinates);

        //Download the plugin
        pluginRegistry.loadPlugin(coordinates, result -> {

            if (result.isSuccess()) {

                //Get downloaded plugin
                Plugin plugin = result.getResult();

                //Assert that's the right plugin
                context.assertEquals(plugin.getCoordinates(), coordinates);

                //Assert plugin is in the right dir
                Path pluginPath = Paths.get(pluginsDir + "/io.apiman.plugins/apiman-plugins-simple-header-policy/1.5.1.Final/apiman-plugins-simple-header-policy.war");
                context.assertTrue(Files.exists(pluginPath));

                waitForPlugin.complete();

            } else {
                context.fail(result.getError());
            }
        });

        waitForPlugin.awaitSuccess();
    }

    /**
     * Test with custom configuration (pluginRepositories and pluginsDir) to download a fake plugin from a fake Maven repo
     *
     * @param context
     * @throws java.io.IOException
     */
    @Test
    public void getFakePluginFromCustomRegistry(TestContext context) throws java.io.IOException {

        Async waitForPlugin = context.async();

        //Preparing JSON config Object
        List<String> pluginRepositories = Arrays.asList(mavenServerUri.toString());
        String pluginsDir = "/tmp/plugins-test2";
        JsonObject jsonObject = new JsonObject(getJsonConfig(pluginRepositories, pluginsDir));

        //Delete temp folder
        File TempDir = new File(pluginsDir);
        if (TempDir.exists()) FileUtils.deleteDirectory(TempDir);

        //Referenced values to test
        Map<String, String> expected = new LinkedHashMap<String, String>() {{
            put("pluginRepositories", String.join(",", pluginRepositories));
            put("pluginsDir", pluginsDir);
        }};

        //Loading VertX configuration
        VertxEngineConfig config = new VertxEngineConfig(jsonObject);
        Map<String, String> pluginRegistryConfig = config.getPluginRegistryConfig();

        //Assert that JSON config object contains the rights parameters
        Assert.assertThat(pluginRegistryConfig, is(expected));


        //Create a fake engine for test plugins loading
        TestVerticle v = new TestVerticle(config);

        //Get pluginRegistry from engine
        IPluginRegistry pluginRegistry = v.createPluginRegistry();

        //Define simple header policy plugin coordinates
        PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(testPluginCoordinates);

        //Download the plugin
        pluginRegistry.loadPlugin(coordinates, result -> {

            if (result.isSuccess()) {

                //Get downloaded plugin
                Plugin plugin = result.getResult();

                //Assert that's the right plugin
                context.assertEquals(plugin.getCoordinates(), coordinates);

                //Assert plugin is in the right dir
                Path pluginPath = Paths.get(pluginsDir + "/io.apiman.test/testPlugin/1.0.0.Final/testPlugin.war");
                context.assertTrue(Files.exists(pluginPath));

                waitForPlugin.complete();

            } else {
                context.fail(result.getError());
            }
        });

        waitForPlugin.awaitSuccess();
    }

    /**
     * Test with custom configuration (pluginRepositories and pluginsDir) to download a fake plugin from a fake Maven repo
     * Despite a unreachable repository in the pluginRepositories array.
     *
     * @param context
     * @throws java.io.IOException
     */
    @Test
    public void getFakePluginUnreachableRegistry(TestContext context) throws java.io.IOException {

        Async waitForPlugin = context.async();

        //Preparing JSON config Object with 2 custom repo : a real and a fake
        List<String> pluginRepositories = Arrays.asList("https://unreachable.maven.org/maven2/", mavenServerUri.toString());
        String pluginsDir = "/tmp/plugins-test3";
        JsonObject jsonObject = new JsonObject(getJsonConfig(pluginRepositories, pluginsDir));

        //Delete temp folder
        File TempDir = new File(pluginsDir);
        if (TempDir.exists()) FileUtils.deleteDirectory(TempDir);

        //Referenced values to test
        Map<String, String> expected = new LinkedHashMap<String, String>() {{
            put("pluginRepositories", String.join(",", pluginRepositories));
            put("pluginsDir", pluginsDir);
        }};

        //Loading VertX configuration
        VertxEngineConfig config = new VertxEngineConfig(jsonObject);
        Map<String, String> pluginRegistryConfig = config.getPluginRegistryConfig();

        //Assert that JSON config object contains the rights parameters
        Assert.assertThat(pluginRegistryConfig, is(expected));


        //Create a fake engine for test plugins loading
        TestVerticle v = new TestVerticle(config);

        //Get pluginRegistry from engine
        IPluginRegistry pluginRegistry = v.createPluginRegistry();

        //Define simple header policy plugin coordinates
        PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(testPluginCoordinates);

        //Download the plugin
        pluginRegistry.loadPlugin(coordinates, result -> {

            if (result.isSuccess()) {

                //Get downloaded plugin
                Plugin plugin = result.getResult();

                //Assert that's the right plugin
                context.assertEquals(plugin.getCoordinates(), coordinates);

                //Assert plugin is in the right dir
                Path pluginPath = Paths.get(pluginsDir + "/io.apiman.test/testPlugin/1.0.0.Final/testPlugin.war");
                context.assertTrue(Files.exists(pluginPath));

                waitForPlugin.complete();

            } else {
                context.fail(result.getError());
            }
        });

        waitForPlugin.awaitSuccess();
    }

    @Test
    public void getRealPluginFromDefaultRegistry(TestContext context) throws java.io.IOException {

        Async waitForPlugin1 = context.async();

        //Preparing JSON config Object
        List<String> pluginRepositories = null;
        String pluginsDir = null;
        JsonObject jsonObject = new JsonObject(getJsonConfig(pluginRepositories, pluginsDir));

        File TempParentDir = Files.createTempDirectory("apiman-gateway-plugins-tmp").getParent().toFile();
        //Delete all old temp dirs
        File[] TempDirs = TempParentDir.listFiles((dir, name) -> name.contains("apiman-gateway-plugins-tmp"));
        for (File td : TempDirs) {
            FileUtils.deleteDirectory(td);
        }

        //Loading VertX configuration
        VertxEngineConfig config = new VertxEngineConfig(jsonObject);

        //Create a fake engine for test plugins loading
        TestVerticle v = new TestVerticle(config);

        //Get pluginRegistry from engine
        IPluginRegistry pluginRegistry = v.createPluginRegistry();

        //Find generated Plugin Temp Dir
        TempDirs = TempParentDir.listFiles((dir, name) -> name.contains("apiman-gateway-plugins-tmp"));
        Arrays.sort(TempDirs, Comparator.comparingLong(File::lastModified).reversed());
        File Tempdir = TempDirs[0];

        //Define simple header policy plugin coordinates
        PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(RealPluginCoordinates);

        //Download the Real plugin (must succeed)
        pluginRegistry.loadPlugin(coordinates, result -> {
            if (result.isSuccess()) {

                //Get downloaded plugin
                Plugin plugin = result.getResult();

                //Assert that's the right plugin
                context.assertEquals(plugin.getCoordinates(), coordinates);

                //Assert plugin is in the right dir
                Path pluginPath = Paths.get(Tempdir + "/io.apiman.plugins/apiman-plugins-simple-header-policy/1.5.1.Final/apiman-plugins-simple-header-policy.war");
                context.assertTrue(Files.exists(pluginPath));

                waitForPlugin1.complete();

            } else {
                context.fail(result.getError());
            }
        });

        waitForPlugin1.awaitSuccess();

        Async waitForPlugin2 = context.async();

        //Define simple header policy plugin coordinates
        PluginCoordinates testcoordinates = PluginCoordinates.fromPolicySpec(testPluginCoordinates);

        //Try download the test plugin (must failed)
        pluginRegistry.loadPlugin(testcoordinates, result -> {
            context.assertFalse(result.isSuccess());

            waitForPlugin2.complete();
        });

        waitForPlugin2.awaitSuccess();
    }

    /**
     * Get Vert.X JSON Configuration file, with customized pluginRepositories and pluginsDir
     *
     * @param pluginRepositories
     * @param pluginsDir
     * @return
     */
    private String getJsonConfig(List<String> pluginRepositories, String pluginsDir) {

        //Minimal configuration to create a fake engine. Include "plugin-registry" to test.
        String jsonStr = "{\n" +
                "   \"registry\":{\n" +
                "      \"class\":\"io.apiman.gateway.engine.es.EsRegistry\",\n" +
                "      \"config\":{\n" +
                "         \"client\":{\n" +
                "            \"type\":\"${test.foo.es.type}\",\n" +
                "            \"cluster-name\":\"elasticsearch\",\n" +
                "            \"host\":\"localhost\",\n" +
                "            \"port\":19250,\n" +
                "            \"initialize\":true\n" +
                "         }\n" +
                "      }\n" +
                "   },\n" +
                "   \"plugin-registry\":{\n" +
                "      \"class\":\"io.apiman.gateway.platforms.vertx3.engine.VertxPluginRegistry\",\n" +
                "      \"config\":{\n" +
                (pluginRepositories != null && pluginRepositories.size() > 0 ?
                        "         \"pluginRepositories\":\n" +
                                "            " + new JsonArray(pluginRepositories).encode() + "\n" : "") +
                ((pluginRepositories != null && pluginRepositories.size() > 0) && StringUtils.isNotEmpty(pluginsDir) ?
                        "         ,\n" : "") +
                (StringUtils.isNotEmpty(pluginsDir) ?
                        "         \"pluginsDir\":\"" + pluginsDir + "\"\n" : "") +
                "      }\n" +
                "   },\n" +
                "   \"metrics\":{\n" +
                "      \"class\":\"io.apiman.gateway.engine.es.EsMetrics\",\n" +
                "      \"config\":{\n" +
                "         \"client\":{\n" +
                "            \"type\":\"es\",\n" +
                "            \"protocol\":\"${apiman.es.protocol}\",\n" +
                "            \"host\":\"${apiman.es.host}\",\n" +
                "            \"port\":\"${apiman.es.port}\",\n" +
                "            \"initialize\":true,\n" +
                "            \"username\":\"${apiman.es.username}\",\n" +
                "            \"password\":\"${apiman.es.password}\",\n" +
                "            \"timeout\":\"${apiman.es.timeout}\"\n" +
                "         }\n" +
                "      }\n" +
                "   }\n" +
                "}";

        return jsonStr;
    }

    @AfterClass
    public static void StopLocalMavenRepo() {
        try {
            if (mavenServer != null) mavenServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
