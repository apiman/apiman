/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {
    
    var pages = [
      'overview', 'find-apis', 'choose-plans', 'import-apis'
    ];

    export var ApiRedirectController = _module.controller("Apiman.ImportApisController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams', 'Logger', 'ApiCatalogSvcs',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams, Logger, ApiCatalogSvcs) => {
            var params = $routeParams;
            $scope.params = params;
            $scope.organizationId = params.org;
            
            $scope.importInfo = {
                apis: [],
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
                if ($scope.currentPage == 'find-apis') {
                    Logger.log("Validating find-apis page.");
                    valid = $scope.importInfo.apis.length > 0;
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
            $scope.apis = [];
            
            $scope.importSources = [
                {
                    id: 'api-catalog',
                    icon: 'search-plus',
                    name: "API Catalog",
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
            $scope.importFrom = 'api-catalog';
            
            $scope.searchApiCatalog = function(searchText) {
                $scope.searchButton.state = 'in-progress';
                $scope.searchDisabled = true;
                var body:any = {};
                body.filters = [];
                body.filters.push({ "name" : "name", "value" : searchText, "operator" : "like" });
                var criteria = angular.toJson(body);
                Logger.log("Searching API catalogs: {0}", criteria);
                ApiCatalogSvcs.search(criteria, function(reply) {
                    $scope.apis = reply.beans;
                    Logger.log("Found {0} apis.", reply.beans.length);
                    $scope.searchButton.state = 'complete';
                    $scope.searchDisabled = false;
                }, function(error) {
                    Logger.error(error);
                    // TODO do something interesting with the error
                    $scope.searchButton.state = 'complete';
                    $scope.searchDisabled = false;
                });
            }
            
            var importApis = function(apis) {
                if (apis.length == 0) {
                    // We're done - show the "Finish" button. :)
                    $scope.hideImportButton = true;
                    $scope.showFinishButton = true;
                    return;
                }
                var api = apis[0];
                apis.splice(0, 1);
                
                Logger.debug("Importing api {0}", api.name);
                Logger.debug("   # Remaining: {0}", apis.length);
                
                api.status = 'importing';

                var newApi = {
                    name: api.name,
                    description: api.description,
                    initialVersion: '1.0',
                    endpoint: api.endpoint,
                    endpointType: api.endpointType,
                    publicAPI: $scope.importInfo.isPublic,
                    plans: $scope.importInfo.plans,
                    definitionUrl: api.definitionUrl,
                    definitionType: api.definitionType
                };
                OrgSvcs.save({ organizationId: params.org, entityType: 'apis' }, newApi, function(reply) {
                    api.status = 'imported';
                    importApis(apis);
                }, function (error) {
                    api.status = 'error';
                    api.error = error;
                    importApis(apis);
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
                var apisToImport = $scope.importInfo.apis.slice(0);
                apisToImport.sort(function(s1,s2) {
                    if (s1.name.toLowerCase() < s2.name.toLowerCase()) {
                        return -1;
                    }
                    if (s1.name.toLowerCase() > s2.name.toLowerCase()) {
                        return 1;
                    }
                    return 0;
                });
                importApis(apisToImport);
            }
            
            $scope.isApiSelected = function(api) {
                var rval = false;
                angular.forEach($scope.importInfo.apis, function(selectedApi) {
                    if (api.name == selectedApi.name && api.endpoint == selectedApi.endpoint) {
                        rval = true;
                    }
                });
                return rval;
            }
            $scope.addApi = function(api) {
                if (!$scope.isApiSelected(api)) {
                    $scope.importInfo.apis.push(api);
                }
            }
            $scope.removeApi = function(api) {
                angular.forEach($scope.importInfo.apis, function(s, idx) {
                    if (api == s) {
                        $scope.importInfo.apis.splice(idx, 1);
                    }
                });
            }
            
            PageLifecycle.loadPage('ImportApis', 'apiEdit', pageData, $scope, function() {
                PageLifecycle.setPageTitle('import-apis');
            });
        }]);
}
