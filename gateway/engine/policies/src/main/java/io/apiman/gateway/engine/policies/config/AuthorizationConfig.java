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
package io.apiman.gateway.engine.policies.config;

import java.util.ArrayList;
import java.util.List;


/**
 * Configuration object for the Authorization policy.
 */
public class AuthorizationConfig {

    private List<AuthorizationRule> rules = new ArrayList<>();

    /**
     * Constructor.
     */
    public AuthorizationConfig() {
    }

    /**
     * @return the rules
     */
    public List<AuthorizationRule> getRules() {
        return rules;
    }

    /**
     * @param rules the rules to set
     */
    public void setRules(List<AuthorizationRule> rules) {
        this.rules = rules;
    }

}
