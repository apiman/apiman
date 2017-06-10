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
package io.apiman.plugins.auth3scale.util.report;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;
import io.apiman.gateway.engine.policies.PolicyFailureCodes;
import io.apiman.plugins.auth3scale.util.Status;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class AuthResponseHandler implements IAsyncResultHandler<IHttpClientResponse> {
    private final IPolicyFailureFactoryComponent failureFactory;
    private IAsyncHandler<PolicyFailure> policyFailureHandler;
    private IAsyncHandler<Status> statusHandler;
    private IAsyncHandler<Throwable> exceptionHandler;

    private IHttpClientResponse response;
    private Status status;

    private static JAXBContext jaxbContext;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(Status.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthResponseHandler(IPolicyFailureFactoryComponent failureFactory) {
        this.failureFactory = failureFactory;
    }

    @Override
    public void handle(IAsyncResult<IHttpClientResponse> result) {
        if (result.isSuccess()) {
            response = result.getResult();
            status = parseStatus(response.getBody());
            PolicyFailure policyFailure = null;

            switch (response.getResponseCode()) {
                case 200:
                case 202:
                    break;
                case 403:
                    // May be able to treat all error cases without distinction by using parsed response, maybe?
                    policyFailure = policyFailure(PolicyFailureType.Authentication,
                            PolicyFailureCodes.BASIC_AUTH_FAILED,
                            403,
                            status.getReason());
                    break;
                case 409: // Over limit.
                    policyFailure = policyFailure(PolicyFailureType.Other,
                            PolicyFailureCodes.RATE_LIMIT_EXCEEDED,
                            409,
                            status.getReason());
                    break;
                default:
                    RuntimeException re = new RuntimeException("Unexpected or undocumented response code: " + response.getResponseCode()); //$NON-NLS-1$
                    exceptionHandler.handle(re); // TODO catch-all. policy failure or exception?
                    break;
            }

            if (policyFailure != null) {
                policyFailureHandler.handle(policyFailure);
            }

            statusHandler.handle(status);

            response.close();
        } else {
            exceptionHandler.handle(result.getError());
        }
    }

    private PolicyFailure policyFailure(PolicyFailureType type, int pfCode, int responseCode, String message) {
        PolicyFailure policyFailure = failureFactory.createFailure(type, pfCode, message);
        policyFailure.setResponseCode(responseCode);
        return policyFailure;
    }

    private Status parseStatus(String body) {
        try (Reader reader = new StringReader(body)) {
            Unmarshaller um = jaxbContext.createUnmarshaller();
            return (Status) um.unmarshal(reader);
        } catch (JAXBException | IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    public AuthResponseHandler failureHandler(IAsyncHandler<PolicyFailure> policyFailureHandler) {
        this.policyFailureHandler = policyFailureHandler;
        return this;
    }

    public AuthResponseHandler statusHandler(IAsyncHandler<Status> statusHandler) {
        this.statusHandler = statusHandler;
        return this;
    }

    public AuthResponseHandler exceptionHandler(IAsyncHandler<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

}
