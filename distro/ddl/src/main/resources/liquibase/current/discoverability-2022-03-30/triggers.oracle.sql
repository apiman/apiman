CREATE MATERIALIZED VIEW Discoverability
    BUILD IMMEDIATE -- Build the MV immediately
    REFRESH FORCE -- Refresh using fast if possible, otherwise use full refresh
--     ON COMMIT -- Refresh when commits are made to relevant tables
AS
SELECT (av2.api_org_id || ':' || av2.api_id || ':' || av2.version || ':' || ap.plan_id || ':' || ap.version) AS id, -- ApiPlans
       av2.API_ORG_ID     AS api_org_id,
       av2.API_ID         AS api_id,
       av2.VERSION        AS api_version,
       ap.PLAN_ID         AS plan_id,
       ap.VERSION         AS plan_version,
       ap.DISCOVERABILITY AS discoverability
FROM API_PLANS ap, API_VERSIONS av2
WHERE av2.id = ap.api_version_id -- ApiVersions
UNION ALL
SELECT (av.api_org_id || ':' || av.api_id || ':' || av.version) AS id,
       av.API_ORG_ID      AS api_org_id,
       av.API_ID          AS api_id,
       av.VERSION         AS api_version,
       NULL               AS plan_id,
       NULL               AS plan_version,
       av.DISCOVERABILITY AS discoverability
FROM API_VERSIONS av;
