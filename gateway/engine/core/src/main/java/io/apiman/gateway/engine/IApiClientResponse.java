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
package io.apiman.gateway.engine;

/**
 * An interface used to abstract the API client response.  This allows the {@link IPolicyErrorWriter} and
 * {@link IPolicyFailureWriter} to write to something not specific to a particular platform.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IApiClientResponse {

    public void setStatusCode(int code);

    public void setHeader(String headerName, String headerValue);

    public void write(String body);

    public void write(StringBuilder builder);

    public void write(StringBuffer buffer);

}
