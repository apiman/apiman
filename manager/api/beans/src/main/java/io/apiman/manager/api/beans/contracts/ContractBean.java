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

import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;

import java.io.Serializable;
import java.util.Date;
import java.util.StringJoiner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.jetbrains.annotations.NotNull;

/**
 * A Contract links a client version to a API version through
 * a plan version.  :)
 *
 * This is how client owners/developers configure their client
 * to allow it to invoke managed APIs.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "contracts",
       uniqueConstraints = { @UniqueConstraint(columnNames = { "clientv_id", "apiv_id" }) })
public class ContractBean implements Serializable, Cloneable {

    private static final long serialVersionUID = -8534463608508756791L;

    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="clientv_id", referencedColumnName="id")
    })
    private ClientVersionBean client;
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="apiv_id", referencedColumnName="id")
    })
    private ApiVersionBean api;
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="planv_id", referencedColumnName="id")
    })
    private PlanVersionBean plan;
    @Column(name = "created_by", updatable=false, nullable=false)
    private String createdBy;
    @Column(name = "created_on", updatable=false, nullable=false)
    private Date createdOn;
    @Column(name = "status", nullable = false)
    @NotNull
    private ContractStatus status = ContractStatus.Created; // For backwards compat?

    /**
     * Constructor.
     */
    public ContractBean() {
    }

    /**
     * @return the client
     */
    public ClientVersionBean getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(ClientVersionBean client) {
        this.client = client;
    }

    /**
     * @return the api
     */
    public ApiVersionBean getApi() {
        return api;
    }

    /**
     * @param api the api to set
     */
    public void setApi(ApiVersionBean api) {
        this.api = api;
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
     *
     * @return
     */
    public ContractStatus getStatus() {
        return status;
    }

    /**
     *
     * @return
     */
    public ContractBean setStatus(ContractStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ContractBean.class.getSimpleName() + "[", "]")
             .add("id=" + id).add("client=" + client).add("api=" + api).add("plan=" + plan)
             .add("createdBy='" + createdBy + "'").add("createdOn=" + createdOn).add("status=" + status)
             .toString();
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    
}
