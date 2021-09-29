// /*
//  * Copyright 2014 JBoss Inc
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *      http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */
// package io.apiman.gateway.engine.policies;
//
// import io.apiman.gateway.engine.async.IAsyncResultHandler;
// import io.apiman.gateway.engine.beans.ApiRequest;
// import io.apiman.gateway.engine.beans.ApiResponse;
// import io.apiman.gateway.engine.beans.IPolicyProbeRequest;
// import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
// import io.apiman.gateway.engine.beans.PolicyFailure;
// import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
// import io.apiman.gateway.engine.policy.IPolicy;
// import io.apiman.gateway.engine.policy.IPolicyChain;
// import io.apiman.gateway.engine.policy.IPolicyContext;
// import io.apiman.gateway.engine.policy.IPolicyFailureChain;
//
// import com.fasterxml.jackson.databind.DeserializationFeature;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.SerializationFeature;
// import com.fasterxml.jackson.databind.json.JsonMapper;
// import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
//
// import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER;
// import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS;
// import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS;
// import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS;
// import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES;
// import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_COMMA;
// import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS;
// import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES;
//
// /**
//  * A base class for policy impls that uses Jackson to parse configuration info.
//  *
//  * @author eric.wittmann@redhat.com
//  * @author marc@blackparrotlabs.io
//  * @param <C> the config type
//  * @param <P> the probe request type
//  */
// public abstract class AbstractMappedPolicyWithProbe<C, P extends IPolicyProbeRequest> implements IPolicy {
//
//     private static final ObjectMapper mapper = JsonMapper
//          .builder()
//          // (mostly) match the JSON5 spec.
//          .enable(ALLOW_UNQUOTED_FIELD_NAMES)
//          .enable(ALLOW_TRAILING_COMMA)
//          .enable(ALLOW_SINGLE_QUOTES)
//          .enable(ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
//          .enable(ALLOW_NON_NUMERIC_NUMBERS)
//          .enable(ALLOW_JAVA_COMMENTS)
//          .enable(ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
//          .enable(ALLOW_UNESCAPED_CONTROL_CHARS)
//          // Avoid weird floating point timestamps
//          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//          // Enable various Java 8 and library data structures to be serialized
//          .addModule(new JavaTimeModule())
//          .addModule(new ParameterNamesModule())
//          .addModule(new Jdk8Module())
//          .build();
//
//     /**
//      * Constructor.
//      */
//     public AbstractMappedPolicyWithProbe() {
//     }
//
//     /**
//      * {@inheritDoc}
//      * <p>
//      * Using Jackson, the configuration is read into the class specified structure by {@link #getConfigurationClass()}.
//      * <p>
//      * Most implementors should not override this method unless they do not want to use Jackson (e.g. using a non-JSON
//      * format).
//      */
//     @Override
//     public C parseConfiguration(String jsonConfiguration) throws ConfigurationParseException {
//         try {
//             return mapper.readerFor(getConfigurationClass()).readValue(jsonConfiguration);
//         } catch (Exception e) {
//             throw new ConfigurationParseException(e);
//         }
//     }
//
//     /**
//      * The class to use for JSON configuration deserialization
//      *
//      * @return the class
//      */
//     protected abstract Class<C> getConfigurationClass();
//
//     /**
//      * {@inheritDoc}
//      */
//     @SuppressWarnings("unchecked")
//     @Override
//     public final void apply(ApiRequest request, IPolicyContext context, Object config,
//             IPolicyChain<ApiRequest> chain) {
//         doApply(request, context, (C) config, chain);
//     }
//
//     /**
//      * Override this method to interact with or observe the request.
//      *
//      * @see #apply(ApiResponse, IPolicyContext, Object, IPolicyChain)
//      * @param request the request
//      * @param chain the ordered policy chain
//      */
//     protected void doApply(ApiRequest request, IPolicyContext context, C config, IPolicyChain<ApiRequest> chain) {
//         chain.doApply(request);
//     }
//
//     /**
//      * {@inheritDoc}
//      */
//     @SuppressWarnings("unchecked")
//     @Override
//     public final void apply(ApiResponse response, IPolicyContext context, Object config,
//             IPolicyChain<ApiResponse> chain) {
//         doApply(response, context, (C) config, chain);
//     }
//
//     /**
//      * Apply the policy to the response.
//      * <p>
//      * Override this method to interact with or observe the response.
//      *
//      * @param response the response
//      * @param context the policy context
//      * @param config the configuration (see {@link #getConfigurationClass()})
//      * @param chain the ordered policy chain
//      */
//     protected void doApply(ApiResponse response, IPolicyContext context, C config, IPolicyChain<ApiResponse> chain) {
//         chain.doApply(response);
//     }
//
//     @SuppressWarnings("unchecked")
//     @Override
//     public void processFailure(PolicyFailure failure, IPolicyContext context, Object config, IPolicyFailureChain chain) {
//         doProcessFailure(failure, context, (C) config, chain);
//     }
//
//     /**
//      * Override if you wish to modify a failure.
//      *
//      * @see IPolicy#processFailure(PolicyFailure, IPolicyContext, Object, IPolicyFailureChain)
//      */
//     protected void doProcessFailure(PolicyFailure failure, IPolicyContext context, C config, IPolicyFailureChain chain) {
//         chain.doFailure(failure);
//     }
//
//     /**
//      * Return the class to unmarshall the raw probe request JSON configuration into.
//      * <p>
//      * For compatibility reasons this is not abstract, but all classes who provide probe capabilities should implement it.
//      */
//     protected Class<P> getProbeRequestClass() {
//         return null;
//     }
//
//     /**
//      * {@inheritDoc}
//      * <p>
//      * This version uses Jackson to parse the probe configuration into the class specified by
//      * {@link #getProbeRequestClass()}, and the policy config specified by {@link #getConfigurationClass()}.
//      * <p>
//      * Most implementors should override the abstract
//      * {@link #doProbe(IPolicyProbeRequest, Object, IPolicyContext, IAsyncResultHandler)} method rather than this one,
//      * unless they are doing something more exotic (e.g. not using JSON).
//      */
//     @Override
//     public void probe(String probeRequestRaw, String policyConfigRaw, IPolicyContext context, IAsyncResultHandler<IPolicyProbeResponse> resultHandler) {
//         try {
//             P probeConfig = mapper.readValue(probeRequestRaw, getProbeRequestClass());
//             C policyConfig = mapper.readValue(policyConfigRaw, getConfigurationClass());
//             doProbe(probeConfig, policyConfig, context, resultHandler);
//         } catch (Exception e) {
//             throw new ConfigurationParseException(e);
//         }
//     }
//
//     /**
//      * Override this method to provide a state probe for your policy implementation.
//      *
//      * @see IPolicy#probe(String, String, IPolicyContext, IAsyncResultHandler)
//      */
//     protected abstract void doProbe(P probeRequest, C policyConfig, IPolicyContext context, IAsyncResultHandler<IPolicyProbeResponse> resultHandler);
// }
