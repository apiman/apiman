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

import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.exportimport.beans.MetadataBean;

/**
 * Global stuff
 * @author msavy
 */
public interface IGlobalStreamWriter {
    // Metadata
    IGlobalStreamWriter writeMetadata(MetadataBean metadata);

    // IIDM related stuff
    IGlobalStreamWriter startUsers();
    IGlobalStreamWriter writeUser(UserBean user);
    IGlobalStreamWriter endUsers();

    IGlobalStreamWriter startRoles();
    IGlobalStreamWriter writeRole(RoleBean role);
    IGlobalStreamWriter endRoles();

    // Orgs
    IGlobalStreamWriter startOrgs();
    IOrgStreamWriter writeOrg(OrganizationBean org);
    IGlobalStreamWriter endOrgs();

    IGlobalStreamWriter startPlugins();
    IGlobalStreamWriter writePlugin(PluginBean pb);
    IGlobalStreamWriter endPlugins();

    IGlobalStreamWriter startGateways();
    IGlobalStreamWriter writeGateways(GatewayBean gb);
    IGlobalStreamWriter endGateways();

    void close();
}
