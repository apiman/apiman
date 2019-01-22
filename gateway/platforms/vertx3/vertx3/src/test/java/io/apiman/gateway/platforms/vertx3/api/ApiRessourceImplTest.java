package io.apiman.gateway.platforms.vertx3.api;

import io.apiman.gateway.engine.*;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ApiRessourceImplTest {

    private static String config = "{\n" +
            "  \"publicEndpoint\": \"https://gateway.acme-corp.com\",\n" +
            "\n" +
            "  \"verticles\": {\n" +
            "    \"https\": {\n" +
            "      \"port\": \"80\",\n" +
            "      \"count\": \"auto\"\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"preferSecure\": true\n" +
            "}";

    private JsonObject apimanConfig = new JsonObject(config);
    private ApiResourceImpl apiResource = new ApiResourceImpl(new VertxEngineConfig(apimanConfig), new DummyEngine());
    private ApiEndpoint endpoint;

    @Test
    public void testGetApiEndpoint(){
        String matchingEndpoint = "https://gateway.acme-corp.com/TestOrg/TestAPI/1.0";

        // Test correct user input
        endpoint = apiResource.getApiEndpoint("TestOrg", "TestAPI", "1.0");
        Assert.assertEquals(matchingEndpoint, endpoint.getEndpoint());


        // Test with trailing slash
        apimanConfig.put("publicEndpoint", "https://gateway.acme-corp.com/");
        apiResource = new ApiResourceImpl(new VertxEngineConfig(apimanConfig), new DummyEngine());
        endpoint = apiResource.getApiEndpoint("TestOrg", "TestAPI", "1.0");
        Assert.assertEquals(matchingEndpoint, endpoint.getEndpoint());


        // Test with path
        matchingEndpoint = "https://gateway.acme-corp.com/TestPath/TestOrg/TestAPI/1.0";

        apimanConfig.put("publicEndpoint", "https://gateway.acme-corp.com/TestPath");
        apiResource = new ApiResourceImpl(new VertxEngineConfig(apimanConfig), new DummyEngine());
        endpoint = apiResource.getApiEndpoint("TestOrg", "TestAPI", "1.0");
        Assert.assertEquals(matchingEndpoint, endpoint.getEndpoint());

        // Test with path and trailing slash
        apimanConfig.put("publicEndpoint", "https://gateway.acme-corp.com/TestPath/");
        apiResource = new ApiResourceImpl(new VertxEngineConfig(apimanConfig), new DummyEngine());
        endpoint = apiResource.getApiEndpoint("TestOrg", "TestAPI", "1.0");
        Assert.assertEquals(matchingEndpoint, endpoint.getEndpoint());


        // Test with other port in config
        matchingEndpoint = "https://gateway.acme-corp.com:4444/TestOrg/TestAPI/1.0";

        apimanConfig = new JsonObject(config);
        apimanConfig.getJsonObject("verticles").getJsonObject("https").put("port", "4444");
        apiResource = new ApiResourceImpl(new VertxEngineConfig(apimanConfig), new DummyEngine());
        endpoint = apiResource.getApiEndpoint("TestOrg", "TestAPI", "1.0");
        Assert.assertEquals(matchingEndpoint, endpoint.getEndpoint());


        // Test with other port in config and path
        matchingEndpoint = "https://gateway.acme-corp.com:4444/TestPath/TestOrg/TestAPI/1.0";

        apimanConfig.put("publicEndpoint", "https://gateway.acme-corp.com/TestPath");
        apiResource = new ApiResourceImpl(new VertxEngineConfig(apimanConfig), new DummyEngine());
        endpoint = apiResource.getApiEndpoint("TestOrg", "TestAPI", "1.0");
        Assert.assertEquals(matchingEndpoint, endpoint.getEndpoint());

        // Test with other port in config, path and trailing slash
        apimanConfig.put("publicEndpoint", "https://gateway.acme-corp.com/TestPath/");
        apiResource = new ApiResourceImpl(new VertxEngineConfig(apimanConfig), new DummyEngine());
        endpoint = apiResource.getApiEndpoint("TestOrg", "TestAPI", "1.0");
        Assert.assertEquals(matchingEndpoint, endpoint.getEndpoint());


        // Test with other port in publicEndpoint, path and trailing slash
        matchingEndpoint = "https://gateway.acme-corp.com:5555/TestPath/TestOrg/TestAPI/1.0";

        apimanConfig.put("publicEndpoint", "https://gateway.acme-corp.com:5555/TestPath/");
        apiResource = new ApiResourceImpl(new VertxEngineConfig(apimanConfig), new DummyEngine());
        endpoint = apiResource.getApiEndpoint("TestOrg", "TestAPI", "1.0");
        Assert.assertEquals(matchingEndpoint, endpoint.getEndpoint());
    }


    public class DummyEngine implements IEngine {
        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public IApiRequestExecutor executor(ApiRequest request, IAsyncResultHandler<IEngineResult> resultHandler) {
            return null;
        }

        @Override
        public IRegistry getRegistry() {
            return null;
        }

        @Override
        public IPluginRegistry getPluginRegistry() {
            return null;
        }

        @Override
        public IApiRequestPathParser getApiRequestPathParser() {
            return null;
        }
    }
}
