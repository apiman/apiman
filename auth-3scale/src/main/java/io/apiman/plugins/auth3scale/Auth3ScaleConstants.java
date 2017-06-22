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
package io.apiman.plugins.auth3scale;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public final class Auth3ScaleConstants {

    public static final String AUTHORIZE_PATH = "/transactions/authorize.xml?";
    public static final String AUTHREP_PATH = "/transactions/authrep.xml?";
    public static final String REPORT_PATH = "/transactions.xml";

    public static final String USER_KEY = "user_key";
    public static final String SERVICE_ID = "service_id";
    public static final String SERVICE_TOKEN = "service_token";
    public static final String REFERRER = "referer"; // Yes, misspelt.
    public static final String USER_ID = "user_id"; // User ID is different from user_key - it is for rate limiting purposes...(?)
    public static final String LOG = "log";
    public static final String TRANSACTIONS = "transactions";

    public static final String APP_ID = "app_id";
    public static final String APP_KEY = "app_key";
    public static final String REDIRECT_URL = "redirect_url";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String CLIENT_ID = "client_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String USAGE = "usage";

    public static final String BLOCKING_FLAG = "3scale.blocking";

    private Auth3ScaleConstants() {}
}
