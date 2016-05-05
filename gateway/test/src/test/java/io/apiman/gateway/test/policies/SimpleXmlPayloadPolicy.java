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
package io.apiman.gateway.test.policies;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.engine.policy.PolicyContextKeys;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A simple policy used for testing payload parsing (rest+xml format).
 *
 * @author eric.wittmann@redhat.com
 */
public class SimpleXmlPayloadPolicy implements IPolicy {

    /**
     * Constructor.
     */
    public SimpleXmlPayloadPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        return new Object();
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @SuppressWarnings("nls")
    @Override
    public void apply(final ApiRequest request, final IPolicyContext context, final Object config,
            final IPolicyChain<ApiRequest> chain) {
        Document xmlPayload = context.getAttribute(PolicyContextKeys.REQUEST_PAYLOAD, (Document) null);
        if (xmlPayload != null) {
            String prop1 = xmlPayload.getElementsByTagName("property-1").item(0).getTextContent();
            request.getHeaders().put("X-Property-1", prop1);
            Element prop5Elem = xmlPayload.createElement("property-5");
            prop5Elem.setTextContent("value-5");
            xmlPayload.getDocumentElement().appendChild(prop5Elem);
        }
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ApiResponse response, IPolicyContext context, Object config,
            IPolicyChain<ApiResponse> chain) {
        chain.doApply(response);
    }

}
