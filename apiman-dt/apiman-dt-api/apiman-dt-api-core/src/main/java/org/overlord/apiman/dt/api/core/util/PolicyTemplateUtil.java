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
package org.overlord.apiman.dt.api.core.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionTemplateBean;
import org.overlord.apiman.dt.api.core.i18n.Messages;

/**
 * Utility for dealing with policy templates.  Policy descriptions are
 * generated dynamically using MVEL 2.0 templates configured on the policy
 * definition.  This utility applies those templates to policy instances
 * using the policy instance's configuration.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyTemplateUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    // Cache a MVEL 2.0 compiled template - the key is PolicyDefId::language
    private static final Map<String, CompiledTemplate> templateCache = new HashMap<String, CompiledTemplate>();
    
    /**
     * Clears out the template cache.
     */
    public static void clearCache() {
        templateCache.clear();
    }

    /**
     * Generates a dynamic description for the given policy and stores the
     * result on the policy bean instance.  This should be done prior
     * to returning the policybean back to the user for a REST call to the
     * management API.
     * @param policy
     */
    public static void generatePolicyDescription(PolicyBean policy) throws Exception {
        PolicyDefinitionBean def = policy.getDefinition();
        PolicyDefinitionTemplateBean templateBean = getTemplateBean(def);
        if (templateBean == null) {
            return;
        }
        String cacheKey = def.getId() + "::" + templateBean.getLanguage(); //$NON-NLS-1$
        CompiledTemplate template = templateCache.get(cacheKey);
        if (template == null) {
            template = TemplateCompiler.compileTemplate(templateBean.getTemplate());
            templateCache.put(cacheKey, template);
        }
        try {
            String jsonConfig = policy.getConfiguration();
            @SuppressWarnings("unchecked")
            Map<String, Object> configMap = mapper.readValue(jsonConfig, Map.class);
            String desc = (String) TemplateRuntime.execute(template, configMap);
            policy.setDescription(desc);
        } catch (Exception e) {
            // TODO log the error
            policy.setDescription(templateBean.getTemplate());
        }
    }

    /**
     * Determines the appropriate template bean to use given the current locale.
     * @param def
     */
    private static PolicyDefinitionTemplateBean getTemplateBean(PolicyDefinitionBean def) {
        Locale currentLocale = Messages.i18n.getLocale();
        String lang = currentLocale.getLanguage();
        String country = lang + "_" + currentLocale.getCountry(); //$NON-NLS-1$
        
        PolicyDefinitionTemplateBean nullBean = null;
        PolicyDefinitionTemplateBean langBean = null;
        PolicyDefinitionTemplateBean countryBean = null;
        for (PolicyDefinitionTemplateBean pdtb : def.getTemplates()) {
            if (pdtb.getLanguage() == null) {
                nullBean = pdtb;
            } else if (pdtb.getLanguage().equals(country)) {
                countryBean = pdtb;
                break;
            } else if (pdtb.getLanguage().equals(lang)) {
                langBean = pdtb;
            }
        }
        if (countryBean != null) {
            return countryBean;
        }
        if (langBean != null) {
            return langBean;
        }
        if (nullBean != null) {
            return nullBean;
        }
        return null;
    }
}
