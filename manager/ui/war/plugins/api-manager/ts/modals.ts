import angular = require("angular");

const _module = angular.module('ApimanModals', ['ApimanLogger', 'ApimanRPC']);

_module.factory('Modals',
    ['Logger', '$uibModal',
        function (Logger, $uibModal) {
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
                },
                // A standard error dialog
                /////////////////////////////////
                error: function (title, message, okCallBack) {
                    var options = {
                        title: title,
                        message: message
                    };

                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'errorModal.html',
                        controller: 'ModalErrorCtrl',
                        resolve: {
                            options: function () {
                                return options;
                            }
                        }
                    });

                    modalInstance.result.then(okCallBack);
                },
                rpcerror: function (rpcdata, title, okCallBack) {
                    let message = "An unspecified error occurred when calling the Manager API";

                    if (title == null) {
                        title = "We ran into an error!"
                    }

                    try {
                        switch (typeof(rpcdata)) {
                            case "string":
                                message = JSON.parse(rpcdata).message;
                                break;

                            case "object":
                                message = rpcdata.message;
                                break;

                            default:
                                alert(typeof(rpcdata))
                                break;
                        }

                    } catch(error) {
                        console.error(error)
                    }

                    const options = {
                        title: title,
                        message: message

                    };

                    const modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'errorModal.html',
                        controller: 'ModalErrorCtrl',
                        resolve: {
                            options: function () {
                                return options;
                            }
                        }
                    });

                    modalInstance.result.then(okCallBack);

                }
            }
        }]);


_module.controller('ModalClientAppDeleteCtrl',
    ['$location', '$rootScope', '$scope', '$uibModalInstance', 'OrgSvcs', 'Configuration', 'PageLifecycle', 'client', 'params',
    function ($location, $rootScope, $scope, $uibModalInstance, OrgSvcs, Configuration, PageLifecycle, client, params) {

        $scope.confirmClientName = '';
        $scope.client = client;

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

            OrgSvcs.remove(deleteAction).$promise.then(function (res) {
                $scope.okayToDelete = false;

                setTimeout(function () {
                    $uibModalInstance.close();

                    // Redirect users to their list of APIs
                    $location.path($rootScope.pluginName + '/users/' + Configuration.user.username + '/clients');
                }, 800);

                // We should display some type of Toastr/Growl notification to the user here
            }, function (err) {
                $scope.okayToDelete = false;
                $uibModalInstance.close();
                PageLifecycle.handleError(err);
            });
        };

        // No, do NOT delete the API
        $scope.no = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);

_module.controller('ModalSelectApiCtrl',
    ['$scope', '$uibModalInstance', 'ApimanSvcs', 'Logger', 'OrgSvcs', 'options',
        function ($scope, $uibModalInstance, ApimanSvcs, Logger, OrgSvcs, options) {

            $scope.options = options;
            $scope.selectedApi = undefined;
            $scope.selectedApiVersion = undefined;
            $scope.title = options.title;
            $scope.searchText = '*';

            $scope.search = function () {
                $scope.selectedApi = undefined;

                if (!$scope.searchText) {
                    $scope.criteria = undefined;
                    $scope.apis = undefined;
                } else {
                    $scope.searchButton.state = 'in-progress';

                    var body: any = {};
                    body.filters = [];

                    body.filters.push({
                        'name': 'name',
                        'value': '%' + $scope.searchText + '%',
                        'operator': 'like'
                    });
                    body.page = 1;
                    body.pageSize = 10000; // ES index.max_result_window

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

            $scope.$watch('selectedApiVersion', function (newValue) {
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

            angular.element(document).ready(() => {
                $('.form-control').val('');
                $scope.search()
            })
        }]);



_module.controller('ModalGetValueCtrl',
    ['$scope', '$uibModalInstance', 'Logger', 'options',
        function ($scope, $uibModalInstance, Logger, options) {
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

_module.controller('ModalConfirmCtrl',
    ['$scope', '$uibModalInstance', 'Logger', 'options',
        function ($scope, $uibModalInstance, Logger, options) {

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

_module.controller('ModalErrorCtrl',
    ['$scope', '$uibModalInstance', 'Logger', 'options',
        function ($scope, $uibModalInstance, Logger, options) {

            $scope.options = options;
            $scope.title = options.title;
            $scope.message = options.message;

            $scope.ok = function () {
                $uibModalInstance.close();
            };
        }
    ]
);