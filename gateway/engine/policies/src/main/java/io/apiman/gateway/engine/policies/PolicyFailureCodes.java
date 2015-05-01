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
package io.apiman.gateway.engine.policies;

/**
 * An index of all standard apiman policy failure codes.
 *
 * @author eric.wittmann@redhat.com
 */
public final class PolicyFailureCodes {

    public static final int IP_NOT_WHITELISTED         = 10001;
    public static final int IP_BLACKLISTED             = 10002;
    public static final int BASIC_AUTH_FAILED          = 10003;
    public static final int BASIC_AUTH_REQUIRED        = 10004;
    public static final int RATE_LIMIT_EXCEEDED        = 10005;
    public static final int NO_USER_FOR_RATE_LIMITING  = 10006;
    public static final int PATHS_TO_IGNORE            = 10007;
    public static final int NO_APP_FOR_RATE_LIMITING   = 10008;
    public static final int USER_NOT_AUTHORIZED        = 10009;

}
