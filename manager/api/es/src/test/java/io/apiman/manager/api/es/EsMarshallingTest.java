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

package io.apiman.manager.api.es;

import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.developers.DeveloperBean;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionTemplateBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.PolicyFormType;
import io.apiman.manager.api.beans.system.MetadataBean;
import io.apiman.manager.api.es.beans.ApiDefinitionBean;
import io.apiman.manager.api.es.beans.PoliciesBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class EsMarshallingTest {

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.es.beans.PoliciesBean)}.
     */
    @Test
    public void testMarshallPoliciesBean() throws Exception {
        PoliciesBean bean = new PoliciesBean();
        bean.setEntityId("ENTITY_ID");
        bean.setOrganizationId("ORG_ID");
        bean.setEntityVersion("VERSION");
        bean.setType(PolicyType.Api);

        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"organizationId\":\"ORG_ID\",\"entityId\":\"ENTITY_ID\",\"entityVersion\":\"VERSION\",\"type\":\"Api\"}", Strings.toString(builder));

        PolicyBean policy = new PolicyBean();
        policy.setCreatedBy("CREATED_BY");
        policy.setCreatedOn(new Date(1));
        policy.setConfiguration("CONFIGURATION");
        policy.setDescription("DESCRIPTION HERE.");
        policy.setEntityId("ENTITY_ID");
        policy.setEntityVersion("ENTITY_VERSION");
        policy.setId(17L);
        policy.setModifiedBy("MODIFIED_BY");
        policy.setModifiedOn(new Date(2));
        policy.setName("NAME");
        policy.setOrderIndex(9);
        policy.setOrganizationId("ORG_ID");
        policy.setType(PolicyType.Api);
        policy.setDefinition(new PolicyDefinitionBean());
        policy.getDefinition().setDeleted(false);
        policy.getDefinition().setDescription("POLICY_DEF_DESCRIPTION");
        policy.getDefinition().setForm("FORM");
        policy.getDefinition().setFormType(PolicyFormType.JsonSchema);
        policy.getDefinition().setIcon("ICON");
        policy.getDefinition().setId("POLICY_DEF_ID");
        policy.getDefinition().setName("POLICY DEF NAME");
        policy.getDefinition().setPluginId(27L);
        policy.getDefinition().setPolicyImpl("POLICY_IMPL");
        PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
        template.setTemplate("TEMPLATE");
        template.setLanguage("EN_US");
        policy.getDefinition().getTemplates().add(template );
        bean.getPolicies().add(policy);

        builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"organizationId\":\"ORG_ID\",\"entityId\":\"ENTITY_ID\",\"entityVersion\":\"VERSION\",\"type\":\"Api\",\"policies\":["
                + "{\"id\":17,\"name\":\"NAME\",\"configuration\":\"CONFIGURATION\",\"createdBy\":\"CREATED_BY\",\"createdOn\":1,\"modifiedBy\":\"MODIFIED_BY\",\"modifiedOn\":2,\"definitionId\":\"POLICY_DEF_ID\",\"orderIndex\":9}]}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.gateways.GatewayBean)}.
     */
    @Test
    public void testMarshallGatewayBean() throws Exception {
        GatewayBean bean = createBean(GatewayBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":\"ID\",\"name\":\"NAME\",\"description\":\"DESCRIPTION\",\"type\":\"REST\",\"configuration\":\"CONFIGURATION\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1,\"modifiedBy\":\"MODIFIEDBY\",\"modifiedOn\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.es.beans.ApiDefinitionBean)}.
     */
    @Test
    public void testMarshallApiDefinitionBean() throws Exception {
        ApiDefinitionBean bean = new ApiDefinitionBean();
        bean.setData("DATA");
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"data\":\"DATA\"}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.contracts.ContractBean)}.
     */
    @Test
    public void testMarshallContractBean() throws Exception {
        ContractBean bean = createBean(ContractBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":17,\"clientOrganizationId\":\"ID\",\"clientOrganizationName\":\"NAME\",\"clientId\":\"ID\",\"clientName\":\"NAME\",\"clientVersion\":\"VERSION\",\"apiOrganizationId\":\"ID\",\"apiOrganizationName\":\"NAME\",\"apiId\":\"ID\",\"apiName\":\"NAME\",\"apiVersion\":\"VERSION\",\"apiDescription\":\"DESCRIPTION\",\"planName\":\"NAME\",\"planId\":\"ID\",\"planVersion\":\"VERSION\",\"createdOn\":1,\"createdBy\":\"CREATEDBY\"}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.plans.PlanBean)}.
     */
    @Test
    public void testMarshallPlanBean() throws Exception {
        PlanBean bean = createBean(PlanBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"organizationId\":\"ID\",\"organizationName\":\"NAME\",\"id\":\"ID\",\"name\":\"NAME\",\"description\":\"DESCRIPTION\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.plans.PlanVersionBean)}.
     */
    @Test
    public void testMarshallPlanVersionBean() throws Exception {
        PlanVersionBean bean = createBean(PlanVersionBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"organizationId\":\"ID\",\"organizationName\":\"NAME\",\"planId\":\"ID\",\"planName\":\"NAME\",\"planDescription\":\"DESCRIPTION\",\"version\":\"VERSION\",\"status\":\"Created\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1,\"modifiedBy\":\"MODIFIEDBY\",\"modifiedOn\":1,\"lockedOn\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.apis.ApiBean)}.
     */
    @Test
    public void testMarshallApiBean() throws Exception {
        ApiBean bean = createBean(ApiBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"organizationId\":\"ID\",\"organizationName\":\"NAME\",\"id\":\"ID\",\"name\":\"NAME\",\"description\":\"DESCRIPTION\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1,\"numPublished\":11}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.apis.ApiVersionBean)}.
     */
    @Test
    public void testMarshallApiVersionBean() throws Exception {
        ApiVersionBean bean = createBean(ApiVersionBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"organizationId\":\"ID\",\"organizationName\":\"NAME\",\"apiId\":\"ID\",\"apiName\":\"NAME\",\"apiDescription\":\"DESCRIPTION\",\"version\":\"VERSION\",\"status\":\"Created\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1,\"modifiedBy\":\"MODIFIEDBY\",\"modifiedOn\":1,\"publishedOn\":1,\"retiredOn\":1,\"publicAPI\":true,\"endpoint\":\"ENDPOINT\",\"endpointType\":\"rest\",\"endpointContentType\":\"json\",\"parsePayload\":true,\"disableKeysStrip\":true,\"definitionType\":\"None\",\"definitionUrl\":\"DEFINITIONURL\",\"gateways\":[{\"gatewayId\":\"GATEWAYID\"}],\"plans\":[{\"planId\":\"PLANID\",\"version\":\"VERSION\"}],\"endpointProperties\":{\"KEY-1\":\"VALUE-1\",\"KEY-2\":\"VALUE-2\"}}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.clients.ClientBean)}.
     */
    @Test
    public void testMarshallClientBean() throws Exception {
        ClientBean bean = createBean(ClientBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"organizationId\":\"ID\",\"organizationName\":\"NAME\",\"id\":\"ID\",\"name\":\"NAME\",\"description\":\"DESCRIPTION\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.clients.ClientVersionBean)}.
     */
    @Test
    public void testMarshallClientVersionBean() throws Exception {
        ClientVersionBean bean = createBean(ClientVersionBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"organizationId\":\"ID\",\"organizationName\":\"NAME\",\"clientId\":\"ID\",\"clientName\":\"NAME\",\"clientDescription\":\"DESCRIPTION\",\"version\":\"VERSION\",\"apikey\":\"APIKEY\",\"status\":\"Created\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1,\"modifiedBy\":\"MODIFIEDBY\",\"modifiedOn\":1,\"publishedOn\":1,\"retiredOn\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.audit.AuditEntryBean)}.
     */
    @Test
    public void testMarshallAuditEntryBean() throws Exception {
        AuditEntryBean bean = createBean(AuditEntryBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":17,\"organizationId\":\"ORGANIZATIONID\",\"entityId\":\"ENTITYID\",\"entityType\":\"Organization\",\"entityVersion\":\"ENTITYVERSION\",\"data\":\"DATA\",\"who\":\"WHO\",\"what\":\"Create\",\"createdOn\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.orgs.OrganizationBean)}.
     */
    @Test
    public void testMarshallOrganizationBean() throws Exception {
        OrganizationBean bean = createBean(OrganizationBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":\"ID\",\"name\":\"NAME\",\"description\":\"DESCRIPTION\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1,\"modifiedBy\":\"MODIFIEDBY\",\"modifiedOn\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.idm.RoleMembershipBean)}.
     */
    @Test
    public void testMarshallRoleMembershipBean() throws Exception {
        RoleMembershipBean bean = createBean(RoleMembershipBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":17,\"organizationId\":\"ORGANIZATIONID\",\"roleId\":\"ROLEID\",\"userId\":\"USERID\",\"createdOn\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.idm.UserBean)}.
     */
    @Test
    public void testMarshallUserBean() throws Exception {
        UserBean bean = createBean(UserBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"username\":\"USERNAME\",\"email\":\"EMAIL\",\"fullName\":\"FULLNAME\",\"joinedOn\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.idm.RoleBean)}.
     */
    @Test
    public void testMarshallRoleBean() throws Exception {
        RoleBean bean = createBean(RoleBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":\"ID\",\"name\":\"NAME\",\"description\":\"DESCRIPTION\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1,\"autoGrant\":true,\"permissions\":[\"orgView\",\"orgEdit\"]}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)}.
     */
    @Test
    public void testMarshallPolicyDefinitionBean() throws Exception {
        PolicyDefinitionBean bean = createBean(PolicyDefinitionBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":\"ID\",\"name\":\"NAME\",\"description\":\"DESCRIPTION\",\"form\":\"FORM\",\"formType\":\"Default\",\"icon\":\"ICON\",\"pluginId\":17,\"policyImpl\":\"POLICYIMPL\",\"deleted\":false,\"templates\":[{\"language\":\"LANGUAGE\",\"template\":\"TEMPLATE\"},{\"language\":\"LANGUAGE\",\"template\":\"TEMPLATE\"}]}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.plugins.PluginBean)}.
     */
    @Test
    public void testMarshallPluginBean() throws Exception {
        PluginBean bean = createBean(PluginBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":17,\"name\":\"NAME\",\"description\":\"DESCRIPTION\",\"createdBy\":\"CREATEDBY\",\"createdOn\":1,\"groupId\":\"GROUPID\",\"artifactId\":\"ARTIFACTID\",\"version\":\"VERSION\",\"classifier\":\"CLASSIFIER\",\"type\":\"TYPE\",\"deleted\":false}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.download.DownloadBean)}.
     */
    @Test
    public void testMarshallDownloadBean() throws Exception {
        DownloadBean bean = createBean(DownloadBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":\"ID\",\"type\":\"exportJson\",\"path\":\"PATH\",\"expires\":1}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(DeveloperBean)}
     */
    @Test
    public void testMarshallDeveloperBean() throws Exception {
        DeveloperBean bean = createBean(DeveloperBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":\"ID\",\"clients\":[{\"clientId\":\"CLIENTID\",\"organizationId\":\"ORGANIZATIONID\"},{\"clientId\":\"CLIENTID\",\"organizationId\":\"ORGANIZATIONID\"}]}", Strings.toString(builder));
    }

    /**
     * Test method for {@link io.apiman.manager.api.es.EsMarshalling#marshall(io.apiman.manager.api.beans.system.MetadataBean)}
     */
    @Test
    public void testMarshallMetadataBean() throws Exception {
        MetadataBean bean = createBean(MetadataBean.class);
        XContentBuilder builder = EsMarshalling.marshall(bean);
        Assert.assertEquals("{\"id\":17,\"exportedOn\":1,\"apimanVersion\":\"APIMANVERSION\",\"importedOn\":1,\"apimanVersionAtImport\":\"APIMANVERSIONATIMPORT\",\"success\":true}", Strings.toString(builder));
    }


    /**
     * Fabricates a new instance of the given bean type.  Uses reflection to figure
     * out all the fields and assign generated values for each.
     */
    private static <T> T createBean(Class<T> beanClass) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        T bean = beanClass.newInstance();
        Map<String, String> beanProps = BeanUtils.describe(bean);
        for (String key : beanProps.keySet()) {
            try {
                Field declaredField = beanClass.getDeclaredField(key);
                Class<?> fieldType = declaredField.getType();
                if (fieldType == String.class ) {
                    BeanUtils.setProperty(bean, key, StringUtils.upperCase(key));
                } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                    BeanUtils.setProperty(bean, key, Boolean.TRUE);
                } else if (fieldType == Date.class) {
                    BeanUtils.setProperty(bean, key, new Date(1));
                } else if (fieldType == Long.class || fieldType == long.class) {
                    BeanUtils.setProperty(bean, key, 17L);
                } else if (fieldType == Integer.class || fieldType == long.class) {
                    BeanUtils.setProperty(bean, key, 11);
                } else if (fieldType == Set.class ) {
                    // Initialize to a linked hash set so that order is maintained.
                    BeanUtils.setProperty(bean, key, new LinkedHashSet());

                    Type genericType = declaredField.getGenericType();
                    String typeName = genericType.getTypeName();
                    String typeClassName = typeName.substring(14, typeName.length() - 1);
                    Class<?> typeClass = Class.forName(typeClassName);
                    Set collection = (Set) BeanUtilsBean.getInstance().getPropertyUtils().getProperty(bean, key);
                    populateSet(collection, typeClass);
                } else if (fieldType == Map.class ) {
                    Map<String, String> map = new LinkedHashMap<String, String>();
                    map.put("KEY-1", "VALUE-1");
                    map.put("KEY-2", "VALUE-2");
                    BeanUtils.setProperty(bean, key, map);
                } else if (fieldType.isEnum()) {
                    BeanUtils.setProperty(bean, key, fieldType.getEnumConstants()[0]);
                } else if (fieldType.getPackage() != null && fieldType.getPackage().getName().startsWith("io.apiman.manager.api.beans")) {
                    Object childBean = createBean(fieldType);
                    BeanUtils.setProperty(bean, key, childBean);
                } else {
                    throw new IllegalAccessException("Failed to handle property named [" + key + "] type: " + fieldType.getSimpleName());
                }
//            String capKey = StringUtils.capitalize(key);
//            System.out.println(key);;
            } catch (NoSuchFieldException e) {
                // Skip it - there is not really a bean property with this name!
            }
        }
        return bean;
    }

    /**
     * Populate the given set with one or two items of the given type.
     * @param collection
     * @param typeClass
     */
    private static void populateSet(Set collection, Class<?> typeClass)
            throws IllegalAccessException, InstantiationException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        if (typeClass.isEnum()) {
            collection.add(typeClass.getEnumConstants()[0]);
            collection.add(typeClass.getEnumConstants()[1]);
        } else if (typeClass == String.class ) {
            collection.add("VALUE_1");
            collection.add("VALUE_2");
        } else if (typeClass.getPackage().getName().startsWith("io.apiman.manager.api.beans")) {
            Object bean1 = createBean(typeClass);
            Object bean2 = createBean(typeClass);
            collection.add(bean1);
            collection.add(bean2);
        } else {
            throw new IllegalAccessException("Failed to populate Set of type: " + typeClass.getSimpleName());
        }
    }

}
