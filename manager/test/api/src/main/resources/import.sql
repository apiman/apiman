-- For tests we need the H2 triggers created here, otherwise ordering will be screwed up (we need triggers created AFTER hbm2ddl runs)
-- Furthermore, we want to avoid maintaining a manual DDL, as we lose some of the rapid development benefits of our h2 test rig.

-- API Plans
CREATE TRIGGER api_plan_discoverability_insert_trigger AFTER INSERT ON api_plans FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiPlanDiscoverabilityTrigger";
CREATE TRIGGER api_plan_discoverability_update_trigger AFTER UPDATE ON api_plans FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiPlanDiscoverabilityTrigger";
CREATE TRIGGER api_plan_discoverability_delete_trigger AFTER DELETE ON api_plans FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiPlanDiscoverabilityTrigger";

-- API Versions
CREATE TRIGGER api_version_discoverability_insert_trigger AFTER INSERT ON api_versions FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiVersionDiscoverabilityTrigger";
CREATE TRIGGER api_version_discoverability_update_trigger AFTER UPDATE ON api_versions FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiVersionDiscoverabilityTrigger";
CREATE TRIGGER api_version_discoverability_delete_trigger AFTER DELETE ON api_versions FOR EACH ROW CALL "io.apiman.manager.api.jpa.h2.ApiVersionDiscoverabilityTrigger";
