/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminGatewaysController = _module.controller("Apiman.AdminGatewaysController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, ApimanSvcs, PageLifecycle) => {
            var promise = $q.all({
                gateways: $q(function(resolve, reject) {
                    ApimanSvcs.query({ entityType: 'gateways' }, function(adminGateways) {
                        resolve(adminGateways);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('AdminGateways', promise, $scope, function() {
                PageLifecycle.setPageTitle('admin-gateways');
            });
    }])

}
