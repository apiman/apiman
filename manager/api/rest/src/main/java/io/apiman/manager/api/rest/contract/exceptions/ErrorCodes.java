/*
 * Copyright 2014 JBoss Inc
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

package io.apiman.manager.api.rest.contract.exceptions;


/**
 * A set of error codes used by the application when returning errors via
 * the DT REST API.
 *
 * @author eric.wittmann@redhat.com
 */
public final class ErrorCodes {

    //
    // HTTP status codes
    //
    public static final int HTTP_STATUS_CODE_INVALID_INPUT  = 400;
    public static final int HTTP_STATUS_CODE_FORBIDDEN      = 403;
    public static final int HTTP_STATUS_CODE_NOT_FOUND      = 404;
    public static final int HTTP_STATUS_CODE_ALREADY_EXISTS = 409;
    public static final int HTTP_STATUS_CODE_INVALID_STATE  = 409;
    public static final int HTTP_STATUS_CODE_SYSTEM_ERROR   = 500;


    //
    // User API related
    //
    public static final int USER_NOT_FOUND                  = 1001;

    public static final String USER_NOT_FOUND_INFO          = null;


    //
    // Role API related
    //
    public static final int ROLE_NOT_FOUND                  = 2001;
    public static final int ROLE_ALREADY_EXISTS             = 2002;

    public static final String ROLE_NOT_FOUND_INFO          = null;
    public static final String ROLE_ALREADY_EXISTS_INFO     = null;


    //
    // Organization API related
    //
    public static final int ORG_ALREADY_EXISTS              = 3001;
    public static final int ORG_NOT_FOUND                   = 3002;

    public static final String ORG_ALREADY_EXISTS_INFO      = null;
    public static final String ORG_NOT_FOUND_INFO           = null;


    //
    // Application API related
    //
    public static final int APP_ALREADY_EXISTS              = 4001;
    public static final int APP_NOT_FOUND                   = 4002;
    public static final int APP_VERSION_NOT_FOUND           = 4003;
    public static final int CONTRACT_NOT_FOUND              = 4004;
    public static final int CONTRACT_ALREADY_EXISTS         = 4005;
    public static final int APP_STATUS_ERROR                = 4006;
    public static final int APP_VERSION_ALREADY_EXISTS      = 4007;

    public static final String APP_ALREADY_EXISTS_INFO      = null;
    public static final String APP_NOT_FOUND_INFO           = null;
    public static final String APP_VERSION_NOT_FOUND_INFO   = null;
    public static final String CONTRACT_NOT_FOUND_INFO      = null;
    public static final String CONTRACT_ALREADY_EXISTS_INFO = null;
    public static final String APP_STATUS_ERROR_INFO        = null;
    public static final String APP_VERSION_ALREADY_EXISTS_INFO = null;


    //
    // Service API related
    //
    public static final int SERVICE_ALREADY_EXISTS              = 5001;
    public static final int SERVICE_NOT_FOUND                   = 5002;
    public static final int SERVICE_VERSION_NOT_FOUND           = 5003;
    public static final int SERVICE_STATUS_ERROR                = 5004;
    public static final int SERVICE_DEFINITION_NOT_FOUND        = 5005;
    public static final int SERVICE_VERSION_ALREADY_EXISTS      = 5006;

    public static final String SERVICE_ALREADY_EXISTS_INFO      = null;
    public static final String SERVICE_NOT_FOUND_INFO           = null;
    public static final String SERVICE_VERSION_NOT_FOUND_INFO   = null;
    public static final String SERVICE_STATUS_ERROR_INFO        = null;
    public static final String SERVICE_DEFINITION_NOT_FOUND_INFO   = null;
    public static final String SERVICE_VERSION_ALREADY_EXISTS_INFO      = null;


    //
    // Plan API related
    //
    public static final int PLAN_ALREADY_EXISTS              = 6001;
    public static final int PLAN_NOT_FOUND                   = 6002;
    public static final int PLAN_VERSION_NOT_FOUND           = 6003;
    public static final int PLAN_VERSION_ALREADY_EXISTS      = 6004;

    public static final String PLAN_ALREADY_EXISTS_INFO      = null;
    public static final String PLAN_NOT_FOUND_INFO           = null;
    public static final String PLAN_VERSION_NOT_FOUND_INFO   = null;
    public static final String PLAN_VERSION_ALREADY_EXISTS_INFO = null;

    //
    // Member API related
    //
    public static final int MEMBER_NOT_FOUND                  = 7001;

    public static final String MEMBER_NOT_FOUND_INFO          = null;



    //
    // Action API related
    //
    public static final int ACTION_ERROR                      = 8001;

    public static final String ACTION_ERROR_INFO              = null;


    //
    // Policy related
    //
    public static final int POLICY_NOT_FOUND                  = 9001;

    public static final String POLICY_NOT_FOUND_INFO          = null;


    //
    // Policy Definition related
    //
    public static final int POLICY_DEF_ALREADY_EXISTS              = 10001;
    public static final int POLICY_DEF_NOT_FOUND                   = 10002;
    public static final int POLICY_DEF_INVALID                     = 10003;

    public static final String POLICY_DEF_ALREADY_EXISTS_INFO      = null;
    public static final String POLICY_DEF_NOT_FOUND_INFO           = null;
    public static final String POLICY_DEF_INVALID_INFO             = null;


    //
    // Gateway related
    //
    public static final int GATEWAY_ALREADY_EXISTS              = 11001;
    public static final int GATEWAY_NOT_FOUND                   = 11002;

    public static final String GATEWAY_ALREADY_EXISTS_INFO      = null;
    public static final String GATEWAY_NOT_FOUND_INFO           = null;


    //
    // Plugin related
    //
    public static final int PLUGIN_ALREADY_EXISTS              = 12001;
    public static final int PLUGIN_NOT_FOUND                   = 12002;
    public static final int PLUGIN_RESOURCE_NOT_FOUND          = 12003;

    public static final String PLUGIN_ALREADY_EXISTS_INFO      = null;
    public static final String PLUGIN_NOT_FOUND_INFO           = null;
    public static final String PLUGIN_RESOURCE_NOT_FOUND_INFO  = null;


    //
    // Metrics related
    //
    public static final int METRIC_CRITERIA_INVALID            = 13001;

    public static final String METRIC_CRITERIA_INVALID_INFO    = null;


    //
    // General cross-cutting errors
    //
    public static final int SEARCH_CRITERIA_INVALID         = 14001;
    public static final int NAME_INVALID                    = 14002;
    public static final int VERSION_INVALID                 = 14003;

    public static final String SEARCH_CRITERIA_INVALID_INFO = null;
    public static final String NAME_INVALID_INFO            = null;
    public static final String VERSION_INVALID_INFO         = null;

}
