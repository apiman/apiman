/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {
    
    var pages = [
      'overview', 'find-services', 'choose-plans', 'import-services'
    ];

    export var ServiceRedirectController = _module.controller("Apiman.ImportServicesController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams', 'Logger', 'ApimanSvcs',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams, Logger, ApimanSvcs) => {
            var params = $routeParams;
            $scope.params = params;
            
            $scope.importInfo = {
                services: [],
                isPublic: false,
                plans: []
            };

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
            
            var pageData = {
                org: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, resolve, reject);
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
                            for (var i = 0; i < lockedPlans.length; i++) {
                                lockedPlans[i].selectedVersion = lockedPlans[i].lockedVersions[0];
                            }
                            resolve(lockedPlans);
                        });
                    }, reject);
                })
            };
            
            $scope.$watch('plans', function(newValue) {
                $scope.importInfo.plans = getSelectedPlans();
            }, true);
            
            var validatePage = function() {
                var valid = true;
                if ($scope.currentPage == 'find-services') {
                    Logger.log("Validating find-services page.");
                    valid = $scope.importInfo.services.length > 0;
                }
                if ($scope.currentPage == 'choose-plans') {
                    Logger.log("Validating choose-plans page.");
                    valid = $scope.importInfo.isPublic || $scope.importInfo.plans.length > 0;
                }
                $scope.currentPageValid = valid;
                Logger.log("Current Page Valid: " + valid);
            };
            
            $scope.$watch('importInfo', validatePage, true);
            $scope.$watch('currentPage', validatePage);
            
            $scope.currentPage = 'overview';
            $scope.currentPageIdx = 0;
            $scope.currentPageValid = true;
            $scope.services = [];
            
            $scope.importSources = [
                {
                    id: 'service-catalog',
                    icon: 'search-plus',
                    name: "Service Catalog",
                    disabled: false
                },
                {
                    id: 'wadl',
                    icon: 'file-text-o',
                    name: "WADL File",
                    disabled: true
                },
                {
                    id: 'swagger',
                    icon: 'ellipsis-h',
                    name: "Swagger File",
                    disabled: true
                }
            ];
            $scope.importFrom = 'service-catalog';
            
            $scope.searchServiceCatalog = function(searchText) {
                $scope.searchButton.state = 'in-progress';
                $scope.searchDisabled = true;
                var body:any = {};
                body.filters = [];
                body.filters.push({ "name" : "name", "value" : searchText, "operator" : "like" });
                var searchStr = angular.toJson(body);
                Logger.log("Searching service catalogs: {0}", searchStr);
                ApimanSvcs.save({ entityType: 'search', secondaryType: 'serviceCatalogs' }, searchStr, function(reply) {
                    $scope.services = reply.beans;
                    Logger.log("Found {0} services.", reply.beans.length);
                    $scope.searchButton.state = 'complete';
                    $scope.searchDisabled = false;
                }, function(error) {
                    Logger.error(error);
                    // TODO do something interesting with the error
                    $scope.searchButton.state = 'complete';
                    $scope.searchDisabled = false;
                });
            }
            
            var importServices = function(services) {
                if (services.length == 0) {
                    // We're done - show the "Finish" button. :)
                    $scope.hideImportButton = true;
                    $scope.showFinishButton = true;
                    return;
                }
                var service = services[0];
                services.splice(0, 1);
                
                Logger.debug("Importing service {0}", service.name);
                Logger.debug("   # Remaining: {0}", services.length);
                
                service.status = 'importing';

                var newService = {
                    name: service.name,
                    description: service.description,
                    initialVersion: '1.0',
                    endpoint: service.endpoint,
                    endpointType: service.endpointType,
                    publicService: $scope.importInfo.isPublic,
                    plans: $scope.importInfo.plans
                };
                OrgSvcs.save({ organizationId: params.org, entityType: 'services' }, newService, function(reply) {
                    service.status = 'imported';
                    importServices(services);
                }, function (error) {
                    service.status = 'error';
                    service.error = error;
                    importServices(services);
                });
            };
            
            $scope.prevPage = function() {
                $scope.currentPageIdx = $scope.currentPageIdx - 1;
                $scope.currentPage = pages[$scope.currentPageIdx];
            }

            $scope.nextPage = function() {
                $scope.currentPageIdx = $scope.currentPageIdx + 1;
                $scope.currentPage = pages[$scope.currentPageIdx];
            }
            $scope.doImport = function() {
                $scope.disableBackButton = true;
                $scope.disableCancelButton = true;
                $scope.importButton.state = 'in-progress';
                var servicesToImport = $scope.importInfo.services.slice(0);
                servicesToImport.sort(function(s1,s2) {
                    if (s1.name.toLowerCase() < s2.name.toLowerCase()) {
                        return -1;
                    }
                    if (s1.name.toLowerCase() > s2.name.toLowerCase()) {
                        return 1;
                    }
                    return 0;
                });
                importServices(servicesToImport);
            }
            
            $scope.isServiceSelected = function(service) {
                var rval = false;
                angular.forEach($scope.importInfo.services, function(selectedService) {
                    if (service.name == selectedService.name && service.endpoint == selectedService.endpoint) {
                        rval = true;
                    }
                });
                return rval;
            }
            $scope.addService = function(service) {
                if (!$scope.isServiceSelected(service)) {
                    $scope.importInfo.services.push(service);
                }
            }
            $scope.removeService = function(service) {
                angular.forEach($scope.importInfo.services, function(s, idx) {
                    if (service == s) {
                        $scope.importInfo.services.splice(idx, 1);
                    }
                });
            }
            
            PageLifecycle.loadPage('ImportServices', pageData, $scope, function() {
                PageLifecycle.setPageTitle('import-services');
            });
        }]);
}
