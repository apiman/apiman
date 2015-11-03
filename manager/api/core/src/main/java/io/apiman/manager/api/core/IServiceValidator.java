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
package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.services.ServiceVersionStatusBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;

import java.util.List;

/**
 * Validates the state of services and service versions.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IServiceValidator {

    /**
     * <p>
     * Is the given service Ready to be published to the Gateway?  This method
     * will return true if all of the criteria for publishing is met.  The 
     * criteria includes (but is not necessarily limited to):
     * </p>
     * 
     * <ul>
     *   <li>A service implementation endpoint is set</li>
     *   <li>At least one Plan is selected for use</li>
     * </ul>
     * 
     * @param service
     * @return true if ready, else false
     * @throws Exception
     */
    boolean isReady(ServiceVersionBean service) throws Exception;

    /**
     * Returns detailed status information about a service, including precisely
     * why the service is in the status it's currently in.  For example, this will
     * answer the question "why can't the service be published?".
     * @param service
     * @param policies
     */
    ServiceVersionStatusBean getStatus(ServiceVersionBean service, List<PolicySummaryBean> policies);

}
