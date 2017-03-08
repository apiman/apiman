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

package io.apiman.gateway.engine.vertx.polling.fetchers;

import io.apiman.gateway.engine.vertx.polling.exceptions.BadResponseCodeError;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.impl.Arguments;

import java.net.URI;
import java.util.Map;

/**
 * Fetch a remote HTTP/S resource using a 3scale Access Token.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class AccessTokenResourceFetcher {
    private Vertx vertx;
    private Map<String, String> options;
    private URI apiUri;
    private boolean isHttps;
    private Buffer rawData = Buffer.buffer();
    private Handler<Throwable> exceptionHandler;
    private String accessToken;

    public AccessTokenResourceFetcher(Vertx vertx, Map<String, String> options, URI uri) {
        this.vertx = vertx;
        this.options = options;
        this.apiUri = uri;
        this.isHttps = uri.getScheme().equals("https") ? true : false;
        this.accessToken = requireOpt("accessToken", "accessToken is required in configuration");
    }

    private int getPort() {
        if (apiUri.getPort() == -1) {
            if (isHttps) {
                return 443;
            } else {
                return 80;
            }
        }
        return apiUri.getPort();
    }

    public void fetch(Handler<Buffer> resultHandler) {
      String path = apiUri.getPath() + "?access_token=" + accessToken;
      vertx.createHttpClient(new HttpClientOptions().setSsl(isHttps))
      .get(getPort(), apiUri.getHost(), path, clientResponse -> {
          clientResponse.exceptionHandler(exceptionHandler);

          if (clientResponse.statusCode() / 100 == 2) {
              clientResponse.handler(data -> {
                  rawData.appendBuffer(data);
                  System.out.println("Got some data " + data);
              })
              .endHandler(end -> resultHandler.handle(rawData));
              } else if (clientResponse.statusCode() == 404) {
              // Is there any way to determine this in advance?
              resultHandler.handle(rawData); // Empty
          } else {
              System.out.println("Response code was " + clientResponse.statusCode());
              System.out.println(path);
              System.out.println(clientResponse.headers().entries());
              exceptionHandler.handle(new BadResponseCodeError("Unexpected response code when trying to retrieve config: " //$NON-NLS-1$
                      + clientResponse.statusCode()));
          }
      })
      .putHeader("Accept", "application/json") // Seems to ignore this.
      .exceptionHandler(exceptionHandler)
      .end();
    }

    public AccessTokenResourceFetcher exceptionHandler(Handler<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    private String requireOpt(String key, String errorMsg) {
        Arguments.require(options.containsKey(key), errorMsg);
        return options.get(key);
    }

}
