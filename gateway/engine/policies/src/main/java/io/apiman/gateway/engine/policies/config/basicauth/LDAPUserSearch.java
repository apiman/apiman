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
package io.apiman.gateway.engine.policies.config.basicauth;

/**
 * Models information about the user search.
 *
 * @author eric.wittmann@redhat.com
 */
public class LDAPUserSearch {

    private String baseDn;
    private String expression;

    /**
     * Constructor.
     */
    public LDAPUserSearch() {
    }

    /**
     * @return the baseDn
     */
    public String getBaseDn() {
        return baseDn;
    }

    /**
     * @param baseDn the baseDn to set
     */
    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    /**
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @param expression the expression to set
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

}
