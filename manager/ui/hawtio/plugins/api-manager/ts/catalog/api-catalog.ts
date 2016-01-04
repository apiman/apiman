/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ApiCatalogController = _module.controller("Apiman.ApiCatalogController",
        ['$q', 'Logger', '$scope', 'ApimanSvcs', 'PageLifecycle', '$uibModal', 'CurrentUserSvcs',
        ($q, Logger, $scope, ApimanSvcs, PageLifecycle, $uibModal, CurrentUserSvcs) => 
    {
            var body:any = {};
            body.filters = [];
            body.filters.push({ "name" : "name", "value" : "*", "operator" : "like" });
            var searchStr = angular.toJson(body);

            $scope.reverse = false;
            $scope.filterApis = function(searchText) {
                $scope.criteria = {
                    name: searchText
                };
            };
            
            $scope.importApi = function(api) {
                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: 'importApiModal.html',
                    controller: 'Apiman.ImportApiController',
                    resolve: {
                        api: function () {
                            var copyOf = angular.copy(api);
                            copyOf.initialVersion = '1.0';
                            return copyOf;
                        },
                        orgs: function() {
                            return $scope.orgs;
                        }
                    }
                });
            };
            
            var pageData = {
                apis: $q(function(resolve, reject) {
                    ApimanSvcs.save({ entityType: 'search', secondaryType: 'apiCatalogs' }, searchStr, function(reply) {
                        resolve(reply.beans);
                    }, reject);
                }),
                orgs: $q(function(resolve, reject) {
                    CurrentUserSvcs.query({ what: 'apiorgs' }, resolve, reject);
                })
            };
            
            PageLifecycle.loadPage('ApiCatalog', undefined, pageData, $scope, function() {
                angular.forEach($scope.apis, function(api) {
                    api.iconIsUrl = false;
                    if (!api.icon) {
                        api.icon = 'puzzle-piece';
                    }
                    if (api.icon.indexOf('http') == 0) {
                        api.iconIsUrl = true;
                    }
                    api.ticon = 'fa-file-text-o';
                    if (api.endpointType == 'soap') {
                        api.ticon = 'fa-file-code-o';
                    }
                });
                PageLifecycle.setPageTitle('api-catalog');
            });
    }]);

    
    
    export var ImportApiController = _module.controller("Apiman.ImportApiController",
        ['$q', '$rootScope', 'Logger', '$scope', 'OrgSvcs', 'PageLifecycle', '$uibModalInstance', 'api', 'orgs',
        ($q, $rootScope, Logger, $scope, OrgSvcs, PageLifecycle, $uibModalInstance, api, orgs) => 
    {
            var recentOrg = $rootScope.mruOrg;

            $scope.api = api;
            $scope.orgs = orgs;

            if (recentOrg) {
                $scope.selectedOrg = recentOrg;
            } else if (orgs.length > 0) {
                $scope.selectedOrg = orgs[0];
            }
            
            $scope.setOrg = function(org) {
                $scope.selectedOrg = org;
            };
            
            $scope.import = function () {
                $scope.importButton.state = 'in-progress';
                var newApi = {
                    'name' : $scope.api.name,
                    'description' : $scope.api.description,
                    'initialVersion' : $scope.api.initialVersion,
                    'endpoint' : $scope.api.endpoint,
                    'endpointType' : $scope.api.endpointType,
                    'definitionUrl' : $scope.api.definitionUrl,
                    'definitionType' : $scope.api.definitionType

                };
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'apis' }, newApi, function(reply) {
                    $uibModalInstance.dismiss('cancel');
                    PageLifecycle.redirectTo('/orgs/{0}/apis/{1}/{2}', reply.organization.id, reply.id, $scope.api.initialVersion);
                }, function(error) {
                    $uibModalInstance.dismiss('cancel');
                    PageLifecycle.handleError(error);
                });
            };

            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
    }]);

    
    
    export var ApiCatalogDefController = _module.controller("Apiman.ApiCatalogDefController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', '$routeParams', '$window', 'Logger', 'ApiDefinitionSvcs', 'Configuration',
        ($q, $scope, ApimanSvcs, PageLifecycle, $routeParams, $window, Logger, ApiDefinitionSvcs, Configuration) => {
            $scope.params = $routeParams;
            $scope.chains = {};
            
            var name = $scope.params.name;
    
            var body:any = {};
            body.filters = [];
            body.filters.push({ "name" : "name", "value" : $scope.params.name, "operator" : "like" });
            var searchStr = angular.toJson(body);
            
            var pageData = {
                apis: $q(function(resolve, reject) {
                    ApimanSvcs.save({ entityType: 'search', secondaryType: 'apiCatalogs' }, searchStr, function(reply) {
                        resolve(reply.beans);
                        $scope.api = reply.beans[0];
                    }, reject);
                })
            };
            
            PageLifecycle.loadPage('ApiCatalogDef', undefined, pageData, $scope, function() {

                $scope.hasError = false;

                PageLifecycle.setPageTitle('api-catalog-def', [ $scope.params.name ]);
                
                var hasSwagger = false;
                try {
                    var swagger = SwaggerUi;
                    hasSwagger = true;
                } catch (e) {}
                
                var definitionUrl = $scope.api.definitionUrl;
                var definitionType = $scope.api.definitionType;
    
                if (definitionType == 'SwaggerJSON' && hasSwagger) {
                    var authHeader = Configuration.getAuthorizationHeader();
                    
                    $scope.definitionStatus = 'loading';
                    var swaggerOptions = {
                        url: definitionUrl,
                        dom_id:"swagger-ui-container",
                        validatorUrl:null,
                        sorter : "alpha",
                        
                        onComplete: function() {
                            $('#swagger-ui-container a').each(function(idx, elem) {
                                var href = $(elem).attr('href');
                                if (href[0] == '#') {
                                    $(elem).removeAttr('href');
                                }
                            });
                            $('#swagger-ui-container div.sandbox_header').each(function(idx, elem) {
                                $(elem).remove();
                            });
                            $('#swagger-ui-container li.operation div.auth').each(function(idx, elem) {
                                $(elem).remove();
                            });
                            $('#swagger-ui-container li.operation div.access').each(function(idx, elem) {
                                $(elem).remove();
                            });
                            $scope.$apply(function(error) {
                                $scope.definitionStatus = 'complete';
                            });
                        },
                        onFailure: function() {
                            $scope.$apply(function(error) {
                                $scope.definitionStatus = 'error';
                                $scope.hasError = true;
                                $scope.error = error;
                            });
                        }
                    };
                    $window.swaggerUi = new SwaggerUi(swaggerOptions);
                    $window.swaggerUi.load();
                    $scope.hasDefinition = true;
                } else {
                    $scope.hasDefinition = false;
                }
            });
    }]);

}
