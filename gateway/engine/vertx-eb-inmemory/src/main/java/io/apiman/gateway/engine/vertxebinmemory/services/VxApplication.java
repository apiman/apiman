/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.gateway.engine.vertxebinmemory.services;

import io.apiman.gateway.engine.beans.Application;
import io.vertx.core.json.Json;

/**
 * An {@link io.apiman.gateway.engine.beans.Application} with a {@link Head}.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class VxApplication implements Head {
    private Application application;
    private String action;
    private String uuid;

    public VxApplication(Application app, String action, String uuid) {
        this.application = app;
        this.action = action;
        this.uuid = uuid;
    }

    public Application getApplication() {
        return application;
    }

    @Override
    public String type() {
        return "application";
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public String body() {
        return Json.encode(application);
    }

    @Override
    public String uuid() {
        return uuid;
    }
}
