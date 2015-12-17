/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ApiCatalogController = _module.controller("Apiman.ApiCatalogController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, ApimanSvcs, PageLifecycle) => {
            var body:any = {};
            body.filters = [];
            body.filters.push({ "name" : "name", "value" : "*", "operator" : "like" });
            var searchStr = angular.toJson(body);
            
            var pageData = {
                apis: $q(function(resolve, reject) {
                    ApimanSvcs.save({ entityType: 'search', secondaryType: 'apiCatalogs' }, searchStr, function(reply) {
                        resolve(reply.beans);
                    }, reject);
                })
            };
            
            PageLifecycle.loadPage('ApiCatalog', 'apiView', pageData, $scope, function() {
                angular.forEach($scope.apis, function(api) {
                    if (!api.icon) {
                        api.icon = 'puzzle-piece';
                    }
                    apimanapis: $q(function(resolve, reject) {
                        if (api.name) {
                            var name = api.name;
                            //HACK TO MAKE IT MATCH
                            if ("cdi-cxf"==name) name = "*cdi*";
                            if ("quickstart-java-cxf-cdi"==name) name = "*cdi*";
                            var body:any = {};
                            body.filters = [];
                            //DON'T USE like but eq.
                            body.filters.push( {"name": "name", "value": name, "operator": "like"});
                            var searchStr = angular.toJson(body);
                            
                            ApimanSvcs.save({ entityType: 'search', secondaryType: 'apis' }, searchStr, function(reply) {
                                api.apimanapis = reply.beans;
                                resolve(reply.beans);
                            }, reject);
                        } else {
                            resolve([]);
                        }
                    })
                });
                PageLifecycle.setPageTitle('api-catalog');
            });
    }]);

    export var ApiCatalogDefController = _module.controller("Apiman.ApiCatalogDefController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', '$routeParams', '$window', 'Logger', 'ApiDefinitionSvcs', 'Configuration',
        ($q, $scope, ApimanSvcs, PageLifecycle, $routeParams, $window, Logger, ApiDefinitionSvcs, Configuration) => {
            $scope.params = $routeParams;
            $scope.chains = {};
            
            var name = $scope.params.name;
    
            var body:any = {};
            body.filters = [];
            body.filters.push({ "name" : "name", "value" : $scope.params.name, "operator" : "eq" });
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
                    
                    Logger.debug("!!!!! Using definition URL: {0}", definitionUrl);
    
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
