-- Trigger functions
-- Only a trigger function can be attached to a trigger, so we need this as an intermediary before we're allowed to call onto the the generic function we want
-- Reminder: To work around DdlParser.java limitations, multiline functions should have $$ as last characters of FIRST line and first characters of LAST line.

-- API Plans
CREATE OR REPLACE FUNCTION api_plan_discoverability_trigger_func() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        CALL delete_apiplan_from_discoverability(OLD);
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        CALL update_apiplan_into_discoverability(NEW);
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        CALL insert_apiplan_into_discoverability(NEW);
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
    ELSIF (TG_OP = 'UPDATE') THEN
        CALL update_apiversion_into_discoverability(NEW);
        RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
        CALL insert_apiversion_into_discoverability(NEW);
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$;

-- Triggers
--- Api Plans
CREATE TRIGGER api_plan_discoverability_trigger AFTER INSERT OR UPDATE OR DELETE ON api_plans FOR EACH ROW EXECUTE PROCEDURE api_plan_discoverability_trigger_func();

--- Api Versions
CREATE TRIGGER api_version_discoverability_trigger AFTER INSERT OR UPDATE OR DELETE ON api_versions FOR EACH ROW EXECUTE PROCEDURE api_version_discoverability_trigger_func();
-- End (postgres sometimes doesn't like the last line to be a trigger function, so this is just to pad it out).