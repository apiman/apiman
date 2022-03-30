-- API Plans
CREATE TRIGGER api_plan_discoverability_insert_trigger AFTER INSERT ON api_plans FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiPlanDiscoverabilityTrigger";
CREATE TRIGGER api_plan_discoverability_update_trigger AFTER UPDATE ON api_plans FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiPlanDiscoverabilityTrigger";
CREATE TRIGGER api_plan_discoverability_delete_trigger AFTER DELETE ON api_plans FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiPlanDiscoverabilityTrigger";

-- API Versions
CREATE TRIGGER api_version_discoverability_insert_trigger AFTER INSERT ON api_versions FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiVersionDiscoverabilityTrigger";
CREATE TRIGGER api_version_discoverability_update_trigger AFTER UPDATE ON api_versions FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiVersionDiscoverabilityTrigger";
CREATE TRIGGER api_version_discoverability_delete_trigger AFTER DELETE ON api_versions FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiVersionDiscoverabilityTrigger";