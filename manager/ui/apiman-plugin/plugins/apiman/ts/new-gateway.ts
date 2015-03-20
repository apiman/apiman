/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var NewGatewayController = _module.controller("Apiman.NewGatewayController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'CurrentUser',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, CurrentUser) => {
           
            $scope.createGateway = function() {
                var gateway = $scope.gateway;
                gateway.configuration = JSON.stringify($scope.configuration);
                
                $scope.createButton.state = 'in-progress';
                ApimanSvcs.save({ entityType: 'gateways' }, gateway, function(reply) {
                    $location.path(pluginName + '/admin-gateways.html');
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                        $scope.createButton.state = 'error';
                    }
                });
            };
            
            PageLifecycle.loadPage('NewGateway', undefined, $scope);
            $('#name').focus();
        }]);

}
