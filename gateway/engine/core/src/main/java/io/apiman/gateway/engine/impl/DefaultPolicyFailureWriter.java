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
package io.apiman.gateway.engine.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.IApiClientResponse;
import io.apiman.gateway.engine.IPolicyFailureWriter;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;

import java.io.StringWriter;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;


/**
 * A default impl of the {@link IPolicyFailureWriter} interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultPolicyFailureWriter implements IPolicyFailureWriter {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static JAXBContext jaxbContext;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(PolicyFailure.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor.
     */
    public DefaultPolicyFailureWriter() {
    }

    /**
     * @see io.apiman.gateway.engine.IPolicyFailureWriter#write(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.beans.PolicyFailure, io.apiman.gateway.engine.IApiClientResponse)
     */
    @Override
    public void write(ApiRequest request, PolicyFailure failure, IApiClientResponse response) {
        String rtype = request.getApi().getEndpointContentType();
        response.setHeader("X-Policy-Failure-Type", String.valueOf(failure.getType())); //$NON-NLS-1$
        response.setHeader("X-Policy-Failure-Message", failure.getMessage()); //$NON-NLS-1$
        response.setHeader("X-Policy-Failure-Code", String.valueOf(failure.getFailureCode())); //$NON-NLS-1$
        for (Entry<String, String> entry : failure.getHeaders().entrySet()) {
            response.setHeader(entry.getKey(), entry.getValue());
        }
        int errorCode = 500;
        if (failure.getType() == PolicyFailureType.Authentication) {
            errorCode = 401;
        } else if (failure.getType() == PolicyFailureType.Authorization) {
            errorCode = 403;
        } else if (failure.getType() == PolicyFailureType.NotFound) {
            errorCode = 404;
        }

        if (failure.getResponseCode() >= 300) {
            errorCode = failure.getResponseCode();
        }

        response.setStatusCode(errorCode);

        if ("xml".equals(rtype)) { //$NON-NLS-1$
            response.setHeader("Content-Type", "application/xml"); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                StringWriter sw = new StringWriter();
                jaxbMarshaller.marshal(failure, sw);
                response.write(sw.getBuffer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response.setHeader("Content-Type", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                StringWriter sw = new StringWriter();
                mapper.writer().writeValue(sw, failure);
                response.write(sw.getBuffer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
