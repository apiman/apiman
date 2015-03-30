/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServicePlansController = _module.controller("Apiman.ServicePlansController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs) => {
            var params = $location.search();
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
                    serviceVersion: $q(function(resolve, reject) {
                        OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, function(serviceVersion) {
                            resolve(serviceVersion);
                        }, function(error) {
                            reject(error);
                        });
                    }),
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
                                    }, function(error) {
                                       reject(error);
                                    });
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
                        }, function(error) {
                            reject(error);
                        });
                    })
                });
            }
            var promise = $q.all(dataLoad);
            
            $scope.$watch('updatedService', function(newValue) {
                var dirty = false;
                if (newValue.publicService != $scope.serviceVersion.publicService) {
                    dirty = true;
                }
                if (newValue.plans.length != $scope.serviceVersion.plans.length) {
                    dirty = true;
                } else {
                    for (var i = 0 ; i < newValue.plans.length; i++) {
                        var p1 = newValue.plans[i];
                        var p2 = $scope.serviceVersion.plans[i];
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
                $scope.updatedService.publicService = $scope.serviceVersion.publicService;
                for (var i = 0; i < lockedPlans.length; i++) {
                    lockedPlans[i].selectedVersion = lockedPlans[i].lockedVersions[0];
                    for (var j = 0; j < $scope.serviceVersion.plans.length; j++) {
                        if (lockedPlans[i].id == $scope.serviceVersion.plans[j].planId) {
                            lockedPlans[i].checked = true;
                            lockedPlans[i].selectedVersion = $scope.serviceVersion.plans[j].version;
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
                    $scope.serviceVersion.publicService = $scope.updatedService.publicService;
                    $scope.isDirty = false;
                    $scope.saveButton.state = 'complete';
                }, function(error) {
                    if (error.status == 409) {
                        $location.url('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                    $scope.saveButton.state = 'error';
                });
            };
            
            PageLifecycle.loadPage('ServicePlans', promise, $scope, function() {
                $scope.reset();
                PageLifecycle.setPageTitle('service-plans', [ $scope.service.name ]);
            });
        }])

}
