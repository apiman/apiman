-- API Plans

CREATE TRIGGER api_plan_discoverability_trigger_insert AFTER INSERT ON api_plans FOR EACH ROW BEGIN CALL insert_apiplan_into_discoverability(NEW); END;

CREATE TRIGGER api_plan_discoverability_trigger_update AFTER UPDATE ON api_plans FOR EACH ROW BEGIN CALL update_apiplan_into_discoverability(NEW); END;

CREATE TRIGGER api_plan_discoverability_trigger_delete AFTER DELETE ON api_plans FOR EACH ROW BEGIN CALL delete_apiplan_from_discoverability(OLD); END;

-- API Versions

CREATE TRIGGER api_version_discoverability_trigger_insert AFTER INSERT ON api_versions FOR EACH ROW BEGIN CALL insert_apiversion_into_discoverability(NEW); END;

CREATE TRIGGER api_version_discoverability_trigger_update AFTER UPDATE ON api_versions FOR EACH ROW BEGIN CALL update_apiversion_into_discoverability(NEW); END;

CREATE TRIGGER api_version_discoverability_trigger_delete AFTER DELETE ON api_versions FOR EACH ROW BEGIN CALL delete_apiversion_from_discoverability(OLD); END;