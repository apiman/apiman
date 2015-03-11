/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var EditGatewayController = _module.controller("Apiman.EditGatewayController",
        ['$q', '$scope', '$location', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, $location, ApimanSvcs, PageLifecycle) => {
            
            var params = $location.search();
            
            var promise = $q.all({
                gateway: $q(function(resolve, reject) {
                    ApimanSvcs.get({ entityType: 'gateways', secondaryType: params.gateway }, function(gateway) {
                        resolve(gateway);
                        $scope.configuration = JSON.parse(gateway.configuration);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            
            $scope.updateGateway  = function() {
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
            
            PageLifecycle.loadPage('EditGateway', promise, $scope);
    }])

}
