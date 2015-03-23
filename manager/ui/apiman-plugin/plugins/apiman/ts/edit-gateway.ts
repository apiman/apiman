/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var EditGatewayController = _module.controller("Apiman.EditGatewayController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, $location, ApimanSvcs, PageLifecycle) => {
            
            var params = $location.search();
            $scope.testButton = {};
            $scope.testButton.state = 'warning';
            $scope.errorStatus = null;
            
            var promise = $q.all({
                gateway: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'gateways', secondaryType: params.gateway }, function(gateway) {
                        $scope.gateway = gateway;
                        resolve(gateway);
                        $scope.configuration = JSON.parse(gateway.configuration);
                        $scope.passwordConfirm = $scope.configuration.password;
                        testGateway();
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            
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
            
            $scope.updateGateway  = function() {
                if ($scope.errorStatus = null) {
                    var gateway:any = {};
                    gateway.configuration = JSON.stringify($scope.configuration);
                    gateway.type = $scope.gateway.type;
                    gateway.description = $scope.gateway.description;
                    ApimanSvcs.update({ entityType: 'gateways', secondaryType: $scope.gateway.id }, gateway, function(reply) {
                         $location.path(pluginName + '/admin-gateways.html');
                    }, function(error) {
                        if (error.status == 409) {
                            $location.path('apiman/error-409.html');
                        } else {
                            $scope.createButton.state = 'error';
                            alert("ERROR=" + error.status + " " + error.statusText);
                        }
                    });
                 }
            }
            
            $scope.deleteGateway  = function() {
                ApimanSvcs.delete({ entityType: 'gateways', secondaryType: $scope.gateway.id }, function(reply) {
                     $location.path(pluginName + '/admin-gateways.html');
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        $scope.createButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            }
            $scope.passwordMatch = passwordMatch;
            $scope.testGateway = testGateway;
            PageLifecycle.loadPage('EditGateway', promise, $scope);
    }])

}
