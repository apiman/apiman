/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var EditGatewayController = _module.controller("Apiman.EditGatewayController",
        ['$location', '$q', '$rootScope', '$routeParams', '$scope', '$uibModal', 'ApimanSvcs', 'PageLifecycle',
        ($location, $q, $rootScope, $routeParams, $scope, $uibModal, ApimanSvcs, PageLifecycle) => {
            $scope.isValid = false;
            var params = $routeParams;
            
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
                $rootScope.isDirty = dirty;
            };
            
            var Gateway = function() {
                return {
                    description: $scope.gateway.description,
                    type: $scope.gateway.type,
                    configuration: angular.toJson($scope.configuration)
                };
            };

            var pageData = {
                gateway: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'gateways', secondaryType: params.gateway }, function(gateway) {
                        $scope.gateway = gateway;
                        $scope.configuration = JSON.parse(gateway.configuration);
                        $scope.passwordConfirm = $scope.configuration.password;
                        $scope.originalGateway = angular.copy(gateway);
                        $scope.originalConfig = angular.copy($scope.configuration);
                        $rootScope.isDirty = false;
                        resolve(gateway);
                    }, reject);
                })
            };
            
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
                    var charRegExp = /([\\]*)?"(pass|password)([\\]*)?":([\\]*)?"(.+?)([\\]*)?"/g;
                    var regTest = charRegExp.test(JSON.stringify(error));

                    if (regTest === true) {
                        error = JSON.stringify(error).replace(charRegExp, '\\"password\\":\\"*****\\"');
                    }

                    $scope.testButton.state = 'error';
                    $scope.testResult = 'error';
                    $scope.testErrorMessage = error;
                });
            };

            $scope.cancel = function() {
                $rootScope.isDirty = false;
                $location.path( $rootScope.pluginName + '/admin/gateways');
            };
            
            $scope.updateGateway  = function() {
                $scope.updateButton.state = 'in-progress';
                var gateway = Gateway();
                ApimanSvcs.update({ entityType: 'gateways', secondaryType: $scope.gateway.id }, gateway, function() {
                    $rootScope.isDirty = false;
                    PageLifecycle.redirectTo('/admin/gateways');
                }, PageLifecycle.handleError);
            };
            
            $scope.deleteGateway  = function(size) {
                $scope.deleteButton.state = 'in-progress';

                var options = {
                    message: 'Do you really want to permanently delete this gateway?  This can be very destructive to any API published to it.',
                    title: 'Confirm Delete Gateway'
                };

                $scope.animationsEnabled = true;

                $scope.toggleAnimation = function () {
                    $scope.animationsEnabled = !$scope.animationsEnabled;
                };

                var modalInstance = $uibModal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'confirmModal.html',
                    controller: 'ModalConfirmCtrl',
                    size: size,
                    resolve: {
                        options: function () {
                            return options;
                        }
                    }
                });

                modalInstance.result.then(function () {
                    ApimanSvcs.delete({ entityType: 'gateways', secondaryType: $scope.gateway.id }, function(reply) {
                        PageLifecycle.redirectTo('/admin/gateways');
                    }, PageLifecycle.handleError);
                }, function () {
                    //console.log('Modal dismissed at: ' + new Date());
                    $scope.deleteButton.state = 'complete';
                });
            };

            $scope.testGateway = testGateway;

            PageLifecycle.loadPage('EditGateway', 'admin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('edit-gateway');
                $scope.$watch('gateway', validate, true);
                $scope.$watch('configuration', validate, true);
                $scope.$watch('passwordConfirm', validate);
                $('#apiman-gateway-description').focus();
            });
    }])

}
