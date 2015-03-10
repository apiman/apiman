/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var PlanOverviewController = _module.controller("Apiman.PlanOverviewController",
        ['$scope', '$location', 'OrgSvcs', 'ApimanSvcs', ($scope, $location, OrgSvcs, ApimanSvcs) => {
            var params = $location.search();
            $scope.organizationId = params.org;

            var detail = 'overview';
            if (params.detail != null) detail = params.detail;
            if (detail == 'overview') $scope.overviewSelected = 'active';
            if (detail == 'policies') $scope.policiesSelected = 'active';
            if (detail == 'activity') $scope.activitySelected = 'active';
            $scope.include = 'plugins/apiman/html/plan-' + detail + '.include';

            OrgSvcs.get({ organizationId: params.org }, function(org) {
                $scope.org = org;
            }, function(error) {
                alert("ERROR=" + error);
            });

            OrgSvcs.get({ organizationId: params.org, entityType: 'plans', entityId: params.plan }, function(plan) {
                $scope.plan = plan;
            }, function(error) {
                alert("ERROR=" + error);
            });

            $scope.setVersion = function(plan) {
                $scope.selectedPlanVersion = plan;
                $location.path(Apiman.pluginName + "/plan-overview.html").search('org', params.org).search('plan', params.plan).search('version', plan.version);
            };

            $scope.lockPlan = function(plan) {
                var lockAction = {
                    type: 'lockPlan',
                    entityId: plan.id,
                    organizationId: plan.organizationId,
                    entityVersion: plan.version
                };
                ApimanSvcs.save(lockAction, function(reply) {
                    alert("locked");
                }, function(error) {
                    alert("ERROR=" + error);
                });
            }

            $scope.removePolicy = function(policy) {
                alert("policyId=" + policy.id);
                OrgSvcs.delete({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
                    $location.path(Apiman.pluginName + '/plan-overview.html').search('detail', params.detail).search('org', reply.organization.id).search('plan', reply.name).search('version', $scope.plan.initialVersion);
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };

            OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions' }, function(versions) {
                $scope.versions = versions;
                if (params.version != null) {
                    for (var i = 0; i < versions.length; i++) {
                        if (params.version == versions[i].version) {
                            $scope.selectedPlanVersion = versions[i];
                            break;
                        }
                    }
                } else {
                    $scope.selectedPlanVersion = versions[0];
                    $scope.entityStatus = $scope.selectedPlanVersion.status;
                }
            }, function(error) {
                alert("ERROR=" + error);
            });
            OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
                $scope.members = members;
            }, function(error) {
                alert("ERROR=" + error);
            });
            if (params.version != null) {
                OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function(policies) {
                    $scope.policies = policies;
                }, function(error) {
                    alert("ERROR=" + error);
                });
            }
        }])


}
