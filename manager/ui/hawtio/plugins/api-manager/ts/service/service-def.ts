/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceDefController = _module.controller("Apiman.ServiceDefController",
        ['$q', '$window', '$rootScope', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'Logger', '$routeParams', 'ServiceDefinitionSvcs', 'Configuration',
        ($q, $window, $rootScope, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, Logger, $routeParams, ServiceDefinitionSvcs, Configuration) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'def';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            
            $scope.changeDetected = function() {
                console.log('Change detected!');
            };

            $scope.typeOptions = [
                { "label" : "No Service Definition", "value" : "None" },
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

            jQuery('#service-definition').height(100);
            jQuery('#service-definition').focus(function() {
                jQuery('#service-definition').height(450);
            });
            jQuery('#service-definition').blur(function() {
                jQuery('#service-definition').height(100);
            });
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

            var pageData = ServiceEntityLoader.getCommonData($scope, $location);

            var loadDefinition = function() {
                ServiceDefinitionSvcs.getServiceDefinition(params.org, params.service, params.version,
                    function(definition) {
                        $scope.serviceDefinition = definition;
                        $scope.updatedServiceDefinition = definition;
                    },
                    function(error) {
                        Logger.error("Error loading definition: {0}", error);
                    });
            };

            var checkDirty = function() {
                if ($scope.version) {
                    var dirty = false;
                    if ($scope.serviceDefinition != $scope.updatedServiceDefinition) {
                        Logger.debug("**** dirty because of service def");
                        dirty = true;
                    }
                    if ($scope.selectedDefinitionType.value != $scope.definitionType) {
                        Logger.debug("**** dirty because of def type: {0} != {1}", $scope.selectedDefinitionType.value, $scope.definitionType);
                        dirty = true;
                    }
                    $rootScope.isDirty = dirty;
                }
            };

            $scope.$watch('updatedService', checkDirty, true);
            $scope.$watch('updatedServiceDefinition', function(newValue, oldValue) {
                if (!newValue) {
                    return;
                }
                checkDirty();
            });
            $scope.$watch('selectedDefinitionType', checkDirty, true);

            $scope.reset = function() {
                selectType($scope.definitionType);
                $scope.updatedServiceDefinition = $scope.serviceDefinition;
                $rootScope.isDirty = false;
            };

            $scope.saveService = function() {
                $scope.saveButton.state = 'in-progress';
                var update = OrgSvcs.updateJSON;
                if ($scope.selectedDefinitionType.value == 'SwaggerJSON') {
                    update = OrgSvcs.updateYAML;
                }
                ServiceDefinitionSvcs.updateServiceDefinition(params.org, params.service, params.version,
                    $scope.updatedServiceDefinition, $scope.selectedDefinitionType.value,
                    function(definition) {
                        Logger.debug("Updated the service definition!");
                        $scope.serviceDefinition = $scope.updatedServiceDefinition;
                        $rootScope.isDirty = false;
                        $scope.saveButton.state = 'complete';
                    },
                    function(error) {
                        Logger.error("Error updating definition: {0}", error);
                        $scope.saveButton.state = 'error';
                    });
            };

            PageLifecycle.loadPage('ServiceDef', pageData, $scope, function() {
                $scope.definitionType = $scope.version.definitionType;
                if (!$scope.definitionType) {
                    $scope.definitionType = 'None';
                }
                if ($scope.version.definitionType && $scope.version.definitionType != 'None') {
                    loadDefinition();
                } else {
                    Logger.debug("Skipped loading service definition - None defined.");
                }
                $scope.reset();
                PageLifecycle.setPageTitle('service-def', [ $scope.service.name ]);
            });
            
            $window.$rootScope = $rootScope;
        }])

}
