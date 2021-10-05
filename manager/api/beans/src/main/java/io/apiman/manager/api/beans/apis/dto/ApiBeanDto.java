package io.apiman.manager.api.beans.apis.dto;

import io.apiman.manager.api.beans.apis.KeyValueTag;
import io.apiman.manager.api.beans.download.BlobReference;
import io.apiman.manager.api.beans.orgs.OrganizationBean;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiBeanDto {
    private static final long serialVersionUID = 1526742536153467539L;

    private OrganizationBean organization;
    private String id;
    private String name;
    @BlobReference
    private String image;
    private String description;
    private Set<KeyValueTag> tags = new HashSet<>();
    private String createdBy;
    @Column(name = "created_on", updatable=false, nullable=false)
    private Date createdOn;
    private Integer numPublished;

    public ApiBeanDto() {
    }

    public OrganizationBean getOrganization() {
        return organization;
    }

    public ApiBeanDto setOrganization(OrganizationBean organization) {
        this.organization = organization;
        return this;
    }

    public String getId() {
        return id;
    }

    public ApiBeanDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApiBeanDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getImage() {
        return image;
    }

    public ApiBeanDto setImage(String image) {
        this.image = image;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApiBeanDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public Set<KeyValueTag> getTags() {
        return tags;
    }

    public ApiBeanDto setTags(Set<KeyValueTag> tags) {
        this.tags = tags;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public ApiBeanDto setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public ApiBeanDto setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Integer getNumPublished() {
        return numPublished;
    }

    public ApiBeanDto setNumPublished(Integer numPublished) {
        this.numPublished = numPublished;
        return this;
    }
}
