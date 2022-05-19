-- Trigger functions
-- Only a trigger function can be attached to a trigger, so we need this as an intermediary before we're allowed to call onto the the generic function we want
-- Reminder: To work around DdlParser.java limitations, multiline functions should have $$ as last characters of FIRST line and first characters of LAST line.

-- ApiPlan
---- Insert
CREATE OR REPLACE PROCEDURE upsert_apiplan_into_discoverability(api_plans) AS $$
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
    ON CONFLICT (id) DO UPDATE SET (org_id, api_id, api_version, plan_id, plan_version, discoverability) = (
        (select api_org_id from Api_Version_CTE),
        (select api_id from Api_Version_CTE),
        (select api_version from Api_Version_CTE),
        $1.plan_id,
        $1.version,
        $1.discoverability
    );
$$ LANGUAGE SQL;

---- Delete
CREATE OR REPLACE PROCEDURE delete_apiplan_from_discoverability(api_plans) AS $$
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
$$ LANGUAGE SQL;

-- ApiVersion
---- Insert
CREATE OR REPLACE PROCEDURE upsert_apiversion_into_discoverability(api_versions) AS $$
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    VALUES (
        CONCAT_WS(':', $1.api_org_id, $1.api_id, $1.version),
        $1.api_org_id,
        $1.api_id,
        $1.version,
        NULL,
        NULL,
        $1.discoverability
    )
    ON CONFLICT (id) DO UPDATE SET (org_id, api_id, api_version, plan_id, plan_version, discoverability) = (
         $1.api_org_id,
         $1.api_id,
         $1.version,
         NULL,
         NULL,
         $1.discoverability
    )
$$ LANGUAGE SQL;

---- Delete
CREATE OR REPLACE PROCEDURE delete_apiversion_from_discoverability(api_versions) AS $$
    DELETE FROM discoverability d WHERE d.id = CONCAT_WS(':', $1.api_org_id, $1.api_id, $1.version);
$$ LANGUAGE SQL;

-- API Plans
CREATE OR REPLACE FUNCTION api_plan_discoverability_trigger_func() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        CALL delete_apiplan_from_discoverability(OLD);
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE' or TG_OP = 'INSERT') THEN
        CALL upsert_apiplan_into_discoverability(NEW);
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$;

-- API Versions
CREATE OR REPLACE FUNCTION api_version_discoverability_trigger_func() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        CALL delete_apiversion_from_discoverability(OLD);
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE' or TG_OP = 'INSERT') THEN
        CALL upsert_apiversion_into_discoverability(NEW);
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$;

-- Triggers
--- Api Plans

DROP TRIGGER IF EXISTS api_plan_discoverability_trigger ON api_plans;
CREATE TRIGGER api_plan_discoverability_trigger AFTER INSERT OR UPDATE OR DELETE ON api_plans FOR EACH ROW EXECUTE PROCEDURE api_plan_discoverability_trigger_func();

--- Api Versions
DROP TRIGGER IF EXISTS api_version_discoverability_trigger ON api_versions;
CREATE TRIGGER api_version_discoverability_trigger AFTER INSERT OR UPDATE OR DELETE ON api_versions FOR EACH ROW EXECUTE PROCEDURE api_version_discoverability_trigger_func();
-- End (postgres sometimes doesn't like the last line to be a trigger function, so this is just to pad it out).
