/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServicePlansController = _module.controller("Apiman.ServicePlansController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'plans';
            $scope.version = params.version;
            $scope.updatedService = new Object();
            
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
            
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                dataLoad = angular.extend(dataLoad, {
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
                                           if (planVersion.status == "Locked") {
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
            var promise = $q.all(dataLoad);
            
            $scope.$watch('updatedService', function(newValue) {
                var dirty = false;
                if (newValue.publicService != $scope.version.publicService) {
                    dirty = true;
                }
                if (newValue.plans.length != $scope.version.plans.length) {
                    dirty = true;
                } else {
                    for (var i = 0 ; i < newValue.plans.length; i++) {
                        var p1 = newValue.plans[i];
                        var p2 = $scope.version.plans[i];
                        if (p1.planId != p2.planId || p1.version != p2.version) {
                            dirty = true;
                        }
                    }
                }
                $scope.isDirty = dirty;
            }, true);
            
            $scope.$watch('plans', function(newValue) {
                $scope.updatedService.plans = getSelectedPlans();
            }, true);
            
            $scope.reset = function() {
                $scope.updatedService.publicService = $scope.version.publicService;
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
                $scope.updatedService.plans = getSelectedPlans();
                $scope.isDirty = false;
            };
            
            $scope.saveService = function() {
                $scope.saveButton.state = 'in-progress';
                
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, $scope.updatedService, function(reply) {
                    $scope.version.publicService = $scope.updatedService.publicService;
                    $scope.isDirty = false;
                    $scope.saveButton.state = 'complete';
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('ServicePlans', promise, $scope, function() {
                $scope.reset();
                PageLifecycle.setPageTitle('service-plans', [ $scope.service.name ]);
            });
        }])

}
