/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewGatewayController = _module.controller("Apiman.NewGatewayController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'CurrentUser', 'Logger',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, CurrentUser, Logger) => {
            $scope.isValid = false;
            $scope.gateway = {};
            $scope.configuration = {
                endpoint: 'http://localhost:8080/apiman-gateway-api/'
            };
            
            var validate = function() {
                $scope.testResult = 'none';
                var valid = true;
                if (!$scope.gateway.name) {
                    valid = false;
                }
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
            };
            
            $scope.$watch('gateway', validate, true);
            $scope.$watch('configuration', validate, true);
            $scope.$watch('passwordConfirm', validate);
            
            var Gateway = function() {
                var gateway = $scope.gateway;
                gateway.configuration = angular.toJson($scope.configuration);
                gateway.type = 'REST';
                return gateway;
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
                    $scope.testButton.state = 'error';
                    $scope.testResult = 'error';
                    $scope.testErrorMessage = error;
                });
            };
            
            $scope.createGateway = function() {
                $scope.createButton.state = 'in-progress';
                var gateway = Gateway();
                ApimanSvcs.save({ entityType: 'gateways' }, gateway, function(reply) {
                    PageLifecycle.redirectTo('/admin/gateways');
                }, PageLifecycle.handleError);
            };
            
            $scope.testGateway = testGateway;
            PageLifecycle.loadPage('NewGateway', 'admin', undefined, $scope, function() {
                PageLifecycle.setPageTitle('new-gateway');
                $('#apiman-gateway-name').focus();
            });
        }]);

}
