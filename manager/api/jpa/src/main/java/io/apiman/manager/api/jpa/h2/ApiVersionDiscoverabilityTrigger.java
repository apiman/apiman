package io.apiman.manager.api.jpa.h2;

import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.h2.tools.TriggerAdapter;
import org.jdbi.v3.core.Jdbi;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */

public class ApiVersionDiscoverabilityTrigger extends TriggerAdapter {

    @Override
    public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
        if (super.type == INSERT || super.type == UPDATE) {
            insertOrUpdate(conn, oldRow, newRow);
            return;
        } else if (super.type == DELETE) {
            delete(conn, oldRow, newRow);
            return;
        }
        throw new IllegalArgumentException("ApiVersionDiscoverabilityTrigger: Unexpected operation type " + super.type);
    }

    private void delete(Connection conn, ResultSet oldRow, ResultSet ignored) throws SQLException {
        String apiOrgId = oldRow.getString("api_org_id");
        String apiId = oldRow.getString("api_id");
        String apiVersion = oldRow.getString("version");

        Jdbi.create(conn).withHandle(h -> h.createUpdate(
                        "DELETE FROM discoverability d "
                                + "WHERE d.api_id = :apiId "
                                + "AND d.org_id = :orgId "
                                + "AND d.api_version = :apiVersion "))
                .bind("orgId", apiOrgId)
                .bind("apiId", apiId)
                .bind("apiVersion", apiVersion)
                .execute();
    }

    private void insertOrUpdate(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
        String apiOrgId = newRow.getString("api_org_id");
        String apiId = newRow.getString("api_id");
        String apiVersion = newRow.getString("version");
        DiscoverabilityLevel level = Optional.ofNullable(newRow.getString("discoverability"))
                .map(DiscoverabilityLevel::valueOf)
                .orElse(DiscoverabilityLevel.ORG_MEMBERS);

        String pdeKey = String.join(":", apiOrgId, apiId, apiVersion);

        if (oldRow != null) {
            DiscoverabilityLevel oldLevel = Optional.ofNullable(oldRow.getString("discoverability"))
                    .map(DiscoverabilityLevel::valueOf)
                    .orElse(DiscoverabilityLevel.ORG_MEMBERS);
            // If no change, just exit.
            if (oldLevel == level) {
                return;
            }
        }
        Jdbi.create(conn).withHandle(h -> h.createUpdate(
                        "MERGE INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability) KEY(id) "
                                + "VALUES (:id, :orgId, :apiId, :apiVersion, NULL, NULL, :discoverability)")
                .bind("id", pdeKey)
                .bind("orgId", apiOrgId)
                .bind("apiId", apiId)
                .bind("apiVersion", apiVersion)
                .bind("discoverability", level)
                .execute()
        );
    }
}

