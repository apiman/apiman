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
import io.apiman.common.logging.DefaultDelegateFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.IApiClientResponse;
import io.apiman.gateway.engine.IPolicyErrorWriter;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.EngineErrorResponse;
import io.apiman.gateway.engine.beans.exceptions.IStatusCode;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * A default implementation of the error formatter.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultPolicyErrorWriter implements IPolicyErrorWriter {

    private IApimanLogger logger = new DefaultDelegateFactory().createLogger(DefaultPolicyErrorWriter.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    private static JAXBContext jaxbContext;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(EngineErrorResponse.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor.
     */
    public DefaultPolicyErrorWriter() {
    }

    /**
     * @see io.apiman.gateway.engine.IPolicyErrorWriter#write(io.apiman.gateway.engine.beans.ApiRequest, java.lang.Throwable, io.apiman.gateway.engine.IApiClientResponse)
     */
    @SuppressWarnings("nls")
    @Override
    public void write(ApiRequest request, Throwable error, IApiClientResponse response) {
        boolean isXml = false;
        if (request != null && request.getApi() != null && "xml".equals(request.getApi().getEndpointContentType())) {
            isXml = true;
        }
        String message = createErrorMessage(request, error);
        logger.error(message, error);
        response.setHeader("X-Gateway-Error", message);

        int statusCode = 500;
        if (error instanceof IStatusCode) {
            statusCode = ((IStatusCode) error).getStatusCode();
        }
        response.setStatusCode(statusCode);

        // #createErrorResponse can be overriden by subclasses (e.g. trace). So need to be careful.
        EngineErrorResponse eer = createErrorResponse(error, message, statusCode);
        if (isXml) {
            response.setHeader("Content-Type", "application/xml");
            try {
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                StringWriter sw = new StringWriter();
                jaxbMarshaller.marshal(eer, sw);
                response.write(sw.getBuffer());
            } catch (Exception e) {
                logger.error(e);
            }
        } else {
            response.setHeader("Content-Type", "application/json");
            try {
                StringWriter sw = new StringWriter();
                mapper.writer().writeValue(sw, eer);
                response.write(sw.getBuffer());
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    protected String createErrorMessage(ApiRequest request, Throwable error) {
        if (error.getMessage() == null) {
            return error.getClass().getCanonicalName() + " error occurred with no message. Refer to logs.";
        }
        return error.getMessage();
    }

    /**
     * @param error
     */
    protected EngineErrorResponse createErrorResponse(Throwable error, String message, int statusCode) {
        EngineErrorResponse eer = new EngineErrorResponse();
        eer.setResponseCode(statusCode);
        eer.setMessage(message);
        return eer;
    }

}
