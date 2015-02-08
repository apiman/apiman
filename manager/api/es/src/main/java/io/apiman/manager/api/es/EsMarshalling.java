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
package io.apiman.manager.api.es;

import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.GatewayType;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionTemplateBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyFormType;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHitField;

/**
 * Marshalls objects into Maps to be used in ES requests.  Also unmarshalls from
 * maps back into objects.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class EsMarshalling {

    /**
     * Marshals the given bean into the given map.
     * @param bean
     * @throws StorageException 
     */
    public static XContentBuilder marshall(GatewayBean bean) throws StorageException {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("type", bean.getType())
                    .field("configuration", bean.getConfiguration())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("modifiedBy", bean.getModifiedBy())
                    .field("modifiedOn", bean.getModifiedOn().getTime())
                .endObject();
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean
     * @throws StorageException 
     */
    public static XContentBuilder marshall(PolicyDefinitionBean bean) throws StorageException {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("form", bean.getForm())
                    .field("formType", bean.getFormType())
                    .field("icon", bean.getIcon())
                    .field("pluginId", bean.getPluginId())
                    .field("policyImpl", bean.getPolicyImpl());
            
            Set<PolicyDefinitionTemplateBean> templates = bean.getTemplates();
            if (templates != null) {
                builder.field("templates").startArray();
                for (PolicyDefinitionTemplateBean template : templates) {
                    builder.startObject();
                    builder.field("language", template.getLanguage());
                    builder.field("template", template.getTemplate());
                    builder.endObject();
                }
                builder.endArray();
            }
            
            builder.endObject();
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean
     * @throws StorageException 
     */
    public static XContentBuilder marshall(PluginBean bean) throws StorageException {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id", bean.getId())
                    .field("name", bean.getName())
                    .field("description", bean.getDescription())
                    .field("createdBy", bean.getCreatedBy())
                    .field("createdOn", bean.getCreatedOn().getTime())
                    .field("groupId", bean.getGroupId())
                    .field("artifactId", bean.getArtifactId())
                    .field("version", bean.getVersion())
                    .field("classifier", bean.getClassifier())
                    .field("type", bean.getType())
                .endObject();
            return builder;
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source
     */
    public static GatewayBean unmarshallGateway(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        GatewayBean bean = new GatewayBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setType(asEnum(source.get("type"), GatewayType.class));
        bean.setConfiguration(asString(source.get("configuration")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setModifiedBy(asString(source.get("modifiedBy")));
        bean.setModifiedOn(asDate(source.get("modifiedOn")));
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param map
     */
    public static GatewaySummaryBean unmarshallGatewaySummary(Map<String, SearchHitField> map) {
        GatewaySummaryBean bean = new GatewaySummaryBean();
        bean.setId(asString(map.get("id").value()));
        bean.setName(asString(map.get("name").value()));
        if (map.containsKey("description")) {
            bean.setDescription(asString(map.get("description").value()));
        }
        bean.setType(asEnum(map.get("type").value(), GatewayType.class));
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source
     */
    public static PolicyDefinitionBean unmarshallPolicyDefinition(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        PolicyDefinitionBean bean = new PolicyDefinitionBean();
        bean.setId(asString(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setForm(asString(source.get("form")));
        bean.setFormType(asEnum(source.get("formType"), PolicyFormType.class));
        bean.setIcon(asString(source.get("icon")));
        bean.setPluginId(asLong(source.get("pluginId")));
        bean.setPolicyImpl(asString(source.get("policyImpl")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> templates = (List<Map<String, Object>>) source.get("templates");
        if (templates != null && !templates.isEmpty()) {
            bean.setTemplates(new HashSet<PolicyDefinitionTemplateBean>());
            for (Map<String, Object> templateMap : templates) {
                PolicyDefinitionTemplateBean template = new PolicyDefinitionTemplateBean();
                template.setLanguage(asString(templateMap.get("language")));
                template.setTemplate(asString(templateMap.get("template")));
                bean.getTemplates().add(template);
            }
        }
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param map
     */
    public static PolicyDefinitionSummaryBean unmarshallPolicyDefinitionSummary(Map<String, SearchHitField> map) {
        PolicyDefinitionSummaryBean bean = new PolicyDefinitionSummaryBean();
        bean.setId(asString(map.get("id").value()));
        bean.setName(asString(map.get("name").value()));
        if (map.containsKey("description")) {
            bean.setDescription(asString(map.get("description").value()));
        }
        bean.setPolicyImpl(asString(map.get("policyImpl").value()));
        if (map.containsKey("icon")) {
            bean.setIcon(asString(map.get("icon").value()));
        }
        if (map.containsKey("pluginId")) {
            bean.setPluginId(asLong(map.get("pluginId").value()));
        }
        if (map.containsKey("formType")) {
            bean.setFormType(asEnum(map.get("formType").value(), PolicyFormType.class));
        }

        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source
     */
    public static PluginBean unmarshallPlugin(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        PluginBean bean = new PluginBean();
        bean.setId(asLong(source.get("id")));
        bean.setName(asString(source.get("name")));
        bean.setDescription(asString(source.get("description")));
        bean.setCreatedBy(asString(source.get("createdBy")));
        bean.setCreatedOn(asDate(source.get("createdOn")));
        bean.setGroupId(asString(source.get("groupId")));
        bean.setArtifactId(asString(source.get("artifactId")));
        bean.setVersion(asString(source.get("version")));
        bean.setType(asString(source.get("type")));
        bean.setClassifier(asString(source.get("classifier")));
        return bean;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param map
     */
    public static PluginSummaryBean unmarshallPluginSummary(Map<String, SearchHitField> map) {
        PluginSummaryBean bean = new PluginSummaryBean();
        bean.setId(asLong(map.get("id").value()));
        bean.setName(asString(map.get("name").value()));
        if (map.containsKey("description")) {
            bean.setDescription(asString(map.get("description").value()));
        }
        bean.setGroupId(asString(map.get("groupId").value()));
        bean.setArtifactId(asString(map.get("artifactId").value()));
        bean.setVersion(asString(map.get("version").value()));
        if (map.containsKey("type")) {
            bean.setType(asString(map.get("type").value()));
        }
        if (map.containsKey("classifier")) {
            bean.setClassifier(asString(map.get("classifier").value()));
        }
        bean.setCreatedBy(asString(map.get("createdBy").value()));
        bean.setCreatedOn(asDate(map.get("createdOn").value()));
        
        return bean;
    }

    /**
     * @param object
     */
    private static String asString(Object object) {
        if (object == null) {
            return null;
        }
        return String.valueOf(object);
    }

    /**
     * @param object
     */
    private static Long asLong(Object object) {
        if (object == null) {
            return null;
        }
        Number n = (Number) object;
        return n.longValue();
    }

    /**
     * @param object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> T asEnum(Object object, Class<T> type) {
        if (object == null) {
            return null;
        }
        return (T) Enum.valueOf((Class) type, String.valueOf(object));
    }

    /**
     * @param object
     */
    private static Date asDate(Object object) {
        if (object == null) {
            return null;
        }
        Number n = (Number) object;
        return new Date(n.longValue());
    }

}
