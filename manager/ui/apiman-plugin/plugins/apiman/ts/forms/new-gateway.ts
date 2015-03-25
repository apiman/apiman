/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewGatewayController = _module.controller("Apiman.NewGatewayController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'CurrentUser',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, CurrentUser) => {
           
            $scope.testButton = {};
            $scope.testButton.state = 'warning';
            $scope.errorStatus = null;
            
            var passwordMatch = function() {
                if ($scope.configuration.password != $scope.passwordConfirm) {
                    $scope.isMatch = false;
                    $scope.errorStatus = "Passwords don't match";
                } else {
                    $scope.isMatch = true;
                    $scope.errorStatus = null;
                }
            }
            
            var testGateway  = function() {
                passwordMatch();
                if ($scope.errorStatus==null) {
                    var gateway:any = {};
                    gateway.configuration = JSON.stringify($scope.configuration);
                    gateway.type = $scope.gateway.type;
                    gateway.description = $scope.gateway.description;
                    ApimanSvcs.update({ entityType: 'gateways' }, gateway, function(reply) {
                        if (reply.success == true) {
                            $scope.errorStatus = null;
                            $scope.testButton.state = 'success';
                        } else {
                            $scope.errorStatus = "Cannot connect to gateway: " + reply.detail;
                            $scope.testButton.state = 'danger';
                        }
                    }, function(error) {
                        if (error.status == 409) {
                            $location.path('apiman/error-409.html');
                        } else {
                            $scope.testButton.state = 'error';
                            alert("ERROR=" + error.status + " " + error.statusText);
                        }
                    });
                }
            }
            
            
            $scope.createGateway = function() {
                var gateway = $scope.gateway;
                gateway.configuration = JSON.stringify($scope.configuration);
                gateway.type = "REST";
                
                ApimanSvcs.save({ entityType: 'gateways' }, gateway, function(reply) {
                    $location.path(pluginName + '/admin-gateways.html');
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            $scope.passwordMatch = passwordMatch;
            $scope.testGateway = testGateway;
            PageLifecycle.loadPage('NewGateway', undefined, $scope);
            $('#name').focus();
        }]);

}
