/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var PlanEntityLoader = _module.factory('PlanEntityLoader', 
        ['$q', 'OrgSvcs', 'Logger', ($q, OrgSvcs, Logger) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $location.search();
                    $scope.setEntityStatus = function(status) {
                        $scope.entityStatus = status;
                    };
                    return {
                        org: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org }, function(org) {
                                resolve(org);
                            }, reject);
                        }),
                        plan: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'plans', entityId: params.plan }, function(plan) {
                                resolve(plan);
                            }, reject);
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
                                $scope.setEntityStatus($scope.selectedPlanVersion.status);
                                resolve(versions);
                            }, reject);
                        })
                    };
                }
            }
        }]);

    export var PlanEntityController = _module.controller("Apiman.PlanEntityController",
        ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', 'PageLifecycle',
        ($q, $scope, $location, ActionSvcs, Logger, PageLifecycle) => {
            var params = $location.search();
            
            $scope.setVersion = function(plan) {
                $scope.selectedPlanVersion = plan;
                $location.search('version', plan.version);
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
                    $scope.setEntityStatus($scope.selectedPlanVersion.status);
                }, PageLifecycle.handleError);
            }
        }])

}
