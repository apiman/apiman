-- Generic procedures for auto-updating the Discoverability view (a manually orchestrated Materialized View).
-- Database-specific mechanisms are needed for the trigger itself, which calls down onto these as needed.

-- ApiPlan
---- Insert
CREATE PROCEDURE insert_apiplan_into_discoverability(api_plans) AS $$
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av
        WHERE av.id = $1.api_version_id
    )
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    SELECT
        CONCAT_WS(':',
            Api_Version_CTE.api_org_id,
            Api_Version_CTE.api_id,
            Api_Version_CTE.api_version,
            $1.plan_id,
            $1.version
        ),
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        $1.plan_id,
        $1.version,
        $1.discoverability
    FROM Api_Version_CTE
$$ LANGUAGE SQL

---- Update
CREATE PROCEDURE update_apiplan_into_discoverability(api_plans) AS $$
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av
        WHERE av.id = $1.api_version_id
    )
    UPDATE discoverability
    SET (id, org_id, api_id, api_version, plan_id, plan_version, discoverability) = (
      CONCAT_WS(':',
            Api_Version_CTE.api_org_id,
            Api_Version_CTE.api_id,
            Api_Version_CTE.api_version,
            $1.plan_id,
            $1.version
      ),
      Api_Version_CTE.api_org_id,
      Api_Version_CTE.api_id,
      Api_Version_CTE.api_version,
      $1.plan_id,
      $1.version,
      $1.discoverability
    )
    FROM Api_Version_CTE
$$ LANGUAGE SQL

---- Delete
CREATE PROCEDURE delete_apiplan_from_discoverability(api_plans) AS $$
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av
        WHERE av.id = $1.api_version_id
    )
    DELETE FROM ONLY discoverability d
    USING Api_Version_CTE
    WHERE d.id = CONCAT_WS(':',
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        $1.plan_id,
        $1.version
    );
$$ LANGUAGE SQL

-- ApiVersion
---- Insert
CREATE PROCEDURE insert_apiversion_into_discoverability(api_versions) AS $$
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    VALUES (
        CONCAT_WS(':', $1.api_org_id, $1.api_id, $1.version),
        $1.api_org_id,
        $1.api_id,
        $1.version,
        NULL,
        NULL,
        $1.discoverability
    );
$$ LANGUAGE SQL

---- Update
CREATE PROCEDURE update_apiversion_into_discoverability(api_versions) AS $$
    UPDATE discoverability
    SET (id, org_id, api_id, api_version, plan_id, plan_version, discoverability) = (
        CONCAT_WS(':', $1.api_org_id, $1.api_id, $1.version),
        $1.api_org_id,
        $1.api_id,
        $1.version,
        NULL,
        NULL,
        $1.discoverability
    );
$$ LANGUAGE SQL

---- Delete
CREATE PROCEDURE delete_apiversion_from_discoverability(api_versions) AS $$
    DELETE discoverability d
    WHERE d.id = CONCAT_WS(':', $1.api_org_id, $1.api_id, $1.version);
$$ LANGUAGE SQL