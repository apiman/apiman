/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

 export var ServicePlansController = _module.controller("Apiman.ServicePlansController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs',
         ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'plans';
            $scope.version = params.version;
            var lockedPlans = [];
            
            var dataLoad = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                dataLoad = angular.extend(dataLoad, {
                    selectedService: $q(function(resolve, reject) {
                        OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, function(selectedService) {
                            resolve(selectedService);
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
                                       for (var j=0; j<planVersions.length; j++) {
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
                              
                               resolve(lockedPlans);
                               
                               for (var i=0; i<lockedPlans.length; i++) {
                                   lockedPlans[i].selectedVersion = lockedPlans[i].lockedVersions[0];
                                   for (var j=0; j<$scope.selectedService.plans.length; j++) {
                                       if (lockedPlans[i].id == $scope.selectedService.plans[j].planId) {
                                           lockedPlans[i].checked=true;
                                           lockedPlans[i].selectedVersion = $scope.selectedService.plans[j].version;
                                           break;
                                       }
                                   }
                               }
                       
                               $scope.lockedPlans = lockedPlans;
                           });
                           resolve(plans);
                        }, function(error) {
                            reject(error);
                        });
                    })
                });
            }
            var promise = $q.all(dataLoad);
             
            $scope.saveService = function() {
                //$scope.saveButton.state = 'Saving...';
                var updatedService:any = {};
                updatedService.endpoint = $scope.selectedService.endpoint;
                updatedService.gateways = $scope.selectedService.gateways; //TBD
                var selectedPlans = [];
                for (var i=0; i<lockedPlans.length; i++) {
                    var plan = lockedPlans[i];
                    if (plan.checked) {
                        var selectedPlan:any = {};
                        selectedPlan.planId = plan.id;
                        selectedPlan.version = plan.selectedVersion;
                        selectedPlans.push(selectedPlan);
                    }
                }
                updatedService.plans = selectedPlans;
                updatedService.endpointType = $scope.selectedService.endpointType;
                updatedService.publicService = $scope.selectedService.publicService;
                
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version }, updatedService, function(reply) {
                    //$scope.saveButton.state = 'Save';
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        //$scope.saveButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            PageLifecycle.loadPage('ServicePlans', promise, $scope);
        }])

}
