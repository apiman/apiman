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
package io.apiman.test.common.util;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.mvel2.integration.VariableResolver;

/**
 * A json variable resolver.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("javadoc")
public class TestVariableResolver implements VariableResolver {
    
    private static final long serialVersionUID = 1L;
    
    private JsonNode node;
    private String fieldName;
    
    /**
     * Constructor.
     * @param node
     * @param fieldName
     */
    public TestVariableResolver(JsonNode node, String fieldName) {
        this.node = node;
        this.fieldName = fieldName;
    }

    /**
     * @see org.mvel2.integration.VariableResolver#getName()
     */
    @Override
    public String getName() {
        return fieldName;
    }

    /**
     * @see org.mvel2.integration.VariableResolver#getType()
     */
    @Override
    public Class<?> getType() {
        return Object.class;
    }

    /**
     * @see org.mvel2.integration.VariableResolver#setStaticType(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void setStaticType(Class type) {
    }

    /**
     * @see org.mvel2.integration.VariableResolver#getFlags()
     */
    @Override
    public int getFlags() {
        return 0;
    }

    /**
     * @see org.mvel2.integration.VariableResolver#getValue()
     */
    @Override
    public Object getValue() {
        JsonNode varNode = node.get(fieldName);
        if (varNode.isObject()) {
            return varNode;
        } else if (varNode.isArray()) {
            List<Object> rval = new ArrayList<>();
            for (int idx = 0; idx < varNode.size(); idx++) {
                JsonNode idxNode = varNode.get(idx);
                rval.add(idxNode);
            }
            return rval;
        } else if (varNode.isNull()) {
            return null;
        } else if (varNode.isBoolean()) {
            return varNode.asBoolean();
        } else if (varNode.isTextual()) {
            return varNode.asText();
        } else if (varNode.isInt()) {
            return varNode.asInt();
        } else if (varNode.isDouble()) {
            return varNode.asDouble();
        }
        return varNode;
    }

    /**
     * @see org.mvel2.integration.VariableResolver#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Object value) {
    }

}
