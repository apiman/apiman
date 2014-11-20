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
package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionTemplateBean;
import io.apiman.manager.api.core.util.PolicyTemplateUtil;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.overlord.commons.i18n.AbstractMessages;

/**
 * Unit test for {@link PolicyTemplateUtil}.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyTemplateUtilTest {

    /**
     * Test method for {@link io.apiman.manager.api.core.util.PolicyTemplateUtil#generatePolicyDescription(io.apiman.manager.api.beans.policies.PolicyBean)}.
     */
    @Test
    public void testGeneratePolicyDescription_novars() throws Exception {
        PolicyBean policy = new PolicyBean();
        PolicyDefinitionBean def = new PolicyDefinitionBean();
        def.setId("novars"); //$NON-NLS-1$
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setTemplate("Hello World"); //$NON-NLS-1$
        def.getTemplates().add(template);
        policy.setDefinition(def);
        policy.setConfiguration("{}"); //$NON-NLS-1$
        PolicyTemplateUtil.generatePolicyDescription(policy);
        Assert.assertEquals("Hello World", policy.getDescription()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link io.apiman.manager.api.core.util.PolicyTemplateUtil#generatePolicyDescription(io.apiman.manager.api.beans.policies.PolicyBean)}.
     */
    @Test
    public void testGeneratePolicyDescription_simplevar() throws Exception {
        PolicyBean policy = new PolicyBean();
        PolicyDefinitionBean def = new PolicyDefinitionBean();
        def.setId("simplevar"); //$NON-NLS-1$
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setTemplate("@{message}"); //$NON-NLS-1$
        def.getTemplates().add(template);
        policy.setDefinition(def);
        policy.setConfiguration("{ \"message\" : \"Hello World\" }"); //$NON-NLS-1$
        PolicyTemplateUtil.generatePolicyDescription(policy);
        Assert.assertEquals("Hello World", policy.getDescription()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link io.apiman.manager.api.core.util.PolicyTemplateUtil#generatePolicyDescription(io.apiman.manager.api.beans.policies.PolicyBean)}.
     */
    @Test
    public void testGeneratePolicyDescription_nested() throws Exception {
        PolicyBean policy = new PolicyBean();
        PolicyDefinitionBean def = new PolicyDefinitionBean();
        def.setId("nested"); //$NON-NLS-1$
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setTemplate("@{messages.messageOne}"); //$NON-NLS-1$
        def.getTemplates().add(template);
        policy.setDefinition(def);
        policy.setConfiguration("{ \"messages\" : { \"messageOne\" : \"Hello World\" } }"); //$NON-NLS-1$
        PolicyTemplateUtil.generatePolicyDescription(policy);
        Assert.assertEquals("Hello World", policy.getDescription()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link io.apiman.manager.api.core.util.PolicyTemplateUtil#generatePolicyDescription(io.apiman.manager.api.beans.policies.PolicyBean)}.
     */
    @Test
    public void testGeneratePolicyDescription_arrays() throws Exception {
        PolicyBean policy = new PolicyBean();
        PolicyDefinitionBean def = new PolicyDefinitionBean();
        def.setId("arrays"); //$NON-NLS-1$
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setTemplate("@{messages[0]} @{messages[1]}"); //$NON-NLS-1$
        def.getTemplates().add(template);
        policy.setDefinition(def);
        policy.setConfiguration("{ \"messages\" : [ \"Hello\", \"World\" ] }"); //$NON-NLS-1$
        PolicyTemplateUtil.generatePolicyDescription(policy);
        Assert.assertEquals("Hello World", policy.getDescription()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link io.apiman.manager.api.core.util.PolicyTemplateUtil#generatePolicyDescription(io.apiman.manager.api.beans.policies.PolicyBean)}.
     */
    @Test
    public void testGeneratePolicyDescription_nested2() throws Exception {
        PolicyBean policy = new PolicyBean();
        PolicyDefinitionBean def = new PolicyDefinitionBean();
        def.setId("nested2"); //$NON-NLS-1$
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setTemplate("@{messages.index[0].value} @{messages.index[1].value}"); //$NON-NLS-1$
        def.getTemplates().add(template);
        policy.setDefinition(def);
        policy.setConfiguration("{ \"messages\" : { \"index\" : [ { \"id\" : \"first\", \"value\" : \"Hello\" }, { \"id\" : \"second\", \"value\" : \"World\" } ] } }"); //$NON-NLS-1$
        PolicyTemplateUtil.generatePolicyDescription(policy);
        Assert.assertEquals("Hello World", policy.getDescription()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link io.apiman.manager.api.core.util.PolicyTemplateUtil#generatePolicyDescription(io.apiman.manager.api.beans.policies.PolicyBean)}.
     */
    @Test
    public void testGeneratePolicyDescription_missingvar() throws Exception {
        PolicyBean policy = new PolicyBean();
        PolicyDefinitionBean def = new PolicyDefinitionBean();
        def.setId("missingvar"); //$NON-NLS-1$
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setTemplate("Templates includes a missing var: @{missingVar}"); //$NON-NLS-1$
        def.getTemplates().add(template);
        policy.setDefinition(def);
        policy.setConfiguration("{}"); //$NON-NLS-1$
        PolicyTemplateUtil.generatePolicyDescription(policy);
        Assert.assertEquals("Templates includes a missing var: @{missingVar}", policy.getDescription()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link io.apiman.manager.api.core.util.PolicyTemplateUtil#generatePolicyDescription(io.apiman.manager.api.beans.policies.PolicyBean)}.
     */
    @Test
    public void testGeneratePolicyDescription_blacklist() throws Exception {
        PolicyBean policy = new PolicyBean();
        PolicyDefinitionBean def = new PolicyDefinitionBean();
        def.setId("blacklist"); //$NON-NLS-1$
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setTemplate("Requests that originate from the set of @{ipList.size()} configured IP address(es) will be denied access to the managed service."); //$NON-NLS-1$
        def.getTemplates().add(template);
        policy.setDefinition(def);
        policy.setConfiguration("{ \"ipList\" : [ \"127.0.0.1\", \"192.168.1.10\" ] }"); //$NON-NLS-1$
        PolicyTemplateUtil.generatePolicyDescription(policy);
        Assert.assertEquals("Requests that originate from the set of 2 configured IP address(es) will be denied access to the managed service.", policy.getDescription()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link io.apiman.manager.api.core.util.PolicyTemplateUtil#generatePolicyDescription(io.apiman.manager.api.beans.policies.PolicyBean)}.
     */
    @Test
    public void testGeneratePolicyDescription_basicauth() throws Exception {
        PolicyBean policy = new PolicyBean();
        PolicyDefinitionBean def = new PolicyDefinitionBean();
        def.setId("basicauth"); //$NON-NLS-1$
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setTemplate("Access to the service is protected by BASIC Authentication through the @{realm} authentication realm.  @if{forwardIdentityHttpHeader != null}Successfully authenticated requests will include '@{forwardIdentityHttpHeader}' as a custom HTTP header to the back end service.@end{}"); //$NON-NLS-1$
        def.getTemplates().add(template);
        policy.setDefinition(def);
        policy.setConfiguration("{ \"realm\" : \"Example\", \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\" }"); //$NON-NLS-1$
        PolicyTemplateUtil.generatePolicyDescription(policy);
        Assert.assertEquals("Access to the service is protected by BASIC Authentication through the Example authentication realm.  Successfully authenticated requests will include 'X-Authenticated-Identity' as a custom HTTP header to the back end service.", policy.getDescription()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link io.apiman.manager.api.core.util.PolicyTemplateUtil#generatePolicyDescription(io.apiman.manager.api.beans.policies.PolicyBean)}.
     */
    @Test
    public void testGeneratePolicyDescription_i18n() throws Exception {
        PolicyBean policy = new PolicyBean();
        PolicyDefinitionBean def = new PolicyDefinitionBean();
        def.setId("i18n"); //$NON-NLS-1$
        
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setLanguage(null);
        template.setTemplate("Default language message."); //$NON-NLS-1$
        def.getTemplates().add(template);
        template = new PolicyDefinitionTemplateBean();
        template.setLanguage("en"); //$NON-NLS-1$
        template.setTemplate("English language message."); //$NON-NLS-1$
        def.getTemplates().add(template);
        template = new PolicyDefinitionTemplateBean();
        template.setLanguage("en_US"); //$NON-NLS-1$
        template.setTemplate("English (US) language message."); //$NON-NLS-1$
        def.getTemplates().add(template);

        policy.setDefinition(def);
        policy.setConfiguration("{}"); //$NON-NLS-1$
        
        try {
            AbstractMessages.setLocale(Locale.ENGLISH);
            PolicyTemplateUtil.generatePolicyDescription(policy);
            Assert.assertEquals("English language message.", policy.getDescription()); //$NON-NLS-1$
            
            AbstractMessages.setLocale(Locale.US);
            PolicyTemplateUtil.generatePolicyDescription(policy);
            Assert.assertEquals("English (US) language message.", policy.getDescription()); //$NON-NLS-1$
        } finally {
            AbstractMessages.clearLocale();
        }
    }

}
