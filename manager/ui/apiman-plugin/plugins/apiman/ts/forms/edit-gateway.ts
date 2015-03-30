/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var EditGatewayController = _module.controller("Apiman.EditGatewayController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', 'Dialogs',
        ($q, $scope, $location, ApimanSvcs, PageLifecycle, Dialogs) => {
            $scope.isValid = false;
            var params = $location.search();
            
            var validate = function() {
                $scope.testResult = 'none';
                // First validation
                var valid = true;
                if (!$scope.configuration.endpoint) {
                    valid = false;
                }
                if (!$scope.configuration.username) {
                    valid = false;
                }
                if (!$scope.configuration.password) {
                    valid = false;
                }
                if ($scope.configuration.password != $scope.passwordConfirm) {
                    valid = false;
                }
                $scope.isValid = valid;
                
                // Now dirty
                var dirty = false;
                if ($scope.gateway.description != $scope.originalGateway.description) {
                    dirty = true;
                }
                if ($scope.configuration.endpoint != $scope.originalConfig.endpoint) {
                    dirty = true;
                }
                if ($scope.configuration.username != $scope.originalConfig.username) {
                    dirty = true;
                }
                if ($scope.configuration.password != $scope.originalConfig.password) {
                    dirty = true;
                }
                $scope.isDirty = dirty;
            };
            
            var Gateway = function() {
                return {
                    description: $scope.gateway.description,
                    type: $scope.gateway.type,
                    configuration: JSON.stringify($scope.configuration)
                };
            };

            var promise = $q.all({
                gateway: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'gateways', secondaryType: params.gateway }, function(gateway) {
                        $scope.gateway = gateway;
                        $scope.configuration = JSON.parse(gateway.configuration);
                        $scope.passwordConfirm = $scope.configuration.password;
                        $scope.originalGateway = angular.copy(gateway);
                        $scope.originalConfig = angular.copy($scope.configuration);
                        $scope.isDirty = false;
                        resolve(gateway);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            
            var testGateway  = function() {
                $scope.testButton.state = 'in-progress';
                var gateway = Gateway();
                ApimanSvcs.update({ entityType: 'gateways' }, gateway, function(reply) {
                    $scope.testButton.state = 'complete';
                    if (reply.success == true) {
                        Logger.info('Connected successfully to Gateway: {0}', reply.detail);
                        $scope.testResult = 'success';
                    } else {
                        Logger.info('Failed to connect to Gateway: {0}', reply.detail);
                        $scope.testResult = 'error';
                        $scope.testErrorMessage = reply.detail;
                    }
                }, function(error) {
                    alert(error);
                    // TODO handle this error
                    $scope.testButton.state = 'error';
                });
            }
            
            $scope.updateGateway  = function() {
                $scope.updateButton.state = 'in-progress';
                var gateway = Gateway();
                ApimanSvcs.update({ entityType: 'gateways', secondaryType: $scope.gateway.id }, gateway, function() {
                     $location.url(pluginName + '/admin-gateways.html');
                }, function(error) {
                    alert(error);
                    // TODO handle this error better
                    $scope.updateButton.state = 'error';
                });
            }
            
            $scope.deleteGateway  = function() {
                $scope.deleteButton.state = 'in-progress';
                Dialogs.confirm('Confirm Delete Gateway', 'Do you really want to permanently delete this gateway?  This can be very destructive to any Service published to it.', function() {
                    ApimanSvcs.delete({ entityType: 'gateways', secondaryType: $scope.gateway.id }, function(reply) {
                        $location.url(pluginName + '/admin-gateways.html');
                    }, function(error) {
                        alert(error);
                        // TODO handle this error better
                        $scope.deleteButton.state = 'error';
                    });
                }, function() {
                    $scope.deleteButton.state = 'complete';
                });
            }
            $scope.testGateway = testGateway;
            PageLifecycle.loadPage('EditGateway', promise, $scope, function() {
                PageLifecycle.setPageTitle('edit-gateway');
                $scope.$watch('gateway', validate, true);
                $scope.$watch('configuration', validate, true);
                $scope.$watch('passwordConfirm', validate);
                $('#apiman-gateway-description').focus();
            });
    }])

}
