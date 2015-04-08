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

import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;

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
     * @param bean the service bean
     * @throws Exception 
     */
    public static XContentBuilder marshall(Service bean) throws Exception {
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
    protected static void marshallInto(Service bean, XContentBuilder builder) throws IOException {
        builder.startObject()
            .field("endpoint", bean.getEndpoint())
            .field("endpointType", bean.getEndpointType())
            .field("publicService", bean.isPublicService())
            .field("organizationId", bean.getOrganizationId())
            .field("serviceId", bean.getServiceId())
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
        List<Policy> policies = bean.getServicePolicies();
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
     * @param source
     */
    @SuppressWarnings("nls")
    public static Service unmarshallService(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        Service bean = new Service();
        bean.setEndpoint(asString(source.get("endpoint")));
        bean.setEndpointProperties(asStringMap(source.get("endpointProperties")));
        bean.setEndpointType(asString(source.get("endpointType")));
        bean.setOrganizationId(asString(source.get("organizationId")));
        bean.setPublicService(asBoolean(source.get("publicService")));
        bean.setServiceId(asString(source.get("serviceId")));
        bean.setVersion(asString(source.get("version")));
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> policies = (List<Map<String,Object>>) source.get("policies");
        if (policies != null) {
            for (Map<String, Object> policySource : policies) {
                Policy policy = new Policy();
                policy.setPolicyImpl(asString(policySource.get("policyImpl")));
                policy.setPolicyJsonConfig(asString(policySource.get("policyJsonConfig")));
                bean.getServicePolicies().add(policy);
            }
        }
        return bean;
    }

    /**
     * Marshals the given bean into the given map.
     * @param bean the application bean
     * @throws Exception 
     */
    public static XContentBuilder marshall(Application bean) throws Exception {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        marshallInto(bean, builder);
        return builder;
    }

    /**
     * @param bean
     * @param builder
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
                    .field("serviceOrgId", contract.getServiceOrgId())
                    .field("serviceId", contract.getServiceId())
                    .field("serviceVersion", contract.getServiceVersion());
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
     * @param source
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
                contract.setServiceOrgId(asString(contractSource.get("serviceOrgId")));
                contract.setServiceId(asString(contractSource.get("serviceId")));
                contract.setServiceVersion(asString(contractSource.get("serviceVersion")));
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
     * @param bean 
     * @throws Exception  
     */
    @SuppressWarnings("nls")
    public static XContentBuilder marshall(ServiceContract bean) throws Exception {
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
        builder.field("apiKey", bean.getApikey());
        builder.field("application");
        marshallInto(bean.getApplication(), builder);
        builder.field("service");
        marshallInto(bean.getService(), builder);
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
     * @param source
     */
    @SuppressWarnings({ "nls", "unchecked" })
    public static ServiceContract unmarshallServiceContract(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        ServiceContract contract = new ServiceContract();
        contract.setApikey(asString(source.get("apiKey")));
        contract.setApplication(unmarshallApplication((Map<String, Object>) source.get("application")));
        contract.setService(unmarshallService((Map<String, Object>) source.get("service")));
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
