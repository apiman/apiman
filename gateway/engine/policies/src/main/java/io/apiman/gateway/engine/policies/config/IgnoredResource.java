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
package io.apiman.gateway.engine.policies.config;

/**
 * A single ignored resource rule consisting of a verb (GET,POST etc.) and path
 * pattern
 *
 * @author wtr@redhat.com
 */
public class IgnoredResource {

    private String verb;
    private String pathPattern;

    /**
     * Used to match all possible http verbs.
     */
    public final static String VERB_MATCH_ALL = "*";

    /**
     * Constructor.
     */
    public IgnoredResource() {
    }

    /**
     * @return the verb
     */
    public String getVerb() {
        return verb;
    }

    /**
     * @param verb
     *            the verb to set
     */
    public void setVerb(String verb) {
        this.verb = verb;
    }

    /**
     * @return the pathPattern
     */
    public String getPathPattern() {
        return pathPattern;
    }

    /**
     * @param pathPattern
     *            the pathPattern to set
     */
    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        IgnoredResource rule2 = (IgnoredResource) obj;
        return this.getVerb().equals(rule2.getVerb()) && this.getPathPattern().equals(rule2.getPathPattern());
    }

}
