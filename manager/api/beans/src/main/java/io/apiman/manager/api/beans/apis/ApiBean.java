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
package io.apiman.manager.api.beans.apis;

import io.apiman.manager.api.beans.download.BlobReference;
import io.apiman.manager.api.beans.orgs.OrganizationBasedCompositeId;
import io.apiman.manager.api.beans.orgs.OrganizationBean;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Models an API.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "apis")
@IdClass(OrganizationBasedCompositeId.class)
@JsonInclude(Include.NON_NULL)
public class ApiBean implements Serializable, Cloneable {

    private static final long serialVersionUID = 1526742536153467539L;

    @Id
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="organization_id", referencedColumnName="id")
    })
    private OrganizationBean organization;
    @Id
    @Column(nullable=false)
    private String id;
    @Column(nullable=false)
    private String name;
    @Column(name = "image_file_ref", updatable = true, nullable = true) // Reference to file storage (we'll ship with DB blob)
    @BlobReference
    private String image;
    @Column(name = "description", updatable=true, nullable=true, length=512)
    private String description;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "api_tag",
            joinColumns = { @JoinColumn(name = "api_id"), @JoinColumn(name = "org_id") },
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<KeyValueTag> tags = new HashSet<>();
    @Column(name = "created_by", updatable=false, nullable=false)
    private String createdBy;
    @Column(name = "created_on", updatable=false, nullable=false)
    private Date createdOn;
    @Column(name = "num_published", updatable=true, nullable=true)
    private Integer numPublished;
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval=true, fetch=FetchType.LAZY, mappedBy="api")
    @JsonIgnore
    private Set<ApiVersionBean> apiVersionSet = new LinkedHashSet<>();

    /**
     * Constructor.
     */
    public ApiBean() {
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
     * @return the organization
     */
    public OrganizationBean getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(OrganizationBean organization) {
        this.organization = organization;
    }

    /**
     * @return the numPublished
     */
    public Integer getNumPublished() {
        return numPublished;
    }

    /**
     * @param numPublished the numPublished to set
     */
    public void setNumPublished(Integer numPublished) {
        this.numPublished = numPublished;
    }

    /**
     * Get the image blob ref for this API.
     *
     * To dereference this manually refer to IBlobStore.
     *
     * @see BlobReference
     * @return get the blob ref.
     */
    public String getImage() {
        return image;
    }

    /**
     * @param imageFileRef image's blob ref
     */
    public void setImage(String imageFileRef) {
        this.image = imageFileRef;
    }

    public Set<KeyValueTag> getTags() {
        return tags;
    }

    public ApiBean setTags(Set<KeyValueTag> tags) {
        this.tags = tags;
        return this;
    }

    public void addTag(KeyValueTag keyValueTag) {
        this.tags.add(keyValueTag);
    }

    public void removeTagByKey(String key) {
        this.tags.remove(new KeyValueTag().setKey(key));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiBean.class.getSimpleName() + "[", "]")
                .add("organization=" + organization)
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("image='" + image + "'")
                .add("description='" + description + "'")
                .add("createdBy='" + createdBy + "'")
                .add("createdOn=" + createdOn)
                .add("numPublished=" + numPublished)
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
