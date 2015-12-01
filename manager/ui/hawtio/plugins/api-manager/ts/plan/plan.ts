/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var PlanRedirectController = _module.controller("Apiman.PlanRedirectController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams) => {
            var orgId = $routeParams.org;
            var planId = $routeParams.plan;
            var pageData = {
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'plans', entityId: planId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };
            $scope.organizationId = orgId;

            PageLifecycle.loadPage('PlanRedirect', 'planView', pageData, $scope, function() {
                var version = $scope.versions[0].version;
                if (!version) {
                    PageLifecycle.handleError({ status: 404 });
                } else {
                    PageLifecycle.forwardTo('/orgs/{0}/plans/{1}/{2}', orgId, planId, version);
                }
            });
        }]);

    export var PlanEntityLoader = _module.factory('PlanEntityLoader',
        ['$q', 'OrgSvcs', 'Logger', '$rootScope', '$routeParams', 'EntityStatusService',
        ($q, OrgSvcs, Logger, $rootScope, $routeParams, EntityStatusService) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $routeParams;
                    return {
                        version: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version }, function(version) {
                                $scope.org = version.plan.organization;
                                $scope.plan = version.plan;
                                EntityStatusService.setEntity(version, 'plan');
                                resolve(version);
                            }, reject);
                        }),
                        versions: $q(function(resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions' }, resolve, reject);
                        })
                    };
                }
            }
        }]);

    export var PlanEntityController = _module.controller("Apiman.PlanEntityController",
        ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', 'PageLifecycle', '$routeParams', 'OrgSvcs', 'EntityStatusService',
        ($q, $scope, $location, ActionSvcs, Logger, PageLifecycle, $routeParams, OrgSvcs, EntityStatusService) => {
            var params = $routeParams;

            $scope.setEntityStatus = EntityStatusService.setEntityStatus;
            $scope.getEntityStatus = EntityStatusService.getEntityStatus;

            $scope.setVersion = function(plan) {
                PageLifecycle.redirectTo('/orgs/{0}/plans/{1}/{2}', params.org, params.plan, plan.version);
            };

            $scope.lockPlan = function() {
                $scope.lockButton.state = 'in-progress';
                var lockAction = {
                    type: 'lockPlan',
                    entityId: params.plan,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                ActionSvcs.save(lockAction, function(reply) {
                    $scope.version.status = 'Locked';
                    $scope.lockButton.state = 'complete';
                    $scope.setEntityStatus($scope.version.status);
                }, PageLifecycle.handleError);
            }

            $scope.updatePlanDescription = function(updatedDescription) {
                var updatePlanBean = {
                    description: updatedDescription
                }

                OrgSvcs.update({
                        organizationId: $scope.organizationId,
                        entityType: 'plans',
                        entityId: $scope.plan.id
                    },
                    updatePlanBean,
                    function(success) {
                    },
                    function(error) {
                        Logger.error("Unable to update plan description:  {0}", error);
                    });
            }
        }])

}
