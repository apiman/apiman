package io.apiman.gateway.test.junit.vertx3;

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class Vertx3GatewayHelper {


    /**
     * Constructor
     */
    public Vertx3GatewayHelper() {}

    /**
     * Loads the vertx3 config from a resource
     *
     * @param config the config to be read
     * @param name the element of the config to be read
     * @return the vertx3 config as jsonObject
     */
    protected JsonObject loadJsonObjectFromResources(JsonNode config, String name) {
        ClassLoader classLoader = getClass().getClassLoader();
        String configPath = config.get(name).asText();
        String conf;
        String fPath;
        File file;
        try {
            fPath = URLDecoder.decode(classLoader.getResource(configPath).getFile(), StandardCharsets.UTF_8.name());
            file = new File(fPath);
            conf = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new JsonObject(conf);
    }

    /**
     * get api port dynamically from configuration
     * @param apiToFilePushEmulatorConfig configuration
     * @return api port
     */
    public int getApiPortDynamically(JsonObject apiToFilePushEmulatorConfig) {
        return apiToFilePushEmulatorConfig.getJsonObject("verticles").getJsonObject("api").getInteger("port");
    }

    /**
     * get gateway port dynamically from configuration
     * @param apiToFilePushEmulatorConfig configuration
     * @return gateway port
     */
    public int getGatewayPortDynamically(JsonObject apiToFilePushEmulatorConfig) {
        boolean preferSecure = apiToFilePushEmulatorConfig.getBoolean("preferSecure");
        String method = preferSecure ? "https" : "http";
        return apiToFilePushEmulatorConfig.getJsonObject("verticles").getJsonObject(method).getInteger("port");
    }
}
