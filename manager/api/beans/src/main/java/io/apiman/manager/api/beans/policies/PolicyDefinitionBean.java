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
package io.apiman.manager.api.beans.policies;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A Policy Definition describes a type of policy that can be added to 
 * an application, service, or plan.  A policy cannot be added unless a
 * definition is first created for it.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
@Entity
@Table(name = "policydefs")
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class PolicyDefinitionBean implements Serializable {

    private static final long serialVersionUID = 1801150127602136865L;
    
    @Id
    @Column(nullable=false)
    private String id;
    @Column(updatable=false, nullable=false)
    private String policyImpl;
    @Column(updatable=true, nullable=false)
    private String name;
    @Column(updatable=true, nullable=false, length=512)
    private String description;
    @Column(updatable=true, nullable=false)
    private String icon;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pd_templates", joinColumns = @JoinColumn(name = "DEF_ID"))
    private Set<PolicyDefinitionTemplateBean> templates = new HashSet<PolicyDefinitionTemplateBean>();

    /**
     * Constructor.
     */
    public PolicyDefinitionBean() {
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the policyImpl
     */
    public String getPolicyImpl() {
        return policyImpl;
    }

    /**
     * @param policyImpl the policyImpl to set
     */
    public void setPolicyImpl(String policyImpl) {
        this.policyImpl = policyImpl;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @return the templates
     */
    public Set<PolicyDefinitionTemplateBean> getTemplates() {
        return templates;
    }

    /**
     * @param templates the templates to set
     */
    public void setTemplates(Set<PolicyDefinitionTemplateBean> templates) {
        this.templates = templates;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PolicyDefinitionBean other = (PolicyDefinitionBean) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
