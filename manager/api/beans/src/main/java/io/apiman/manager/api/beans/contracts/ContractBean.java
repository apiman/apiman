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
package io.apiman.manager.api.beans.contracts;

import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A Contract links an application version to a service version through
 * a plan version.  :)
 * 
 * This is how application owners/developers configure their application
 * to allow it to invoke managed services.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
@Entity
@Table(name = "contracts",
       uniqueConstraints = { @UniqueConstraint(columnNames = { "appv_id", "svcv_id", "planv_id" }) })
public class ContractBean implements Serializable {
    
    private static final long serialVersionUID = -8534463608508756791L;

    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="appv_id", referencedColumnName="id")
    })
    private ApplicationVersionBean application;
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="svcv_id", referencedColumnName="id")
    })
    private ServiceVersionBean service;
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="planv_id", referencedColumnName="id")
    })
    private PlanVersionBean plan;
    @Column(updatable=false, nullable=false)
    private String createdBy;
    @Column(updatable=false, nullable=false)
    private Date createdOn;
    @Column(updatable=false, nullable=false)
    private String apikey;

    /**
     * Constructor.
     */
    public ContractBean() {
    }

    /**
     * @return the application
     */
    public ApplicationVersionBean getApplication() {
        return application;
    }

    /**
     * @param application the application to set
     */
    public void setApplication(ApplicationVersionBean application) {
        this.application = application;
    }

    /**
     * @return the service
     */
    public ServiceVersionBean getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(ServiceVersionBean service) {
        this.service = service;
    }

    /**
     * @return the plan
     */
    public PlanVersionBean getPlan() {
        return plan;
    }

    /**
     * @param plan the plan to set
     */
    public void setPlan(PlanVersionBean plan) {
        this.plan = plan;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * @return the apikey
     */
    public String getApikey() {
        return apikey;
    }

    /**
     * @param apikey the apikey to set
     */
    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

}
