/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

 export var ApiPlansController = _module.controller('Apiman.ApiPlansController',
        ['$q', '$rootScope', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'EntityStatusSvc', 'Configuration',
        ($q, $rootScope, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, EntityStatusSvc, Configuration) => {
            var params = $routeParams;

            $scope.organizationId = params.org;
            $scope.tab = 'plans';
            $scope.version = params.version;
            $scope.updatedApi = new Object();
            $scope.showMetrics = Configuration.ui.metrics;

            var lockedPlans = [];

            var getSelectedPlans = function() {
                var selectedPlans = [];

                for (var i = 0; i < lockedPlans.length; i++) {
                    var plan = lockedPlans[i];

                    if (plan.checked) {
                        var selectedPlan:any = {};

                        selectedPlan.planId = plan.id;
                        selectedPlan.version = plan.selectedVersion;
                        selectedPlans.push(selectedPlan);
                    }
                }

                return selectedPlans;
            };

            $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;

            var pageData = ApiEntityLoader.getCommonData($scope, $location);

            if (params.version != null) {
                pageData = angular.extend(pageData, {
                    plans: $q(function(resolve, reject) {
                        OrgSvcs.query({ organizationId: params.org, entityType: 'plans' }, function(plans) {
                            //for each plan find the versions that are locked
                            var promises = [];

                            angular.forEach(plans, function(plan) {
                                promises.push($q(function(resolve, reject) {
                                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: plan.id, versionsOrActivity: 'versions' }, function(planVersions) {
                                        //for each plan find the versions that are locked
                                       var lockedVersions = [];

                                       for (var j = 0; j < planVersions.length; j++) {
                                           var planVersion = planVersions[j];

                                           if (planVersion.status == 'Locked') {
                                               lockedVersions.push(planVersion.version);
                                           }
                                       }

                                       // if we found locked plan versions then add them
                                       if (lockedVersions.length > 0) {
                                           plan.lockedVersions = lockedVersions;
                                           lockedPlans.push(plan);
                                       }

                                       resolve(planVersions);
                                    }, reject);
                                }))
                            });

                            $q.all(promises).then(function() {
                                lockedPlans.sort(function(a,b) {
                                    if (a.id.toLowerCase() < b.id.toLowerCase()) {
                                        return -1;
                                    } else if (b.id < a.id) {
                                        return 1;
                                    } else {
                                        return 0;
                                    }
                                });

                                resolve(lockedPlans);
                            });
                        }, reject);
                    })
                });
            }

            $scope.$watch('updatedApi', function(newValue) {
                $rootScope.isDirty = false;

                if (newValue.publicAPI != $scope.version.publicAPI) {
                    $rootScope.isDirty = true;
                }

                if (newValue.plans && $scope.version.plans && newValue.plans.length != $scope.version.plans.length) {
                    $rootScope.isDirty = true;
                } else if (newValue.plans && $scope.version.plans) {
                    newValue.plans = _.sortBy(newValue.plans, 'planId');
                    $scope.version.plans = _.sortBy($scope.version.plans, 'planId');

                    for (var i = 0 ; i < newValue.plans.length; i++) {
                        var p1 = newValue.plans[i];
                        var p2 = $scope.version.plans[i];

                        if (p1.planId != p2.planId || p1.version != p2.version) {
                            $rootScope.isDirty = true;
                        }
                    }
                }
            }, true);

            $scope.$watch('plans', function(newValue) {
                $scope.updatedApi.plans = getSelectedPlans();
            }, true);

            $scope.changedVersion = function(item) {
                //console.log('changedVersion: ' + JSON.stringify(item));
            };

            $scope.reset = function() {
                $scope.updatedApi.publicAPI = $scope.version.publicAPI;

                for (var i = 0; i < lockedPlans.length; i++) {
                    lockedPlans[i].selectedVersion = lockedPlans[i].lockedVersions[0];

                    for (var j = 0; j < $scope.version.plans.length; j++) {
                        if (lockedPlans[i].id == $scope.version.plans[j].planId) {
                            lockedPlans[i].checked = true;
                            lockedPlans[i].selectedVersion = $scope.version.plans[j].version;
                            break;
                        }
                    }
                }

                $scope.updatedApi.plans = getSelectedPlans();
                $rootScope.isDirty = false;
            };

            $scope.saveApi = function() {
                $scope.saveButton.state = 'in-progress';

                OrgSvcs.update({ organizationId: params.org, entityType: 'apis', entityId: params.api, versionsOrActivity: 'versions', version: params.version }, $scope.updatedApi, function(reply) {
                    $scope.version.publicAPI = $scope.updatedApi.publicAPI;
                    $scope.saveButton.state = 'complete';
                    $scope.version = reply;
                    EntityStatusSvc.setEntityStatus(reply.status);

                    $rootScope.isDirty = false;
                }, PageLifecycle.handleError);
            };

            PageLifecycle.loadPage('ApiPlans', 'apiView', pageData, $scope, function() {
                $scope.reset();
                PageLifecycle.setPageTitle('api-plans', [ $scope.api.name ]);
            });
        }]);
}
