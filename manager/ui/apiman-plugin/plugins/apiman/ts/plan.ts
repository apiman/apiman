/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var PlanEntityLoader = _module.factory('PlanEntityLoader', 
        ['$q', 'OrgSvcs', 'Logger', ($q, OrgSvcs, Logger) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $location.search();
                    return {
                        org: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org }, function(org) {
                                resolve(org);
                            }, function(error) {
                                reject(error);
                            });
                        }),
                        plan: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'plans', entityId: params.plan }, function(plan) {
                                resolve(plan);
                            }, function(error) {
                                reject(error);
                            });
                        }),
                        versions: $q(function(resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions' }, function(versions) {
                                if (params.version != null) {
                                    for (var i = 0; i < versions.length; i++) {
                                        if (params.version == versions[i].version) {
                                            $scope.selectedPlanVersion = versions[i];
                                            break;
                                        }
                                    }
                                } else {
                                    $scope.selectedPlanVersion = versions[0];
                                }
                                $scope.entityStatus = $scope.selectedPlanVersion.status;
                                resolve(versions);
                            }, function(error) {
                                reject(error);
                            });
                        })
                    };
                }
            }
        }]);

    export var PlanEntityController = _module.controller("Apiman.PlanEntityController",
        ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', ($q, $scope, $location, ActionSvcs, Logger) => {
            var params = $location.search();
            
            $scope.setVersion = function(plan) {
                $scope.selectedPlanVersion = plan;
                $location.path(Apiman.pluginName + "/plan-overview.html").search('org', params.org).search('plan', params.plan).search('version', plan.version);
            };

            $scope.lockPlan = function(plan) {
                $scope.lockButton.state = 'in-progress';
                var lockAction = {
                    type: 'lockPlan',
                    entityId: plan.id,
                    organizationId: plan.organizationId,
                    entityVersion: plan.version
                };
                ActionSvcs.save(lockAction, function(reply) {
                    $scope.selectedPlanVersion.status = 'Locked';
                    $scope.lockButton.state = 'complete';
                    // need to set the entity status up a couple of scopes to get proper
                    // full-page propagation of the change event (this controller has its 
                    // own scope plus a scope from the ng-include)
                    $scope.$parent.$parent.entityStatus = $scope.selectedPlanVersion.status;
                }, function(error) {
                    $scope.lockButton.state = 'error';
                    alert("ERROR=" + error);
                });
            }
        }])

}
