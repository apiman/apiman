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

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.http.IHttpClientResponse;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class ReportResponseHandler implements IAsyncResultHandler<IHttpClientResponse> {
    private final static IAsyncResult<ReportResponse> RESULT_OK = AsyncResultImpl.create(new SuccessfulReportResponse());
    private final static SAXParserFactory factory = SAXParserFactory.newInstance();
    private final IAsyncResultHandler<ReportResponse> resultHandler;

    public ReportResponseHandler(IAsyncResultHandler<ReportResponse> resultHandler) {
        this.resultHandler = resultHandler;
    }

    @Override
    public void handle(IAsyncResult<IHttpClientResponse> result) {
        if (result.isSuccess()) {
            IHttpClientResponse postResponse = result.getResult();
            if ((postResponse.getResponseCode() / 100) == 2) {
                resultHandler.handle(RESULT_OK);
            } else {
                try {
//                  ReportResponse reportResponse = parseReport(postResponse.getBody());
//                  RuntimeException re = new RuntimeException(String.format("Backend report failed. Code: %s, Message: %s",
//                          reportResponse.getErrorCode(), reportResponse.getErrorMessage()));
                    ReportResponse reportResponse = parseReport(postResponse.getBody());
                    resultHandler.handle(AsyncResultImpl.create(reportResponse));
                } catch (Exception e) {
                    RuntimeException re = new RuntimeException("Unable to parse report response", e); // TODO more specific //$NON-NLS-1$
                    resultHandler.handle(AsyncResultImpl.create(re));
                }
            }
        }
    }

    private static ReportResponse parseReport(String report) {
        try {
            SAXParser saxParser = factory.newSAXParser();
            ReturnCodeListener listener = new ReturnCodeListener();
            saxParser.parse(new InputSource(new StringReader(report)), listener);
            return listener;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static interface ReportResponse {
        boolean success();
        // boolean unauthorized?
        String getErrorCode();
        String getErrorMessage();
    }

    private static final class SuccessfulReportResponse implements ReportResponse {
        @Override
        public boolean success() {
            return true;
        }

        @Override
        public String getErrorCode() {
            return null; // We don't actually care
        }

        @Override
        public String getErrorMessage() {
            return null; // We don't actually care
        }

        @Override
        public String toString() {
            return "SuccessfulReportResponse [success=" + success() + "]";
        }
    }

    private static final class ReturnCodeListener extends DefaultHandler implements ReportResponse {
        private boolean qErrorMessage;
        private String returnCode;
        private String errorMessage;

        @Override
        public void startElement(String uri, String localName,
                String qName, Attributes attributes) throws SAXException {
            if ("error".equalsIgnoreCase(qName)) {
                qErrorMessage = true;
                returnCode = attributes.getValue("code");
            }
        }

        @Override
        public void characters(char ch[], int start, int length)
                throws SAXException {
            if (qErrorMessage) {
                errorMessage = new String(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            qErrorMessage = false;
        }

        @Override
        public String getErrorCode() {
            return returnCode;
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return "ReturnCodeListener [returnCode=" + returnCode + ", errorMessage=" + errorMessage + "]";
        }

        @Override
        public boolean success() {
            return false;
        }

    }
}
