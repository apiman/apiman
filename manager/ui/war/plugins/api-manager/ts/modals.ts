/// <reference path="../../includes.ts"/>
module ApimanModals {

    export var _module = angular.module('ApimanModals', ['ApimanLogger', 'ApimanRPC']);

    export var Modals = _module.factory('Modals',
        ['Logger', '$uibModal',
        (Logger, $uibModal) => {
                return {
                    // Simple data entry dialog
                    ///////////////////////////
                    getValue: function (title, message, label, initialValue, okCallback, cancelCallback) {
                        var options = {
                            initialValue: initialValue,
                            label: label,
                            message: message,
                            title: title
                        };

                        var modalInstance = $uibModal.open({
                            animation: true,
                            templateUrl: 'getValueModal.html',
                            controller: 'ModalGetValueCtrl',
                            resolve: {
                                options: function () {
                                    return options;
                                }
                            }
                        });

                        modalInstance.result.then(okCallback, cancelCallback);
                    },
                    // A standard confirmation dialog
                    /////////////////////////////////
                    confirm: function (title, message, yesCallback, noCallback) {
                        var options = {
                            title: title,
                            message: message
                        };

                        var modalInstance = $uibModal.open({
                            animation: true,
                            templateUrl: 'confirmModal.html',
                            controller: 'ModalConfirmCtrl',
                            resolve: {
                                options: function () {
                                    return options;
                                }
                            }
                        });

                        modalInstance.result.then(yesCallback, noCallback);
                    }
                }
            }]);


    export var ClientAppDeleteModalCtrl = _module.controller('ModalClientAppDeleteCtrl', 
        function ($location, $rootScope, $scope, $uibModalInstance, OrgSvcs, Configuration, PageLifecycle, client, params) {
        
        $scope.confirmClientName = '';
        
        // Used for enabling/disabling the submit button
        $scope.okayToDelete = false;
        
        $scope.typed = function () {
            // For user convenience, compare lower case values so that check is not case-sensitive
            $scope.okayToDelete = ($scope.confirmClientName.toLowerCase() === client.name.toLowerCase());
        };
        
        // Yes, delete the API
        $scope.yes = function () {
            var deleteAction = {
                entityId: client.id,
                entityType: 'clients',
                organizationId: params.org
            };
            
            OrgSvcs.remove(deleteAction).$promise.then(function(res) {
                $scope.okayToDelete = false;
                
                setTimeout(function() {
                    $uibModalInstance.close();
                    
                    // Redirect users to their list of APIs
                    $location.path($rootScope.pluginName + '/users/' + Configuration.user.username + '/clients');
                }, 800);
                
                // We should display some type of Toastr/Growl notification to the user here
            }, function(err) {
                $scope.okayToDelete = false;
                $uibModalInstance.close();
                PageLifecycle.handleError(err);
            });
        };
        
        // No, do NOT delete the API
        $scope.no = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });

    export var ModalSelectApiCtrl = _module.controller('ModalSelectApiCtrl',
        ['$scope', '$uibModalInstance', 'ApimanSvcs', 'Logger', 'OrgSvcs', 'options',
        ($scope, $uibModalInstance, ApimanSvcs, Logger, OrgSvcs, options) => {

                $scope.options = options;
                $scope.selectedApi = undefined;
                $scope.selectedApiVersion = undefined;
                $scope.title = options.title;

                $scope.search = function () {
                    $scope.selectedApi = undefined;

                    if (!$scope.searchText) {
                        $scope.criteria = undefined;
                        $scope.apis = undefined;
                    } else {
                        $scope.searchButton.state = 'in-progress';

                        var body:any = {};
                        body.filters = [];

                        body.filters.push({
                            'name': 'name',
                            'value': '%' + $scope.searchText + '%',
                            'operator': 'like'
                        });
                        body.page = 1;
                        body.pageSize = 0x7fffffff; // Java Interger.MAX_VALUE

                        var searchStr = angular.toJson(body);

                        Logger.log('Searching for apis: {0}', $scope.searchText);

                        ApimanSvcs.save({
                            entityType: 'search',
                            secondaryType: 'apis'
                        }, searchStr, function (reply) {
                            if (reply.beans.length > 0) {
                                $scope.apis = reply.beans;
                            } else {
                                $scope.apis = undefined;
                            }

                            $scope.criteria = $scope.searchText;

                            Logger.log('Found {0} apis.', reply.beans.length);

                            $scope.searchButton.state = 'complete';
                        }, function (error) {
                            Logger.error(error);

                            // TODO do something interesting with the error
                            $scope.apis = undefined;
                            $scope.criteria = $scope.searchText;
                            $scope.searchButton.state = 'error';
                        });
                    }
                };
                
                $scope.$watch('selectedApiVersion', function(newValue) {
                    Logger.info("===========> Api Version: {0}", newValue);
                }, false);

                $scope.onApiSelected = function (api) {
                    if ($scope.selectedApi) {
                        $scope.selectedApi.selected = false;
                    }

                    $scope.selectedApi = api;
                    api.selected = true;
                    $scope.selectedApiVersion = undefined;

                    OrgSvcs.query({
                        organizationId: api.organizationId,
                        entityType: 'apis',
                        entityId: api.id,
                        versionsOrActivity: 'versions'
                    }, function (versions) {
                        if ($scope.options.publishedOnly === true) {
                            var validVersions = [];

                            angular.forEach(versions, function (version) {
                                if (version.status == 'Published') {
                                    validVersions.push(version);
                                }
                            });

                            $scope.apiVersions = validVersions;
                        } else {
                            $scope.apiVersions = versions;
                        }

                        if ($scope.apiVersions.length > 0) {
                            $scope.selectedApiVersion = $scope.apiVersions[0];
                        }
                    }, function (error) {
                        $scope.apiVersions = [];
                        $scope.selectedApiVersion = undefined;
                    });
                };
                
                $scope.onApiVersionSelected = function (apiVersion) {
                    Logger.info("===========> Called onApiVersionSelected: {0}", apiVersion);
                    $scope.selectedApiVersion = apiVersion;
                };

                $scope.ok = function () {
                    $uibModalInstance.close($scope.selectedApiVersion);
                };

                $scope.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            }]);



    export var ModalGetValueCtrl = _module.controller('ModalGetValueCtrl',
        ['$scope', '$uibModalInstance', 'Logger', 'options',
        ($scope, $uibModalInstance, Logger, options) => {
                $scope.options = options;
                $scope.title = $scope.options.title;
                $scope.message = $scope.options.message;
                $scope.label = $scope.options.label;
                $scope.value = $scope.options.initialValue;
                
                $scope.ok = function () {
                    $uibModalInstance.close($scope.value);
                };

                $scope.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        ]
    );

    export var ModalConfirmCtrl = _module.controller('ModalConfirmCtrl',
        ['$scope', '$uibModalInstance', 'Logger', 'options',
        ($scope, $uibModalInstance, Logger, options) => {

                $scope.options = options;
                $scope.title = $scope.options.title;
                $scope.message = $scope.options.message;

                $scope.ok = function () {
                    $uibModalInstance.close();
                };

                $scope.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        ]
    );

}