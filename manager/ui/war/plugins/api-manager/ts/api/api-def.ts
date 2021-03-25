/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

export var ApiDefController = _module.controller('Apiman.ApiDefController',
    ['$q', '$rootScope', '$scope', '$location', 'Modals', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'Logger', '$routeParams', 'ApiDefinitionSvcs', 'Configuration', 'EntityStatusSvc', 'CurrentUser',
        ($q, $rootScope, $scope, $location, Modals, PageLifecycle, ApiEntityLoader, OrgSvcs, Logger, $routeParams, ApiDefinitionSvcs, Configuration, EntityStatusSvc, CurrentUser) => {
            var params = $routeParams;

            $scope.organizationId = params.org;
            $scope.tab = 'def';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.textAreaHeight = '100';
            $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;
            $scope.typeOptions = [
                { "label" : "No API Definition",     "value" : "None" },
                { "label" : "Swagger (JSON)",        "value" : "SwaggerJSON" },
                { "label" : "Swagger (YAML)",        "value" : "SwaggerYAML" },
                { "label" : "WSDL",                  "value" : "WSDL" }
            ];

            var selectType = function (newType) {
                angular.forEach($scope.typeOptions, function (option) {
                    if (option.value == newType) {
                        $scope.selectedDefinitionType = option;
                    }
                });
            };
            selectType('None');

            $scope.finishModification = function () {
                $rootScope.isDirty = false;
                $scope.saveButton.state = 'complete';
                EntityStatusSvc.getEntity().modifiedOn = Date.now();
                EntityStatusSvc.getEntity().modifiedBy = CurrentUser.getCurrentUser();
            };

            $scope.isValidDefinition = (data, definitionType) => {
                switch (definitionType) {
                    case 'SwaggerJSON':
                        try {
                            $.parseJSON(data);
                            return true;
                        } catch (error) {
                            return false;
                        }
                    case 'WSDL':
                        try {
                            $.parseXML(data);
                            return true;
                        } catch (error) {
                            return false;
                        }
                        default:
                            return true;
                }
            }

            $scope.saveApi = function () {
                console.log('$scope.selectedDefinitionType.value: ' + $scope.selectedDefinitionType.value);
                $scope.saveButton.state = 'in-progress';

                if (!$scope.isValidDefinition($scope.updatedApiDefinition, $scope.selectedDefinitionType.value)) {
                    Modals.error('Invalid API Definition!','The specified API definition is not a valid ' + $scope.selectedDefinitionType.value, null);
                    $scope.saveButton.state = 'error';
                    return;
                }

                ApiDefinitionSvcs.updateApiDefinition(params.org, params.api, params.version,
                    $scope.updatedApiDefinition, $scope.selectedDefinitionType.value,
                    function() {
                        Logger.debug("Updated the api definition!");
                        $scope.apiDefinition = $scope.updatedApiDefinition;
                        $scope.finishModification();
                    },
                    function (error) {
                        Modals.rpcerror(error, null, null);
                        Logger.error("Error updating definition: {0}", error);
                        $scope.saveButton.state = 'error';
                    });
            };


            $scope.$on('afterdrop', function (event, data) {
                let newValue = data.value;

                if (newValue) {
                    if (newValue.lastIndexOf('{', 0) === 0) {
                        $scope.$apply(function () {
                            selectType('SwaggerJSON');
                        });
                    } else if (newValue.lastIndexOf('<', 0) === 0) {
                        $scope.$apply(function () {
                            selectType('WSDL');
                        });
                    } else {
                        $scope.$apply(function () {
                            selectType('SwaggerYAML');
                        });
                    }
                }
            });

            var pageData = ApiEntityLoader.getCommonData($scope, $location);

            var loadDefinition = function () {
                ApiDefinitionSvcs.getApiDefinition(params.org, params.api, params.version,
                    function (definition) {
                        $scope.apiDefinition = definition;
                        $scope.updatedApiDefinition = definition;
                    },
                    function (error) {
                        Modals.rpcerror(error, null, null);
                        Logger.error("Error loading definition: {0}", error);
                    });
            };

            let loadDefinitionUrl = function () {
                $scope.apimanDefinitionUrl = ApiDefinitionSvcs.getApimanDefinitionUrl(params.org, params.api, params.version)
            };

            var checkDirty = function () {
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

            $scope.$watch('updatedApiDefinition', function (newValue, oldValue) {
                if (!newValue && !oldValue) {
                    return;
                }

                checkDirty();
            });

            $scope.$watch('selectedDefinitionType', checkDirty, true);

            $scope.reset = function () {
                selectType($scope.definitionType);
                $scope.updatedApiDefinition = $scope.apiDefinition;
                $scope.updatedApiDefinitionUrl = $scope.version.definitionUrl;
                $rootScope.isDirty = false;
            };

            $scope.downloadDefinition = function () {
                let element = document.createElement('a');
                element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent($scope.apiDefinition));
                let fileName = $scope.api.id + '-' + $scope.version.version + '-' + $scope.definitionType;
                let fileExtension = '.json';
                if ($scope.definitionType == 'SwaggerYAML') {
                    fileExtension = '.yaml';
                } else if ($scope.definitionType == 'WSDL') {
                    fileExtension = '.xml';
                }
                element.setAttribute('download', fileName + fileExtension);
                element.style.display = 'none';
                document.body.appendChild(element);
                element.click();
                document.body.removeChild(element);
            };

            $scope.updateDefinitionFromUrl = function () {
                let definitionUrl = $scope.updatedApiDefinitionUrl;
                let definitionType = $scope.selectedDefinitionType.value;
                ApiDefinitionSvcs.updateApiDefinitionFromUrl(params.org, params.api, params.version, definitionUrl, definitionType,
                    function () {
                        Logger.debug("Updated the api definition!");
                        loadDefinition();
                        $scope.finishModification();
                    },
                    function (error) {
                        Modals.rpcerror(error, null, null);
                        Logger.error("Error updating definition: {0}", error);
                        $scope.saveButton.state = 'error';
                    })

            };

            PageLifecycle.loadPage('ApiDef', 'apiView', pageData, $scope, function () {
                $scope.definitionType = $scope.version.definitionType;
                $scope.updatedApiDefinitionUrl = $scope.version.definitionUrl;

                if (!$scope.definitionType) {
                    $scope.definitionType = 'None';
                }

                if ($scope.version.definitionType && $scope.version.definitionType != 'None' && $scope.version.definitionType != 'External') {
                    loadDefinition();
                    loadDefinitionUrl();
                } else {
                    Logger.debug("Skipped loading api definition - None defined.");
                }

                $scope.reset();

                PageLifecycle.setPageTitle('api-def', [$scope.api.name]);
            });
        }]);
}
