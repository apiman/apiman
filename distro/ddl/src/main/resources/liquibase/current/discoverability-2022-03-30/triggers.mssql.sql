-- ApiPlan

CREATE TRIGGER insert_apiplan_into_discoverability
    ON api_plans AFTER INSERT
    AS
BEGIN
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av, inserted
        WHERE av.id = inserted.api_version_id
    )
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    SELECT
        CONCAT_WS(':',
            Api_Version_CTE.api_org_id,
            Api_Version_CTE.api_id,
            Api_Version_CTE.api_version,
            inserted.plan_id,
            inserted.version
        ),
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        inserted.plan_id,
        inserted.version,
        inserted.discoverability
    FROM Api_Version_CTE, inserted;
END;

/

CREATE TRIGGER update_apiplan_into_discoverability
    ON api_plans AFTER UPDATE
    AS
BEGIN
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av, inserted
        WHERE av.id = inserted.api_version_id
    )
    UPDATE discoverability
    SET org_id = Api_Version_CTE.api_org_id,
        api_id = Api_Version_CTE.api_id,
        api_version = Api_Version_CTE.api_version,
        plan_id = inserted.plan_id,
        plan_version = inserted.version,
        discoverability = inserted.discoverability
    FROM Api_Version_CTE, discoverability, inserted
    WHERE id = CONCAT_WS(':',
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        inserted.plan_id,
        inserted.version
    );
END;

/

CREATE TRIGGER api_plan_discoverability_trigger_delete
    ON api_plans AFTER DELETE
    AS
BEGIN
    WITH Api_Version_CTE (api_org_id, api_id, api_version)
    AS
    (
        SELECT av.api_org_id AS api_org_id, av.api_id AS api_id, av.version AS api_version
        FROM api_versions av, deleted
        WHERE av.id = deleted.api_version_id
    )
    DELETE d
    FROM discoverability d, Api_Version_CTE, deleted
    WHERE d.id = CONCAT_WS(':',
        Api_Version_CTE.api_org_id,
        Api_Version_CTE.api_id,
        Api_Version_CTE.api_version,
        deleted.plan_id,
        deleted.version
    );
END;

/

-- ApiVersion
CREATE TRIGGER insert_apiversion_into_discoverability
    ON api_versions AFTER INSERT
    AS
BEGIN
    INSERT INTO discoverability(id, org_id, api_id, api_version, plan_id, plan_version, discoverability)
    SELECT
        CONCAT_WS(':', inserted.api_org_id, inserted.api_id, inserted.version),
        inserted.api_org_id,
        inserted.api_id,
        inserted.version,
        NULL,
        NULL,
        inserted.discoverability
    FROM inserted
END;

/

CREATE TRIGGER update_apiversion_into_discoverability
    ON api_versions AFTER INSERT
    AS
BEGIN
    UPDATE discoverability
    SET org_id = inserted.api_org_id,
        api_id = inserted.api_id,
        api_version = inserted.version,
        plan_id = NULL,
        plan_version = NULL,
        discoverability = inserted.discoverability
    FROM inserted
    WHERE id = CONCAT_WS(':', inserted.api_org_id, inserted.api_id, inserted.version);
END;

/

CREATE TRIGGER delete_apiversion_from_discoverability
    ON api_versions AFTER DELETE
    AS
BEGIN
    DELETE d
    FROM discoverability d, deleted
    WHERE d.id = CONCAT_WS(':', deleted.api_org_id, deleted.api_id, deleted.version);
END;

/
