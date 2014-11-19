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
package org.overlord.apiman.test.common.util;

import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.impl.BaseVariableResolverFactory;

/**
 * MVEL resolver for jackson parsed json.
 * 
 * @author eric.wittmann@redhat.com
 */
public class TestVariableResolverFactory extends BaseVariableResolverFactory {

    private static final long serialVersionUID = 6176871022243088981L;
    
    JsonNode node;

    /**
     * Constructor.
     */
    public TestVariableResolverFactory(JsonNode node) {
        this.node = node;
    }

    /**
     * @see org.mvel2.integration.VariableResolverFactory#createVariable(java.lang.String, java.lang.Object)
     */
    @Override
    public VariableResolver createVariable(String name, Object value) {
        return null;
    }

    /**
     * @see org.mvel2.integration.impl.BaseVariableResolverFactory#createIndexedVariable(int, java.lang.String, java.lang.Object)
     */
    @Override
    public VariableResolver createIndexedVariable(int index, String name, Object value) {
        return null;
    }

    /**
     * @see org.mvel2.integration.VariableResolverFactory#createVariable(java.lang.String, java.lang.Object, java.lang.Class)
     */
    @Override
    public VariableResolver createVariable(String name, Object value, Class<?> type) {
        return null;
    }

    /**
     * @see org.mvel2.integration.impl.BaseVariableResolverFactory#createIndexedVariable(int, java.lang.String, java.lang.Object, java.lang.Class)
     */
    @Override
    public VariableResolver createIndexedVariable(int index, String name, Object value, Class<?> type) {
        return null;
    }

    /**
     * @see org.mvel2.integration.impl.BaseVariableResolverFactory#setIndexedVariableResolver(int, org.mvel2.integration.VariableResolver)
     */
    @Override
    public VariableResolver setIndexedVariableResolver(int index, VariableResolver variableResolver) {
        return null;
    }

    /**
     * @see org.mvel2.integration.VariableResolverFactory#isTarget(java.lang.String)
     */
    @Override
    public boolean isTarget(String name) {
        return variableResolvers.containsKey(name);
    }

    /**
     * @see org.mvel2.integration.VariableResolverFactory#isResolveable(java.lang.String)
     */
    @Override
    public boolean isResolveable(String name) {
        if (variableResolvers.containsKey(name)) {
            return true;
        }
        if (node.isObject() && node.get(name) != null) {
            variableResolvers.put(name, new TestVariableResolver(node, name));
            return true;
        }
        if (nextFactory != null) {
            return nextFactory.isResolveable(name);
        }
        return false;
    }

    /**
     * @see org.mvel2.integration.impl.BaseVariableResolverFactory#getVariableResolver(java.lang.String)
     */
    @Override
    public VariableResolver getVariableResolver(String name) {
        VariableResolver vr = variableResolvers.get(name);
        return vr != null ? vr : (nextFactory == null ? null : nextFactory.getVariableResolver(name));
    }

    /**
     * @see org.mvel2.integration.impl.BaseVariableResolverFactory#getKnownVariables()
     */
    @Override
    public Set<String> getKnownVariables() {
        return variableResolvers.keySet();
    }

    /**
     * @see org.mvel2.integration.impl.BaseVariableResolverFactory#variableIndexOf(java.lang.String)
     */
    @Override
    public int variableIndexOf(String name) {
        return 0;
    }

    /**
     * @see org.mvel2.integration.impl.BaseVariableResolverFactory#isIndexedFactory()
     */
    @Override
    public boolean isIndexedFactory() {
        return false;
    }
}
