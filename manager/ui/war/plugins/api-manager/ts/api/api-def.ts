/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

 export var ApiDefController = _module.controller('Apiman.ApiDefController',
        ['$q', '$rootScope', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'Logger', '$routeParams', 'ApiDefinitionSvcs', 'Configuration', 'EntityStatusSvc', 'CurrentUser',
        ($q, $rootScope, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, Logger, $routeParams, ApiDefinitionSvcs, Configuration, EntityStatusSvc, CurrentUser) => {
            var params = $routeParams;

            $scope.organizationId = params.org;
            $scope.tab = 'def';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.textAreaHeight = '100';
            
            $scope.typeOptions = [
                { "label" : "No API Definition",     "value" : "None" },
                { "label" : "Swagger (JSON)",        "value" : "SwaggerJSON" },
                { "label" : "Swagger (YAML)",        "value" : "SwaggerYAML" }
            ];

            var selectType = function(newType) {
                angular.forEach($scope.typeOptions, function(option) {
                    if (option.value == newType) {
                        $scope.selectedDefinitionType = option;
                    }
                });
            };

            selectType('None');

            $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;

            $scope.saveApi = function() {
                console.log('$scope.selectedDefinitionType.value: ' + $scope.selectedDefinitionType.value);
                $scope.saveButton.state = 'in-progress';

                ApiDefinitionSvcs.updateApiDefinition(params.org, params.api, params.version,
                    $scope.updatedApiDefinition, $scope.selectedDefinitionType.value,
                    function(definition) {
                        Logger.debug("Updated the api definition!");
                        $scope.apiDefinition = $scope.updatedApiDefinition;
                        $rootScope.isDirty = false;
                        $scope.saveButton.state = 'complete';
                        EntityStatusSvc.getEntity().modifiedOn = Date.now();
                        EntityStatusSvc.getEntity().modifiedBy = CurrentUser.getCurrentUser();
                    },
                    function(error) {
                        Logger.error("Error updating definition: {0}", error);
                        $scope.saveButton.state = 'error';
                    });
            };
            

            $scope.$on('afterdrop', function(event, data) {
                var newValue = data.value;

                if (newValue) {
                    if (newValue.lastIndexOf('{', 0) === 0) {
                        $scope.$apply(function() {
                            selectType('SwaggerJSON');
                        });
                    }

                    if (newValue.lastIndexOf('swagger:', 0) === 0) {
                        $scope.$apply(function() {
                            selectType('SwaggerYAML');
                        });
                    }
                }
            });

            var pageData = ApiEntityLoader.getCommonData($scope, $location);

            var loadDefinition = function() {
                ApiDefinitionSvcs.getApiDefinition(params.org, params.api, params.version,
                    function(definition) {
                        $scope.apiDefinition = definition;
                        $scope.updatedApiDefinition = definition;
                    },
                    function(error) {
                        Logger.error("Error loading definition: {0}", error);
                    });
            };


            var checkDirty = function() {
                if ($scope.version) {
                    var dirty = false;
                    
                    Logger.debug("Model def type: {1}   UI Def type: {0}", $scope.definitionType, $scope.selectedDefinitionType.value);

                    if ($scope.apiDefinition != $scope.updatedApiDefinition) {
                        Logger.debug("**** dirty because of api def");
                        dirty = true;
                    }

                    if ($scope.selectedDefinitionType.value != $scope.definitionType) {
                        Logger.debug("**** dirty because of def type: {0} != {1}", $scope.selectedDefinitionType.value, $scope.definitionType);
                        dirty = true;
                    }

                    $rootScope.isDirty = dirty;
                }
            };

            $scope.$watch('updatedApi', checkDirty, true);

            $scope.$watch('updatedApiDefinition', function(newValue, oldValue) {
                if (!newValue && !oldValue) {
                    return;
                }

                checkDirty();
            });

            $scope.$watch('selectedDefinitionType', checkDirty, true);

            $scope.reset = function() {
                selectType($scope.definitionType);
                $scope.updatedApiDefinition = $scope.apiDefinition;
                $rootScope.isDirty = false;
            };

            PageLifecycle.loadPage('ApiDef', 'apiView', pageData, $scope, function() {
                $scope.definitionType = $scope.version.definitionType;

                if (!$scope.definitionType) {
                    $scope.definitionType = 'None';
                }

                if ($scope.version.definitionType && $scope.version.definitionType != 'None' && $scope.version.definitionType != 'External') {
                    loadDefinition();
                } else {
                    Logger.debug("Skipped loading api definition - None defined.");
                }

                $scope.reset();

                PageLifecycle.setPageTitle('api-def', [ $scope.api.name ]);
            });
        }]);
}
