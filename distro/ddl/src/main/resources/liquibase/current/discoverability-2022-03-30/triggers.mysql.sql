

-- API Plans
CREATE TRIGGER insert_apiplan_into_discoverability AFTER INSERT ON api_plans
FOR EACH ROW BEGIN
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av
        WHERE av.id = NEW.api_version_id
    )
    SELECT
        CONCAT_WS(':',
                  Api_Version_CTE.api_org_id,
                  Api_Version_CTE.api_id,
                  Api_Version_CTE.api_version,
                  NEW.plan_id,
                  NEW.version
            ),
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        NEW.plan_id,
        NEW.version,
        NEW.discoverability
    FROM Api_Version_CTE;
END;

CREATE TRIGGER update_apiplan_into_discoverability AFTER UPDATE ON api_plans
FOR EACH ROW BEGIN
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS (
         SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
         FROM api_versions av
         WHERE av.id = NEW.api_version_id
    )
    UPDATE discoverability
    SET org_id = Api_Version_CTE.api_org_id,
        api_id = Api_Version_CTE.api_id,
        api_version = Api_Version_CTE.api_version,
        plan_id = NEW.plan_id,
        plan_version = NEW.version,
        discoverability = NEW.discoverability
    WHERE id = CONCAT_WS(':',
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        NEW.plan_id,
        NEW.version
    );
END;

CREATE TRIGGER api_plan_discoverability_trigger_delete AFTER DELETE ON api_plans
FOR EACH ROW
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av
        WHERE av.id = OLD.api_version_id
    )
    DELETE FROM discoverability
    USING Api_Version_CTE, discoverability
    WHERE discoverability.id = CONCAT_WS(':',
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        OLD.plan_id,
        OLD.version
    );
END;

-- API Versions
CREATE TRIGGER insert_apiversion_into_discoverability AFTER INSERT ON api_versions
FOR EACH ROW BEGIN
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    VALUES (
        id = CONCAT_WS(':', NEW.api_org_id, NEW.api_id, NEW.version),
        NEW.api_org_id,
        NEW.api_id,
        NEW.version,
        NULL,
        NULL,
        NEW.discoverability
    );    
END;

CREATE TRIGGER update_apiversion_into_discoverability AFTER UPDATE ON api_versions
FOR EACH ROW BEGIN
    UPDATE discoverability
    SET org_id = NEW.api_org_id,
        api_id = NEW.api_id,
        api_version = NEW.version,
        plan_id = NULL,
        plan_version = NULL,
        discoverability = NEW.discoverability
    WHERE id = CONCAT_WS(':', NEW.api_org_id, NEW.api_id, NEW.version);
END;

CREATE TRIGGER delete_apiversion_from_discoverability AFTER DELETE ON api_versions
FOR EACH ROW BEGIN
    DELETE FROM discoverability d WHERE d.id = CONCAT_WS(':', OLD.api_org_id, OLD.api_id, OLD.version);
END;