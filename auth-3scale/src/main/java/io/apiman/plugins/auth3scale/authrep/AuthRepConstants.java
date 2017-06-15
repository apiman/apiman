/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.plugins.auth3scale.authrep;

import java.net.URI;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public interface AuthRepConstants {
    String DEFAULT_BACKEND = "http://su1.3scale.net:80";
    String AUTHORIZE_PATH = "/transactions/authorize.xml?";
    String AUTHREP_PATH = "/transactions/authrep.xml?";
    String REPORT_PATH = "/transactions.xml";
    URI REPORT_URI = URI.create(DEFAULT_BACKEND+REPORT_PATH);

    String USER_KEY = "user_key";
    String SERVICE_ID = "service_id";
    String SERVICE_TOKEN = "service_token";
    String REFERRER = "referer"; // Yes, misspelt.
    String USER_ID = "user_id"; // User ID is different from user_key - it is for rate limiting purposes...(?)
    String LOG = "log";
    String TRANSACTIONS = "transactions";

    String APP_ID = "app_id";
    String APP_KEY = "app_key";
    String REDIRECT_URL = "redirect_url";
    String REDIRECT_URI = "redirect_uri";
    String ACCESS_TOKEN = "access_token";
    String CLIENT_ID = "client_id";
    String TIMESTAMP = "timestamp";
    String USAGE = "usage";

    String BLOCKING_FLAG = "3scale.blocking";
}
