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
package io.apiman.gateway.engine.es;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

/**
 * Helper class to marshal objects to JSON and back again.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESRegistryMarshalling {

    /**
     * Marshals the given bean into the given map.
     * @param bean the api bean
     * @return the content builder
     * @throws Exception when json marshalling fails
     */
    public static XContentBuilder marshall(Api bean) throws Exception {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        marshallInto(bean, builder);
        return builder;
    }

    /**
     * @param bean
     * @param builder
     * @throws IOException
     */
    @SuppressWarnings("nls")
    protected static void marshallInto(Api bean, XContentBuilder builder) throws IOException {
        builder.startObject()
            .field("endpoint", bean.getEndpoint())
            .field("endpointType", bean.getEndpointType())
            .field("endpointContentType", bean.getEndpointContentType())
            .field("publicAPI", bean.isPublicAPI())
            .field("organizationId", bean.getOrganizationId())
            .field("apiId", bean.getApiId())
            .field("version", bean.getVersion());
        Map<String, String> endpointProperties = bean.getEndpointProperties();
        if (endpointProperties != null) {
            builder.startArray("endpointProperties");
            for (Entry<String, String> entry : endpointProperties.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                builder.startObject().field(key, val).endObject();
            }
            builder.endArray();
        }
        List<Policy> policies = bean.getApiPolicies();
        if (policies != null) {
            builder.startArray("policies");
            for (Policy policy : policies) {
                builder.startObject()
                    .field("policyImpl", policy.getPolicyImpl())
                    .field("policyJsonConfig", policy.getPolicyJsonConfig())
                .endObject();
            }
            builder.endArray();
        }
        builder.endObject();
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source mappings
     * @return the api
     */
    @SuppressWarnings("nls")
    public static Api unmarshallApi(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        Api bean = new Api();
        bean.setEndpoint(asString(source.get("endpoint")));
        bean.setEndpointProperties(asStringMap(source.get("endpointProperties")));
        bean.setEndpointType(asString(source.get("endpointType")));
        bean.setEndpointContentType(asString(source.get("endpointContentType")));
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setPublicAPI(asBoolean(source.get("publicAPI")));
        bean.setApiId(asString(source.get("apiId")));
        bean.setVersion(asString(source.get("version")));
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> policies = (List<Map<String,Object>>) source.get("policies");
        if (policies != null) {
            for (Map<String, Object> policySource : policies) {
                Policy policy = new Policy();
                policy.setPolicyImpl(asString(policySource.get("policyImpl")));
                policy.setPolicyJsonConfig(asString(policySource.get("policyJsonConfig")));
                bean.getApiPolicies().add(policy);
            }
        }
        return bean;
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the application bean
     * @return the content builder
     * @throws IOException when json marshalling fails
     */
    public static XContentBuilder marshall(Application bean) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        marshallInto(bean, builder);
        return builder;
    }

    /**
     * @param bean the bean
     * @param builder the content builder
     */
    @SuppressWarnings("nls")
    private static void marshallInto(Application bean, XContentBuilder builder) throws IOException {
        builder.startObject()
            .field("organizationId", bean.getOrganizationId())
            .field("applicationId", bean.getApplicationId())
            .field("version", bean.getVersion());
        Set<Contract> contracts = bean.getContracts();
        if (contracts != null) {
            builder.startArray("contracts");
            for (Contract contract : contracts) {
                builder.startObject()
                    .field("apiKey", contract.getApiKey())
                    .field("plan", contract.getPlan())
                    .field("apiOrgId", contract.getApiOrgId())
                    .field("apiId", contract.getApiId())
                    .field("apiVersion", contract.getApiVersion());
                List<Policy> policies = contract.getPolicies();
                if (policies != null) {
                    builder.startArray("policies");
                    for (Policy policy : policies) {
                        builder.startObject()
                            .field("policyImpl", policy.getPolicyImpl())
                            .field("policyJsonConfig", policy.getPolicyJsonConfig())
                        .endObject();
                    }
                    builder.endArray();
                }
                builder.endObject();
            }
            builder.endArray();
        }
        builder.endObject();
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source mappings
     * @return the application
     */
    @SuppressWarnings({ "nls", "unchecked" })
    public static Application unmarshallApplication(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        Application bean = new Application();
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setApplicationId(asString(source.get("applicationId")));
        bean.setVersion(asString(source.get("version")));
        List<Map<String,Object>> contracts = (List<Map<String,Object>>) source.get("contracts");
        if (contracts != null) {
            for (Map<String, Object> contractSource : contracts) {
                Contract contract = new Contract();
                contract.setApiKey(asString(contractSource.get("apiKey")));
                contract.setPlan(asString(contractSource.get("plan")));
                contract.setApiOrgId(asString(contractSource.get("apiOrgId")));
                contract.setApiId(asString(contractSource.get("apiId")));
                contract.setApiVersion(asString(contractSource.get("apiVersion")));
                List<Map<String,Object>> policies = (List<Map<String,Object>>) contractSource.get("policies");
                if (policies != null) {
                    for (Map<String, Object> policySource : policies) {
                        Policy policy = new Policy();
                        policy.setPolicyImpl(asString(policySource.get("policyImpl")));
                        policy.setPolicyJsonConfig(asString(policySource.get("policyJsonConfig")));
                        contract.getPolicies().add(policy);
                    }
                }

                bean.getContracts().add(contract);
            }
        }
        return bean;
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the api contract
     * @throws Exception when json marshalling fails
     * @return the content builder
     */
    @SuppressWarnings("nls")
    public static XContentBuilder marshall(ApiContract bean) throws Exception {
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
        builder.field("apiKey", bean.getApikey());
        builder.field("plan", bean.getPlan());
        builder.field("application");
        marshallInto(bean.getApplication(), builder);
        builder.field("api");
        marshallInto(bean.getApi(), builder);
        List<Policy> policies = bean.getPolicies();
        if (policies != null) {
            builder.startArray("policies");
            for (Policy policy : policies) {
                builder.startObject()
                    .field("policyImpl", policy.getPolicyImpl())
                    .field("policyJsonConfig", policy.getPolicyJsonConfig())
                .endObject();
            }
            builder.endArray();
        }
        builder.endObject();
        return builder;
    }

    /**
     * Unmarshals the given map source into a bean.
     * @param source the source mappings
     * @return the api contract
     */
    @SuppressWarnings({ "nls", "unchecked" })
    public static ApiContract unmarshallApiContract(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ApiContract contract = new ApiContract();
        contract.setApikey(asString(source.get("apiKey")));
        contract.setApplication(unmarshallApplication((Map<String, Object>) source.get("application")));
        contract.setApi(unmarshallApi((Map<String, Object>) source.get("api")));
        contract.setPlan(asString(source.get("plan")));
        List<Map<String,Object>> policies = (List<Map<String,Object>>) source.get("policies");
        if (policies != null) {
            for (Map<String, Object> policySource : policies) {
                Policy policy = new Policy();
                policy.setPolicyImpl(asString(policySource.get("policyImpl")));
                policy.setPolicyJsonConfig(asString(policySource.get("policyJsonConfig")));
                contract.getPolicies().add(policy);
            }
        }

        return contract;
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
    @SuppressWarnings("unchecked")
    private static Map<String, String> asStringMap(Object object) {
        Map<String, String> map = new HashMap<>();
        if (object != null) {
            List<Map<String,Object>> data = (List<Map<String,Object>>) object;
            for (Map<String, Object> entry : data) {
                String key = entry.keySet().iterator().next();
                String val = String.valueOf(entry.get(key));
                map.put(key, val);
            }
        }
        return  map;
    }

    /**
     * @param object
     */
    private static Boolean asBoolean(Object object) {
        if (object == null) {
            return null;
        }
        return (Boolean) object;
    }

}
