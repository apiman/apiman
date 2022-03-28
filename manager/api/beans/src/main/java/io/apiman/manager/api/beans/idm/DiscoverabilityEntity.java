package io.apiman.manager.api.beans.idm;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Immutable;

/**
 * <h1>Discoverability</h1>
 *
 * <h2>Introduction &amp; Background</h2>
 *
 * <p><strong>Discoverability</strong> is an implicit permissions system that allows API Plans and public APIs to be selectively exposed outside an organization.</p>
 *
 * <p>By default, Apiman uses an explicit permissions system that is a combination of RBAC and ABAC to determine whether a given user can view an API.
 * This requires a user to be added to an organization and granted <code>apiView</code> permissions</p>
 *
 * <p>However, there are a range of use-cases which are not adequately covered by explicit permissions that this implicit system attempts to address:</p>
 *
 * <ul>
 *     <li>Multi-tenancy: many different organizations may cohabit on the same Apiman Manager instance, yet allow non-members to subscribe to a curated set of APIs</li>
 *     <li>Developer portal: allow APIs to be exposed to dev portal users without exposing everything</li>
 *     <li>Expose a subset of APIs in an organization to external consumers without needing to know them a-priori</li>
 *     <li>Distinguish between different categories of Apiman user and offer different APIs</li>
 * </ul>
 *
 * <p>With discoverability, view permissions can be granted <strong>implicitly</strong> to various categories of users, on an API Plan (Api Version + Plan Version) and/or
 * Public Api Version level. This ensures that users can expose only the things they want, and with the narrowest possible scope.
 * Other entities in the organization which are attached to the exposed API Version inherit these permissions, such as any attached plans, plan versions, etc.
 * This ensures that the Apiman Manager API continues to function as before.</p>

 * <p>This entity represents a read-only materialized view of the various entities in Apiman which can have <code>discoverability</code> levels associated with them.
 * Triggers in the RDBMS synchronise the data into the <code>discoverability</code> table automatically.</p>
 *
 * <p>The primary purpose of this view is that it provides a centralised and performant way to integrate the discoverability system into searches and pagination without
 * resorting to expensive joins and integrating significant amounts of noisy business logic into every query.</p>
 *
 * <p>For various local needs, this view can be downloaded and indexing to provide fast and convenient filtering.
 * See <code>IndexedDiscoverabilities</code> in <code>apiman-manager-api-security</code>.
 * </p>
 *
 * <p><strong>You should not write this entity to the database from the application; it is managed by the RDBMS by trigger functions
 * (or materialized view, as appropriate)</strong></p>
 *
 * <h2>Key structure</h2>
 * <ul>
 *     <li>Api Plan (Api Version + Plan Version): orgId:apiId:apiVersion:planId:planVersion</li>
 *     <li>Public Api Version: orgId:apiId:apiVersion</li>
 * </ul>
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Entity
// @Table(name = "discoverability", uniqueConstraints = {
//         @UniqueConstraint(columnNames = {
//                 "org_id", "api_id", "api_version", "plan_id", "plan_version"
//         })
// })
@Table(name = "discoverability")
@Immutable
public class DiscoverabilityEntity implements Serializable {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "org_id")
    @NotBlank
    private String orgId;

    @Column(name = "api_id")
    @NotBlank
    private String apiId;

    @Column(name = "api_version")
    @NotBlank
    private String apiVersion;

    @Column(name = "plan_id")
    @Nullable
    private String planId;

    @Column(name = "plan_version")
    @Nullable
    private String planVersion;

    @Column(name = "discoverability")
    @NotNull
    @Enumerated(EnumType.STRING)
    private DiscoverabilityLevel discoverability;

    public DiscoverabilityEntity() {
    }

    public String getId() {
        return id;
    }

    public DiscoverabilityEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getOrgId() {
        return orgId;
    }

    public DiscoverabilityEntity setOrgId(String orgId) {
        this.orgId = orgId;
        return this;
    }

    public String getApiId() {
        return apiId;
    }

    public DiscoverabilityEntity setApiId(String apiId) {
        this.apiId = apiId;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public DiscoverabilityEntity setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Nullable
    public String getPlanId() {
        return planId;
    }

    public DiscoverabilityEntity setPlanId(@Nullable String planId) {
        this.planId = planId;
        return this;
    }

    @Nullable
    public String getPlanVersion() {
        return planVersion;
    }

    public DiscoverabilityEntity setPlanVersion(@Nullable String planVersion) {
        this.planVersion = planVersion;
        return this;
    }

    public DiscoverabilityLevel getDiscoverability() {
        return discoverability;
    }

    public DiscoverabilityEntity setDiscoverability(DiscoverabilityLevel discoverability) {
        this.discoverability = discoverability;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiscoverabilityEntity that = (DiscoverabilityEntity) o;
        return Objects.equals(id, that.id) && discoverability == that.discoverability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, discoverability);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DiscoverabilityEntity.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("orgId='" + orgId + "'")
                .add("apiId='" + apiId + "'")
                .add("apiVersion='" + apiVersion + "'")
                .add("planId='" + planId + "'")
                .add("planVersion='" + planVersion + "'")
                .add("discoverability=" + discoverability)
                .toString();
    }
}
