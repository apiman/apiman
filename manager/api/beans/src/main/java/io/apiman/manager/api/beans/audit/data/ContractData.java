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
package io.apiman.manager.api.beans.audit.data;

import io.apiman.manager.api.beans.contracts.ContractBean;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * The data saved along with the audit entry when a contract is created.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class ContractData implements Serializable {
    
    private static final long serialVersionUID = -937575521565548994L;

    private String appOrgId;
    private String appId;
    private String appVersion;
    private String serviceOrgId;
    private String serviceId;
    private String serviceVersion;
    private String planId;
    private String planVersion;
    
    /**
     * Constructor.
     */
    public ContractData() {
    }

    /**
     * Constructor.
     * @param bean
     */
    public ContractData(ContractBean bean) {
        setAppOrgId(bean.getApplication().getApplication().getOrganization().getId());
        setAppId(bean.getApplication().getApplication().getId());
        setAppVersion(bean.getApplication().getVersion());
        setServiceOrgId(bean.getService().getService().getOrganization().getId());
        setServiceId(bean.getService().getService().getId());
        setServiceVersion(bean.getService().getVersion());
        setPlanId(bean.getPlan().getPlan().getId());
        setPlanVersion(bean.getPlan().getVersion());
    }

    /**
     * @return the appOrgId
     */
    public String getAppOrgId() {
        return appOrgId;
    }

    /**
     * @param appOrgId the appOrgId to set
     */
    public void setAppOrgId(String appOrgId) {
        this.appOrgId = appOrgId;
    }

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the appVersion
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * @param appVersion the appVersion to set
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * @return the serviceOrgId
     */
    public String getServiceOrgId() {
        return serviceOrgId;
    }

    /**
     * @param serviceOrgId the serviceOrgId to set
     */
    public void setServiceOrgId(String serviceOrgId) {
        this.serviceOrgId = serviceOrgId;
    }

    /**
     * @return the serviceId
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * @param serviceId the serviceId to set
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * @return the serviceVersion
     */
    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * @param serviceVersion the serviceVersion to set
     */
    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    /**
     * @return the planId
     */
    public String getPlanId() {
        return planId;
    }

    /**
     * @param planId the planId to set
     */
    public void setPlanId(String planId) {
        this.planId = planId;
    }

    /**
     * @return the planVersion
     */
    public String getPlanVersion() {
        return planVersion;
    }

    /**
     * @param planVersion the planVersion to set
     */
    public void setPlanVersion(String planVersion) {
        this.planVersion = planVersion;
    }
    
}
