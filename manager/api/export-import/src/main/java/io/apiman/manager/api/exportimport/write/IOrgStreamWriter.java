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
package io.apiman.manager.api.exportimport.write;

import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;

/**
 * This is meant to be a builder-style interface to allow data to be streamed
 * from a DB source and written to a disk-based representation without needing
 * to cache all of it in memory at once.
 *
 * Per-Org items
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface IOrgStreamWriter {
    // IIDM
    IOrgStreamWriter startMemberships();
    IOrgStreamWriter writeMembership(RoleMembershipBean membership);
    IOrgStreamWriter endMemberships();

    // IStorage related
    IOrgStreamWriter startServiceVersions();
    IOrgStreamWriter writeServiceVersion(ServiceVersionBean svb);
    IOrgStreamWriter endServiceVersions();

    IOrgStreamWriter startApplicationVersions();
    IOrgStreamWriter writeApplicationVersion(ApplicationVersionBean avb);
    IOrgStreamWriter endApplicationVersions();

    IOrgStreamWriter startPlanVersions();
    IOrgStreamWriter writePlanVersion(PlanVersionBean pvb);
    IOrgStreamWriter endPlanVersions();

    IOrgStreamWriter startContracts();
    IOrgStreamWriter writeContract(ContractBean cb);
    IOrgStreamWriter endContracts();

    IOrgStreamWriter startPolicies();
    IOrgStreamWriter writePolicy(PolicyBean pb);
    IOrgStreamWriter endPolicies();

    // Audit
    IOrgStreamWriter startAudits();
    IOrgStreamWriter writeAudit(AuditEntryBean ab);
    IOrgStreamWriter endAudits();
}
