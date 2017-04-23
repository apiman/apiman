/*
 * Copyright 2017 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.gateway.platforms.vertx3;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.launcher.VertxCommandLauncher;
import io.vertx.core.impl.launcher.VertxLifecycleHooks;
import io.vertx.core.json.JsonObject;

public class ApimanLauncher extends VertxCommandLauncher implements VertxLifecycleHooks {

    /**
     * Main entry point.
     *
     * @param args the user command line arguments.
     */
    public static void main(String[] args) {
      new ApimanLauncher()
          .register(ApimanVersionCommand.class)
          .dispatch(args);
    }

    /**
     * Utility method to execute a specific command.
     *
     * @param cmd  the command
     * @param args the arguments
     */
    public static void executeCommand(String cmd, String... args) {
      new ApimanLauncher().execute(cmd, args);
    }

    /**
     * Hook for sub-classes of {@link Launcher} after the config has been parsed.
     *
     * @param config the read config, empty if none are provided.
     */
    @Override
    public void afterConfigParsed(JsonObject config) {
    }

    /**
     * Hook for sub-classes of {@link Launcher} before the vertx instance is started.
     *
     * @param options the configured Vert.x options. Modify them to customize the Vert.x instance.
     */
    @Override
    public void beforeStartingVertx(VertxOptions options) {

    }

    /**
     * Hook for sub-classes of {@link Launcher} after the vertx instance is started.
     *
     * @param vertx the created Vert.x instance
     */
    @Override
    public void afterStartingVertx(Vertx vertx) {

    }

    /**
     * Hook for sub-classes of {@link Launcher} before the verticle is deployed.
     *
     * @param deploymentOptions the current deployment options. Modify them to customize the deployment.
     */
    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {

    }

    /**
     * A deployment failure has been encountered. You can override this method to customize the behavior.
     * By default it closes the `vertx` instance.
     *
     * @param vertx             the vert.x instance
     * @param mainVerticle      the verticle
     * @param deploymentOptions the verticle deployment options
     * @param cause             the cause of the failure
     */
    @Override
    public void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
      // Default behaviour is to close Vert.x if the deploy failed
      vertx.close();
    }
}
