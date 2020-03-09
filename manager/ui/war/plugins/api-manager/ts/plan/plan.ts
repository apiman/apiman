/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
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
        ['$q', 'OrgSvcs', 'Logger', '$rootScope', '$routeParams', 'EntityStatusSvc',
        ($q, OrgSvcs, Logger, $rootScope, $routeParams, EntityStatusSvc) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $routeParams;
                    return {
                        version: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version }, function(version) {
                                $scope.org = version.plan.organization;
                                $scope.plan = version.plan;
                                EntityStatusSvc.setEntity(version, 'plan');
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
        ['$q', '$uibModal', '$scope', '$location', 'ActionSvcs', 'Logger', 'PageLifecycle', '$routeParams', 'OrgSvcs', 'EntityStatusSvc',
        ($q, $uibModal, $scope, $location, ActionSvcs, Logger, PageLifecycle, $routeParams, OrgSvcs, EntityStatusSvc) => {
            var params = $routeParams;

            $scope.setEntityStatus = EntityStatusSvc.setEntityStatus;
            $scope.getEntityStatus = EntityStatusSvc.getEntityStatus;

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
            };

            $scope.updatePlanDescription = function(updatedDescription) {
                var updatePlanBean = {
                    description: updatedDescription
                };

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
            };


            // ----- Delete --------------------->>>>

            // Add check for ability to delete, show/hide Delete option
            $scope.canNotDelete = function () {
               return ($scope.version && $scope.version.status === 'Locked') || ($scope.versions && $scope.versions.some(function (planItem) {
                   return planItem.status === 'Locked';
               }));
            };

            // Call delete, open modal
            $scope.callDelete = function(size) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'deleteModal.html',
                    controller: 'PlanDeleteModalCtrl',
                    size: size,
                    resolve: {
                        organizationId: function() {
                            return $scope.organizationId;
                        },
                        plan: function() {
                            return $scope.plan;
                        }
                    }
                });

                modalInstance.result.then(function (selectedItem) {
                    $scope.selected = selectedItem;
                }, function () {
                    Logger.info('Modal dismissed at: ' + new Date());
                });
            };
        }]);


    export var PlanDeleteModalCtrl = _module.controller('PlanDeleteModalCtrl', function ($location,
                                                                                         $rootScope,
                                                                                         $scope,
                                                                                         $uibModalInstance,
                                                                                         OrgSvcs,
                                                                                         Configuration,
                                                                                         PageLifecycle,
                                                                                         organizationId,
                                                                                         plan) {

        $scope.confirmPlanName = '';
        $scope.plan = plan;
        
        // Used for enabling/disabling the submit button
        $scope.okayToDelete = false;

        $scope.typed = function () {
            // For user convenience, compare lower case values so that check is not case-sensitive
            $scope.okayToDelete = ($scope.confirmPlanName.toLowerCase() === plan.name.toLowerCase());
        };

        // Yes, delete the plan
        $scope.yes = function () {
            var deleteAction = {
                organizationId: organizationId,
                entityType: 'plans',
                entityId: plan.id
            };

            OrgSvcs.remove(deleteAction).$promise.then(function(res) {
                $scope.okayToDelete = false;

                // Redirect users to their list of plans
                setTimeout(function() {
                    $uibModalInstance.close();

                    $location.path($rootScope.pluginName + '/orgs/' + organizationId + '/plans');
                }, 900);

                // We should display some type of Toastr/Growl notification to the user here
            }, function(err) {
                $scope.okayToDelete = false;
                $uibModalInstance.close();
                PageLifecycle.handleError(err);
            });
        };

        // No, do NOT delete the API
        $scope.no = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });

}
