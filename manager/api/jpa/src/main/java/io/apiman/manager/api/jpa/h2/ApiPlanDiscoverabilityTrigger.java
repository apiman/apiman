/*
 * Copyright 2022 Black Parrot Labs Ltd
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
package io.apiman.manager.api.jpa.h2;

import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import org.h2.tools.TriggerAdapter;
import org.jdbi.v3.core.Jdbi;

/**
 * Synchronize DiscoverabilityEntity materialized view when any ApiPlan with a DiscoverabilityLevel is inserted, updated, or deleted.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ApiPlanDiscoverabilityTrigger extends TriggerAdapter {

    @Override
    public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
        if (super.type == INSERT || super.type == UPDATE) {
            insertOrUpdate(conn, oldRow, newRow);
            return;
        } else if (super.type == DELETE) {
            delete(conn, oldRow, newRow);
            return;
        }
        throw new IllegalArgumentException("ApiPlanDiscoverabilityTrigger: Unexpected operation type " + super.type);
    }

    private void delete(Connection conn, ResultSet oldRow, ResultSet ignored) throws SQLException {
        Jdbi jdbi = Jdbi.create(conn);
        Long apiVersionId = oldRow.getLong("api_version_id");
        String planId = oldRow.getString("plan_id");
        String planVersion = oldRow.getString("version");

        FlatApiVersionBeanView av = jdbi.withHandle(h -> h
                        .createQuery("select avb.* FROM api_versions avb WHERE avb.id = :id")
                        .bind("id", apiVersionId))
                .mapToBean(FlatApiVersionBeanView.class)
                .first();

        jdbi.withHandle(h -> h.createUpdate(
                        "DELETE FROM discoverability d "
                                + "WHERE d.api_id = :apiId "
                                + "AND d.org_id = :orgId "
                                + "AND d.api_version = :apiVersion "
                                + "AND d.plan_id = :planId "
                                + "AND d.plan_version = :planVersion "))
                .bind("orgId", av.getApiOrgId())
                .bind("apiId", av.getApiId())
                .bind("apiVersion", av.getVersion())
                .bind("planId", planId)
                .bind("planVersion", planVersion)
                .execute();
    }

    private void insertOrUpdate(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
        Long apiVersionId = newRow.getLong("api_version_id");
        String planId = newRow.getString("plan_id");
        String planVersion = newRow.getString("version");
        DiscoverabilityLevel level = Optional.ofNullable(newRow.getString("discoverability"))
                .map(DiscoverabilityLevel::valueOf)
                .orElse(DiscoverabilityLevel.ORG_MEMBERS);

        Jdbi jdbi = Jdbi.create(conn);

        FlatApiVersionBeanView av = jdbi.withHandle(h -> h
                        .createQuery("select avb.* FROM api_versions avb WHERE avb.id = :id")
                        .bind("id", apiVersionId))
                .mapToBean(FlatApiVersionBeanView.class)
                .first();

        String pdeKey = String.join(":", av.getApiOrgId(), av.getApiId(), av.getVersion(), planId, planVersion);

        if (oldRow != null) {
            DiscoverabilityLevel oldLevel = Optional.ofNullable(oldRow.getString("discoverability"))
                    .map(DiscoverabilityLevel::valueOf)
                    .orElse(DiscoverabilityLevel.ORG_MEMBERS);
            // If no change, just exit.
            if (oldLevel == level) {
                return;
            }
        }

        jdbi.withHandle(h -> h.createUpdate(
                        "MERGE INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability) KEY(id) "
                                + "VALUES (:id, :orgId, :apiId, :apiVersion, :planId, :planVersion, :discoverability)")
                .bind("id", pdeKey)
                .bind("orgId", av.getApiOrgId())
                .bind("apiId", av.getApiId())
                .bind("apiVersion", av.getVersion())
                .bind("planId", planId)
                .bind("planVersion", planVersion)
                .bind("discoverability", level)
                .execute()
        );
    }

    // TODO(msavy): record candidate
    public static class FlatApiVersionBeanView {
        private String apiOrgId;
        private String apiId;
        private String version;

        public FlatApiVersionBeanView() {
        }

        public FlatApiVersionBeanView(String apiOrgId, String apiId, String version) {
            this.apiOrgId = apiOrgId;
            this.apiId = apiId;
            this.version = version;
        }

        public String getApiOrgId() {
            return apiOrgId;
        }

        public void setApiOrgId(String apiOrgId) {
            this.apiOrgId = apiOrgId;
        }

        public String getApiId() {
            return apiId;
        }

        public void setApiId(String apiId) {
            this.apiId = apiId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FlatApiVersionBeanView that = (FlatApiVersionBeanView) o;
            return Objects.equals(apiOrgId, that.apiOrgId) && Objects.equals(apiId, that.apiId) && Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(apiOrgId, apiId, version);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", FlatApiVersionBeanView.class.getSimpleName() + "[", "]")
                    .add("apiorgId='" + apiOrgId + "'")
                    .add("apiId='" + apiId + "'")
                    .add("version='" + version + "'")
                    .toString();
        }
    }
}